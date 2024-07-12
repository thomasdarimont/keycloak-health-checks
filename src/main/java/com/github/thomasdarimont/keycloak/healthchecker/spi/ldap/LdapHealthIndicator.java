package com.github.thomasdarimont.keycloak.healthchecker.spi.ldap;

import com.github.thomasdarimont.keycloak.healthchecker.model.HealthStatus;
import com.github.thomasdarimont.keycloak.healthchecker.spi.AbstractHealthIndicator;
import com.github.thomasdarimont.keycloak.healthchecker.support.ExceptionUtils;
import org.keycloak.Config;
import org.keycloak.component.ComponentModel;
import org.keycloak.models.KeycloakContext;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.LDAPConstants;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;
import org.keycloak.storage.UserStorageProvider;
import org.keycloak.storage.UserStorageProviderModel;
import org.keycloak.storage.ldap.LDAPStorageProvider;
import org.keycloak.storage.ldap.LDAPUtils;
import org.keycloak.storage.ldap.idm.model.LDAPObject;
import org.keycloak.storage.ldap.idm.query.Condition;
import org.keycloak.storage.ldap.idm.query.internal.LDAPQuery;
import org.keycloak.storage.ldap.idm.query.internal.LDAPQueryConditionsBuilder;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class LdapHealthIndicator extends AbstractHealthIndicator {

    public static final String USERNAME_PATTERN = "*";
    public static final int LDAP_QUERY_RESULT_LIMIT = 5;
    public static final String ERROR_PROVIDER_NOT_FOUND_BY_NAME = "provider_not_found_by_name";
    public static final String HINT_PROVIDER_NOT_ENABLED = "provider_not_enabled";
    public static final String ERROR_PROVIDER_NOT_FOUND_BY_ID = "provider_not_found_by_id";
    public static final String HINT_NO_RESULT = "no_result";
    public static final String PARAM_PROVIDER_NAME = "providerName";
    private final KeycloakSession session;
    private final Config.Scope config;

    public LdapHealthIndicator(KeycloakSession session, Config.Scope config) {
        super("ldap");
        this.session = session;
        this.config = config;
    }

    @Override
    public boolean isApplicable() {
        // do we have an enabled user storage provider
        return session.getContext().getRealm().getStorageProviders(LDAPStorageProvider.class)
                .findAny().isPresent();
    }

    @Override
    public HealthStatus check() {

        List<LdapStatusInfo> ldapLdapStatusInfos = checkLdapStatus();

        Map<String, LdapStatusInfo> aggregatedLdapStatus = new LinkedHashMap<>();
        boolean containsError = false;
        for (LdapStatusInfo statusInfo : ldapLdapStatusInfos) {
            if (Status.ERROR.equals(statusInfo.getStatus())) {
                containsError = true;
            }
            aggregatedLdapStatus.put(statusInfo.getProviderName(), statusInfo);
        }

        return (containsError
                ? reportDown()
                : reportUp())
                .withAttribute("ldapStatus", aggregatedLdapStatus);
    }

    protected List<LdapStatusInfo> checkLdapStatus() {

        KeycloakContext keycloakContext = session.getContext();
        RealmModel realm = keycloakContext.getRealm();

        String providerName = keycloakContext.getUri().getQueryParameters().getFirst(PARAM_PROVIDER_NAME);
        if (providerName != null) {
            // we want the status of a dedicated ldap provider
            return Collections.singletonList(checkLdapStatus(realm, providerName));
        }

        // we want the status of all registered dedicated ldap providers
        return realm.getStorageProviders(LDAPStorageProvider.class)
                .map(uspm -> checkLdapStatus(realm, uspm.getName(), uspm))
                .collect(Collectors.toList());
    }

    protected boolean isLdapUserStorageProvider(UserStorageProviderModel providerModel) {
        return providerModel != null && LDAPConstants.LDAP_PROVIDER.equals(providerModel.getProviderId());
    }

    protected LdapStatusInfo checkLdapStatus(RealmModel realm, String providerName) {
        return checkLdapStatus(realm, providerName, getLdapUserStorageProviderModelByName(realm, providerName));
    }

    protected LdapStatusInfo checkLdapStatus(RealmModel realm, String providerName, ComponentModel componentModel) {
        UserStorageProviderModel ldapProviderModel;
        if (componentModel instanceof UserStorageProviderModel) {
            ldapProviderModel = (UserStorageProviderModel) componentModel;
        } else {
            return LdapStatusInfo.error(providerName, ERROR_PROVIDER_NOT_FOUND_BY_NAME);
        }

        if (!ldapProviderModel.isEnabled()) {
            return LdapStatusInfo.ok(ldapProviderModel.getName(), HINT_PROVIDER_NOT_ENABLED);
        }

        try {
            LDAPStorageProvider ldapStorageProvider = (LDAPStorageProvider) session.getProvider(UserStorageProvider.class, ldapProviderModel);

            if (ldapStorageProvider == null) {
                return LdapStatusInfo.error(ldapProviderModel.getName(), ERROR_PROVIDER_NOT_FOUND_BY_ID);
            }

            List<LDAPObject> ldapObjects = queryLdap(realm, ldapStorageProvider);

            if (ldapObjects == null || ldapObjects.isEmpty()) {
                return LdapStatusInfo.ok(ldapProviderModel.getName(), HINT_NO_RESULT);
            }

        } catch (Exception ex) {
            Throwable rootCause = ExceptionUtils.getRootCause(ex);
            String hint = rootCause == null ? null : rootCause.getMessage();
            return LdapStatusInfo.error(ldapProviderModel.getName(), ex.getMessage(), hint);
        }

        return LdapStatusInfo.ok(ldapProviderModel.getName());
    }

    protected List<LDAPObject> queryLdap(RealmModel realm, LDAPStorageProvider ldapStorageProvider) {
        return searchUsersInLdapByUsername(realm, ldapStorageProvider, USERNAME_PATTERN);
    }

    protected UserStorageProviderModel getLdapUserStorageProviderModelByName(RealmModel realm, String providerName) {
        ComponentModel componentModel = realm.getStorageProviders(LDAPStorageProvider.class)
                .filter(provider -> provider.getName().equals(providerName))
                .findFirst()
                .orElse(null);
        if (componentModel instanceof UserStorageProviderModel) {
            return (UserStorageProviderModel) componentModel;
        } else {
            return null;
        }
    }

    protected List<LDAPObject> searchUsersInLdapByUsername(RealmModel realm, LDAPStorageProvider ldapStorageProvider, String usernamePattern) {

        try (LDAPQuery ldapQuery = LDAPUtils.createQueryForUserSearch(ldapStorageProvider, realm)) {
            LDAPQueryConditionsBuilder conditionsBuilder = new LDAPQueryConditionsBuilder();
            Condition usernameCondition = conditionsBuilder.equal(UserModel.USERNAME, usernamePattern);
            ldapQuery.addWhereCondition(usernameCondition);
            ldapQuery.setLimit(LDAP_QUERY_RESULT_LIMIT);

            return ldapQuery.getResultList();
        }
    }

    public static class LdapStatusInfo {

        private final String providerName;

        private final Status status;

        private final String errorMessage;

        private final String hint;

        public LdapStatusInfo(String providerName, Status status, String errorMessage, String hint) {
            this.providerName = providerName;
            this.status = status;
            this.errorMessage = errorMessage;
            this.hint = hint;
        }

        public String getProviderName() {
            return providerName;
        }

        public Status getStatus() {
            return status;
        }

        public String getErrorMessage() {
            return errorMessage;
        }

        public String getHint() {
            return hint;
        }

        public static LdapStatusInfo ok(String providerName) {
            return ok(providerName, null);
        }

        public static LdapStatusInfo ok(String providerName, String hint) {
            return new LdapStatusInfo(providerName, Status.OK, null, hint);
        }

        public static LdapStatusInfo error(String providerName, String errorMessage) {
            return error(providerName, errorMessage, null);
        }

        public static LdapStatusInfo error(String providerName, String errorMessage, String hint) {
            return new LdapStatusInfo(providerName, Status.ERROR, errorMessage, hint);
        }
    }

    enum Status {
        OK, ERROR
    }
}
