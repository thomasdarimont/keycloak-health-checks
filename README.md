# Keycloak Health Checks

A collection of health checks for KeyCloak subsystems.

## Requirements

* KeyCloak 3.4.0.Final

## Build

`mvn install`

## Installation

After the extension has been built, install it as a JBoss/WildFly module via `jboss-cli`:

```
module add --name=de.tdlabs.keycloak.extensions.keycloak-health-checks --resources=target/keycloak-health-checks.jar --module-xml=src/main/keycloak/module.xml

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