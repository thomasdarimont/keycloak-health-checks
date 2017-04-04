# Keycloak Health Checks

A collection of health checks for KeyCloak subsystems.

## Requirements

* KeyCloak 2.5.5.Final (works also with Keycloak 3.0.0.Final)

## Build

`mvn install`

## Installation

After the extension has been built, install it as a JBoss/WildFly module via `jboss-cli`:

```
[disconnected /] module add --name=de.tdlabs.keycloak.extensions.keycloak-health-checks --resources=/home/tom/dev/repos/gh/thomasdarimont/keycloak-dev/keycloak-health-checks/target/keycloak-health-checks-1.0.0-SNAPSHOT.jar --dependencies=org.keycloak.keycloak-core,org.keycloak.keycloak-services,org.keycloak.keycloak-server-spi,org.keycloak.keycloak-server-spi-private,javax.api,javax.ws.rs.api,com.fasterxml.jackson.core.jackson-core,com.fasterxml.jackson.core.jackson-databind,com.fasterxml.jackson.core.jackson-annotations

```

Alternatively, create `$KEYCLOAK_HOME/modules/beercloak/main/module.xml` to load extension from the local Maven repo:

```xml
<?xml version="1.0" ?>
<module xmlns="urn:jboss:module:1.1" name="de.tdlabs.keycloak.extensions.keycloak-health-checks">

    <resources>
        <artifact name="de.tdlabs.keycloak:keycloak-health-checks:1.0.0-SNAPSHOT"/>        
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
                <provider>module:de.tdlabs.keycloak.extensions.keycloak-health-checks</provider>
            </providers>
...
```

... or register the provider via the module via `jboss-cli`:
```
/subsystem=keycloak-server:list-add(name=providers,value=module:de.tdlabs.keycloak.extensions.keycloak-health-checks)
```

## Running example

Start Keycloak and browse to: http://localhost:8080/auth/realms/master/health/check

You should now see something like with `HTTP Status 200 OK`
```json
{
   "details":{
      "database":{
         "state":"UP"
      },
      "filesystem":{
         "state":"UP"
      }
   },
   "name":"keycloak",
   "state":"UP"
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
      }
   },
   "name":"keycloak",
   "state":"DOWN"
}
```

# TODO

## Add health-check for infinispan once Keycloak is on infinispan 9.0.x
http://blog.infinispan.org/2017/03/checking-infinispan-cluster-health-and.html