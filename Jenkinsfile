// https://github.com/camunda/jenkins-global-shared-library
// https://github.com/camunda/cambpm-jenkins-shared-library
@Library(['camunda-ci', 'cambpm-jenkins-shared-library@CAM-11822']) _

def failedStageTypes = []

pipeline {
  agent {
    node {
      label 'jenkins-job-runner'
    }
  }
  environment {
    LOGGER_LOG_LEVEL = 'DEBUG'
  }
  options {
    buildDiscarder(logRotator(numToKeepStr: '5'))
    copyArtifactPermission('*')
    throttleJobProperty(
      throttleEnabled: true,
      throttleOption: 'project',
      maxConcurrentTotal: 2
    )
  }
  parameters {
    string name: 'EE_DOWNSTREAM', defaultValue: 'cambpm-ee-main/PR-727', description: 'The name of the EE branch/PR to run the EE pipeline on, e.g. cambpm-ee-main-pr/master'
    string name: 'MAVEN_VERSION', defaultValue: 'maven-3.8-latest', description: 'e.g. maven-3.2-latest'
  }
  stages {
    stage('Maven'){
      parallel {
          



    stage('ASSEMBLY') {
      when {
        expression {
          env.BRANCH_NAME == cambpmDefaultBranch() || (changeRequest() && !pullRequest.labels.contains('ci:no-build'))
        }
      }
      environment {
        NEXUS_SNAPSHOT_REPOSITORY = cambpmConfig.nexusSnapshotRepository()
        NEXUS_SNAPSHOT_REPOSITORY_ID = cambpmConfig.nexusSnapshotRepositoryId()
      }
      steps {
        cambpmConditionalRetry([
          agentLabel: 'h2_perf32',
          suppressErrors: false,
          runSteps: {
            withVault([vaultSecrets: [
                [
                    path        : 'secret/products/cambpm/ci/xlts.dev',
                    secretValues: [
                        [envVar: 'XLTS_REGISTRY', vaultKey: 'registry'],
                        [envVar: 'XLTS_AUTH_TOKEN', vaultKey: 'authToken']]
                ]]]) {
              cambpmRunMaven('.',
                  'clean source:jar deploy source:test-jar com.mycila:license-maven-plugin:check -Pdistro,distro-ce,distro-wildfly,distro-webjar,h2-in-memory -DaltStagingDirectory=${WORKSPACE}/staging -Dmaven.repo.local=\${WORKSPACE}/3.8/.m2 -DskipRemoteStaging=true -DskipTests',
                  withCatch: false,
                  withNpm: true,
                  // we use JDK 11 to build the artifacts, as it is required by the Quarkus extension
                  // the compiler source and target is set to JDK 8 in the release parents
                  jdkVersion: 'jdk-11-latest',
                  mvnVersion: 'maven-3.8-latest')
            }

            // archive all .jar, .pom, .xml, .txt runtime artifacts + required .war/.zip/.tar.gz for EE pipeline
            // add a new line for each group of artifacts
            cambpmArchiveArtifacts('3.8/.m2/org/camunda/**/*-SNAPSHOT/**/*.pom,3.8/.m2/org/camunda/**/*-SNAPSHOT/**/*.xml,3.8/.m2/org/camunda/**/*-SNAPSHOT/**/*.txt',
                               )

            //cambpmStash("platform-stash-runtime",
            //            ".m2/org/camunda/**/*-SNAPSHOT/**",
            //            "**/qa/**,**/*qa*/**,**/*.zip,**/*.tar.gz")
            //cambpmStash("platform-stash-archives",
            //            ".m2/org/camunda/bpm/**/*-SNAPSHOT/**/*.zip,.m2/org/camunda/bpm/**/*-SNAPSHOT/**/*.tar.gz")
            //cambpmStash("platform-stash-qa",
            //          ".m2/org/camunda/bpm/**/qa/**/*-SNAPSHOT/**,.m2/org/camunda/bpm/**/*qa*/**/*-SNAPSHOT/**",
            //          "**/*.zip,**/*.tar.gz")



          }
        ])

      }
          }
      stage('ASSEMBLY-3.2') {
      when {
        expression {
          env.BRANCH_NAME == cambpmDefaultBranch() || (changeRequest() && !pullRequest.labels.contains('ci:no-build'))
        }
      }
      environment {
        NEXUS_SNAPSHOT_REPOSITORY = cambpmConfig.nexusSnapshotRepository()
        NEXUS_SNAPSHOT_REPOSITORY_ID = cambpmConfig.nexusSnapshotRepositoryId()
      }
      steps {
        cambpmConditionalRetry([
          agentLabel: 'h2_perf32',
          suppressErrors: false,
          runSteps: {
            withVault([vaultSecrets: [
                [
                    path        : 'secret/products/cambpm/ci/xlts.dev',
                    secretValues: [
                        [envVar: 'XLTS_REGISTRY', vaultKey: 'registry'],
                        [envVar: 'XLTS_AUTH_TOKEN', vaultKey: 'authToken']]
                ]]]) {
              cambpmRunMaven('.',
                  'clean source:jar deploy source:test-jar com.mycila:license-maven-plugin:check -Pdistro,distro-ce,distro-wildfly,distro-webjar,h2-in-memory -Dmaven.repo.local=\${WORKSPACE}/3.2/.m2 -DaltStagingDirectory=${WORKSPACE}/staging -DskipRemoteStaging=true -DskipTests',
                  withCatch: false,
                  withNpm: true,
                  // we use JDK 11 to build the artifacts, as it is required by the Quarkus extension
                  // the compiler source and target is set to JDK 8 in the release parents
                  jdkVersion: 'jdk-11-latest',
                  mvnVersion: 'maven-3.2-latest')
            }
            
            cambpmArchiveArtifacts('3.2/.m2/org/camunda/**/*-SNAPSHOT/**/*.pom,3.2/.m2/org/camunda/**/*-SNAPSHOT/**/*.xml,3.2/.m2/org/camunda/**/*-SNAPSHOT/**/*.txt',
                      )


          }
        ])

      }
          }}
        }

  }
}
