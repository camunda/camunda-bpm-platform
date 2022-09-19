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
                  'clean source:jar deploy source:test-jar com.mycila:license-maven-plugin:check -Pdistro,distro-ce,distro-wildfly,distro-webjar,h2-in-memory -DaltStagingDirectory=${WORKSPACE}/staging -DskipRemoteStaging=true -DskipTests',
                  withCatch: false,
                  withNpm: true,
                  // we use JDK 11 to build the artifacts, as it is required by the Quarkus extension
                  // the compiler source and target is set to JDK 8 in the release parents
                  jdkVersion: 'jdk-11-latest',
                  mvnVersion: params.MAVEN_VERSION)
            }

            // archive all .jar, .pom, .xml, .txt runtime artifacts + required .war/.zip/.tar.gz for EE pipeline
            // add a new line for each group of artifacts
           // cambpmArchiveArtifacts('.m2/org/camunda/**/*-SNAPSHOT/**/*.jar,.m2/org/camunda/**/*-SNAPSHOT/**/*.pom,.m2/org/camunda/**/*-SNAPSHOT/**/*.xml,.m2/org/camunda/**/*-SNAPSHOT/**/*.txt',
           //                       '.m2/org/camunda/**/*-SNAPSHOT/**/camunda-webapp*frontend-sources.zip',
           //                       '.m2/org/camunda/**/*-SNAPSHOT/**/license-book*.zip',
           //                       '.m2/org/camunda/**/*-SNAPSHOT/**/camunda-*-assembly*.tar.gz',
           //                       '.m2/org/camunda/**/*-SNAPSHOT/**/camunda-webapp*.war',
           //                       '.m2/org/camunda/**/*-SNAPSHOT/**/',
           //                       '.m2/org/camunda/**/*-SNAPSHOT/**/camunda-example-invoice*.war',
           //                       '.m2/org/camunda/**/*-SNAPSHOT/**/camunda-h2-webapp*.war',
           //                       '.m2/org/camunda/**/*-SNAPSHOT/**/camunda-bpm-run-modules-swaggerui-*-run-swaggerui-license-book-json.json')

            //cambpmStash("platform-stash-runtime",
            //            ".m2/org/camunda/**/*-SNAPSHOT/**",
            //            "**/qa/**,**/*qa*/**,**/*.zip,**/*.tar.gz")
            //cambpmStash("platform-stash-archives",
            //            ".m2/org/camunda/bpm/**/*-SNAPSHOT/**/*.zip,.m2/org/camunda/bpm/**/*-SNAPSHOT/**/*.tar.gz")
            //cambpmStash("platform-stash-qa",
            //          ".m2/org/camunda/bpm/**/qa/**/*-SNAPSHOT/**,.m2/org/camunda/bpm/**/*qa*/**/*-SNAPSHOT/**",
            //          "**/*.zip,**/*.tar.gz")


            script {
              sh 'cd .m2/org/camunda'
              sh 'find . -name "*-7.18.0-SNAPSHOT.jar" -print  -exec jar -tf {} /; > ./jar-list.txt'
            //  sh 'find . -name "camunda-webapp*frontend-sources.zip" -print  -exec jar -tvf {} /; > ./frontend-sources-list.txt'
              //sh 'find . -name "camunda-*-assembly*.tar.gz" -print  -exec jar -tvf {} /; > ./tar-list.txt'
              //TODO
              sh 'find . -name "camunda-webapp*.war" -print  -exec jar -tf {} /; > ./camunda-webapp-war-list.txt'
              sh 'find . -name "camunda-engine-rest*.war" -print  -exec jar -tf {} /; > ./camunda-engine-rest-war-list.txt'
              sh 'find . -name "camunda-engine-rest*.war" -print  -exec jar -tf {} /; > ./camunda-engine-rest-war-list.txt'
              
              
              cambpmStash("platform-lists",
                        ".m2/org/camunda/**/*-SNAPSHOT/**",
                        "**-list.txt")
                        
              // JOB_NAME, e.g.: '7.15/cambpm-ce/cambpm-main/PR-1373'
              // keep leading slash for the absolute project path
            //  platformVersionDir = "/" + env.JOB_NAME.split('/')[0]
            //  upstreamProjectName = "/" + env.JOB_NAME
            //  upstreamBuildNumber = env.BUILD_NUMBER

           //  if (cambpmWithLabels('webapp-integration', 'all-as', 'h2', 'websphere', 'weblogic', 'jbosseap', 'run', 'spring-boot', 'authorizations', 'e2e')) {
           //    cambpmTriggerDownstream(
           //      platformVersionDir + "/cambpm-ee/" + eeMainProjectBranch,
           //      [string(name: 'UPSTREAM_PROJECT_NAME', value: upstreamProjectName),
           //      string(name: 'UPSTREAM_BUILD_NUMBER', value: upstreamBuildNumber)],
           //      true, true, true
           //    )
           //  }


            }
          }
        ])

      }
    }
  }
}
