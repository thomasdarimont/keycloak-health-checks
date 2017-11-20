
if [ "$#" -ne 1 ]; then
    echo "Usage: > redeploy /path/to/keycloak"
  exit 1
fi

if $1/bin/jboss-cli.sh --command="module remove --name=de.tdlabs.keycloak.extensions.keycloak-health-checks"
then
   echo "Removed previous health-check"
fi

if $1/bin/jboss-cli.sh -c --command="/subsystem=keycloak-server:list-remove(name=providers,value=module:de.tdlabs.keycloak.extensions.keycloak-health-checks)"
then
   echo "Removed previous configuration"
fi

if mvn clean install
then
   echo "Built module"
else 
  echo "Unable to build module"
  exit 1
fi

if $1/bin/jboss-cli.sh --command="module add --name=de.tdlabs.keycloak.extensions.keycloak-health-checks --resources=target/keycloak-health-checks.jar --module-xml=src/main/keycloak/module.xml"
then
   echo "Installed health-check"
else 
  echo "Unable to install health-check"
  exit 1
fi

if $1/bin/jboss-cli.sh -c --command="/subsystem=keycloak-server:list-add(name=providers,value=module:de.tdlabs.keycloak.extensions.keycloak-health-checks)"
then
   echo "Added configuration"
else 
  echo "Unable to add configuration"
  exit 1
fi

if $1/bin/jboss-cli.sh -c --command=":shutdown(restart=true)"
then
   echo "Restarted"
else 
  echo "Unable to restart"
  exit 1
fi


