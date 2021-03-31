@Library('unifly-jenkins-common') _

pipeline {
    agent any

    options {
        timestamps()
        buildDiscarder(
                logRotator(
                    // number of build logs to keep
                    numToKeepStr:'50',
                    // history to keep in days
                    daysToKeepStr: '60',
                    // number of builds have their artifacts kept
                    artifactNumToKeepStr: '1'
                )
            )
        disableConcurrentBuilds()
        skipStagesAfterUnstable()
    }

    environment {
        GIT_REPO = 'https://github.com/unifly-aero/keycloak-health-checks.git'
        CREDENTIALS_ID = 'unifly-jenkins'
        JAVA_HOME="${tool 'openjdk-11'}"
        PATH="${env.JAVA_HOME}/bin:${tool 'nodejs-12'}/bin:${env.PATH}"
        ORG_GRADLE_PROJECT_uniflyVersionTargetBranch="${env.BRANCH_NAME}"
        UNIFLY_ARTIFACTORY = credentials('unifly-artifactory')
        artifactory_user = "$UNIFLY_ARTIFACTORY_USR"
        artifactory_password = "$UNIFLY_ARTIFACTORY_PSW"
    }

    stages {

        stage('Package') {
            steps {
                sh "./mvnw package"
            }
        }

        stage('Publish') {
            when { not { changeRequest() } }
            steps {
                  sh "./mvnw -s settings.xml deploy"
            }
        }
    }

    post {
        failure {
            sendSummary()
        }
        fixed {
            sendSummary()
        }
    }
}