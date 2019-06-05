# Keycloak Health Checks

A collection of health-checks for Keycloak subsystems.

## Requirements

* Keycloak 6.0.1

## Build

`mvn install`

## Installation

After the extension has been built, install it as a JBoss/WildFly module via `jboss-cli`:

```

$ bin/jboss-cli.sh 
You are disconnected at the moment. Type 'connect' to connect to the server or 'help' for the list of supported commands.
[disconnected /] connect
[standalone@localhost:9990 /] 

[standalone@localhost:9990 /] module add --name=com.github.thomasdarimont.keycloak.extensions.keycloak-health-checks --resources=/home/tom/dev/repos/gh/thomasdarimont/keycloak-dev/keycloak-health-checks/target/keycloak-health-checks-6.0.1.0-SNAPSHOT.jar --dependencies=org.keycloak.keycloak-core,org.keycloak.keycloak-services,org.keycloak.keycloak-server-spi,org.keycloak.keycloak-server-spi-private,javax.api,javax.ws.rs.api,com.fasterxml.jackson.core.jackson-core,com.fasterxml.jackson.core.jackson-databind,com.fasterxml.jackson.core.jackson-annotations,org.jboss.logging,org.infinispan,org.infinispan.commons
```

Alternatively, create `$KEYCLOAK_HOME/modules/com/github/thomasdarimont/keycloak/extensions/keycloak-health-checks/main/module.xml` to load extension from the local Maven repo:

```xml
<?xml version="1.0" ?>
<module xmlns="urn:jboss:module:1.1" name="com.github.thomasdarimont.keycloak.extensions.keycloak-health-checks">

    <resources>
        <artifact name="com.github.thomasdarimont.keycloak:keycloak-health-checks:6.0.1.0-SNAPSHOT"/>
    </resources>

    <dependencies>
        <module name="org.keycloak.keycloak-core"/>
        <module name="org.keycloak.keycloak-services"/>
        <module name="org.keycloak.keycloak-server-spi"/>
        <module name="org.keycloak.keycloak-server-spi-private"/>
        <module name="javax.api"/>
        <module name="javax.ws.rs.api"/>
        <module name="com.fasterxml.jackson.core.jackson-core"/>
        <module name="com.fasterxml.jackson.core.jackson-databind"/>
        <module name="com.fasterxml.jackson.core.jackson-annotations"/>
        <module name="org.jboss.logging"/>
        <module name="org.infinispan"/>
        <module name="org.infinispan.commons"/>
    </dependencies>
</module>
```

## Configuration

Edit the wildfly `standalone.xml` or `standalone-ha.xml`
`$KEYCLOAK_HOME/standalone/configuration/standalone.xml`:

```xml
...
        <subsystem xmlns="urn:jboss:domain:keycloak-server:1.1">
            <web-context>auth</web-context>
            <providers>
                <provider>
                    classpath:${jboss.home.dir}/providers/*
                </provider>
                <provider>module:com.github.thomasdarimont.keycloak.extensions.keycloak-health-checks</provider>
            </providers>
...
```

... or register the provider via the module via `jboss-cli`:
```
/subsystem=keycloak-server:list-add(name=providers,value=module:com.github.thomasdarimont.keycloak.extensions.keycloak-health-checks)
```

## Uninstall

To uninstall the provider just remove the ... from `standalone.xml` or `standalone-ha.xml`.
To uninstall the module just remove the `com/github/thomasdarimont...` directory in your `modules` folder.

## Running example

Start Keycloak and browse to: `http://localhost:8080/auth/realms/master/health/check`

You should now see something like with `HTTP Status 200 OK`

```
curl -v http://localhost:8080/auth/realms/master/health/check | jq -C .
...
< HTTP/1.1 200 OK
< Connection: keep-alive
< Content-Type: application/json
< Content-Length: 1090
< Date: Wed, 06 Feb 2019 19:09:42 GMT
```

```json
{
  "details": {
    "database": {
      "connection": "established",
      "state": "UP"
    },
    "filesystem": {
      "freebytes": 288779120640,
      "state": "UP"
    },
    "infinispan": {
      "numberOfNodes": 1,
      "state": "UP",
      "healthStatus": "HEALTHY",
      "nodeNames": [],
      "cacheDetails": [
        {
          "cacheName": "realms",
          "healthStatus": "HEALTHY"
        },
        {
          "cacheName": "authenticationSessions",
          "healthStatus": "HEALTHY"
        },
        {
          "cacheName": "sessions",
          "healthStatus": "HEALTHY"
        },
        {
          "cacheName": "authorizationRevisions",
          "healthStatus": "HEALTHY"
        },
        {
          "cacheName": "clientSessions",
          "healthStatus": "HEALTHY"
        },
        {
          "cacheName": "work",
          "healthStatus": "HEALTHY"
        },
        {
          "cacheName": "keys",
          "healthStatus": "HEALTHY"
        },
        {
          "cacheName": "users",
          "healthStatus": "HEALTHY"
        },
        {
          "cacheName": "loginFailures",
          "healthStatus": "HEALTHY"
        },
        {
          "cacheName": "offlineClientSessions",
          "healthStatus": "HEALTHY"
        },
        {
          "cacheName": "authorization",
          "healthStatus": "HEALTHY"
        },
        {
          "cacheName": "realmRevisions",
          "healthStatus": "HEALTHY"
        },
        {
          "cacheName": "offlineSessions",
          "healthStatus": "HEALTHY"
        },
        {
          "cacheName": "actionTokens",
          "healthStatus": "HEALTHY"
        },
        {
          "cacheName": "userRevisions",
          "healthStatus": "HEALTHY"
        }
      ],
      "clusterName": "ISPN"
    }
  },
  "name": "keycloak",
  "state": "UP"
}
```

In case a check fails, you should get a response with `HTTP Status 503 SERVICE UNAVAILABLE` with a body like:
```json
{
   "details":{
      "filesystem":{
         "state":"UP"
      },
      "database":{
         "message":"javax.resource.ResourceException: IJ000453: Unable to get managed connection for java:jboss/datasources/KeycloakDS",
         "state":"DOWN"
      },
      "infinispan": {
         "numberOfNodes": 1,
         "state": "UP",
         "healthStatus": "HEALTHY",
         "nodeNames": [],
         "cacheDetails": [
         {
           "cacheName": "realms",
           "healthStatus": "HEALTHY"
         },
         {
           "cacheName": "authenticationSessions",
           "healthStatus": "HEALTHY"
         },
         {
           "cacheName": "sessions",
           "healthStatus": "HEALTHY"
         },
         {
           "cacheName": "authorizationRevisions",
           "healthStatus": "HEALTHY"
         },
         {
           "cacheName": "clientSessions",
           "healthStatus": "HEALTHY"
         },
         {
           "cacheName": "work",
           "healthStatus": "HEALTHY"
         },
         {
           "cacheName": "keys",
           "healthStatus": "HEALTHY"
         },
         {
           "cacheName": "users",
           "healthStatus": "HEALTHY"
         },
         {
           "cacheName": "loginFailures",
           "healthStatus": "HEALTHY"
         },
         {
           "cacheName": "offlineClientSessions",
           "healthStatus": "HEALTHY"
         },
         {
           "cacheName": "authorization",
           "healthStatus": "HEALTHY"
         },
         {
           "cacheName": "realmRevisions",
           "healthStatus": "HEALTHY"
         },
         {
           "cacheName": "offlineSessions",
           "healthStatus": "HEALTHY"
         },
         {
           "cacheName": "actionTokens",
           "healthStatus": "HEALTHY"
         },
         {
           "cacheName": "userRevisions",
           "healthStatus": "HEALTHY"
         }],
         "clusterName": "ISPN"
        }
   },
   "name":"keycloak",
   "state":"DOWN"
}
```

You can also query the health-checks individually by appending the name of the check to the end of `/health` endpoint URL. 

The following health-checks are currently available:
* `database`
* `filesystem` 
* `infinispan` 

```
$ curl -s http://localhost:8080/auth/realms/master/health/check/database | jq -C .
{
  "state": "UP",
  "details": {
    "connection": "established",
    "state": "UP"
  },
  "name": "database"
}

```

## Securing the health endpoint

The health endpoint should not be directly exposed to the internet. There are multiple ways to properly secure Keycloak endpoints
like firewalls, reverse-proxies, or JBoss / wildfly specific configuration options.

The keycloak documentation provides additional information about [securing admin endpoints](https://github.com/keycloak/keycloak-documentation/blob/master/server_admin/topics/threat/admin.adoc#port-restriction). The same mechanism
can be used to protect the health-endpoints. 
