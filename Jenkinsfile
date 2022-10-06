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
            cambpmArchiveArtifacts(//'.m2/org/camunda/**/*-SNAPSHOT/**/*.jar,.m2/org/camunda/**/*-SNAPSHOT/**/*.pom,.m2/org/camunda/**/*-SNAPSHOT/**/*.xml,.m2/org/camunda/**/*-SNAPSHOT/**/*.txt',
                                  '3.8/.m2/org/camunda/**/*-SNAPSHOT/**/camunda-webapp*frontend-sources.zip',
                                  '3.8/.m2/org/camunda/**/*-SNAPSHOT/**/license-book*.zip',
                                  '3.8/.m2/org/camunda/**/*-SNAPSHOT/**/camunda-*-assembly*.tar.gz',
                                  '3.8/.m2/org/camunda/**/*-SNAPSHOT/**/camunda-webapp*.war',
                                  '3.8/.m2/org/camunda/**/*-SNAPSHOT/**/',
                                  '3.8/.m2/org/camunda/**/*-SNAPSHOT/**/camunda-example-invoice*.war',
                                  '3.8/.m2/org/camunda/**/*-SNAPSHOT/**/camunda-h2-webapp*.war',
                                  '3.8/.m2/org/camunda/**/*-SNAPSHOT/**/camunda-bpm-run-modules-swaggerui-*-run-swaggerui-license-book-json.json')

            //cambpmStash("platform-stash-runtime",
            //            ".m2/org/camunda/**/*-SNAPSHOT/**",
            //            "**/qa/**,**/*qa*/**,**/*.zip,**/*.tar.gz")
            //cambpmStash("platform-stash-archives",
            //            ".m2/org/camunda/bpm/**/*-SNAPSHOT/**/*.zip,.m2/org/camunda/bpm/**/*-SNAPSHOT/**/*.tar.gz")
            //cambpmStash("platform-stash-qa",
            //          ".m2/org/camunda/bpm/**/qa/**/*-SNAPSHOT/**,.m2/org/camunda/bpm/**/*qa*/**/*-SNAPSHOT/**",
            //          "**/*.zip,**/*.tar.gz")


            script {
              
              //sh 'mkdir 3.8'
              def JAR_OUTPUT = sh(returnStdout: true,
                                  script: "find 3.8/.m2/org -name '*-7.18.0-SNAPSHOT.jar' -print  -exec jar -tf {} \\;").trim()
              echo "list: ${JAR_OUTPUT}"
              writeFile(file: '3.8/jar-list.txt', text: JAR_OUTPUT)

              JAR_OUTPUT = sh(returnStdout: true,
                              script: "find 3.8/.m2/org -name '*-7.18.0-SNAPSHOT.zip' -print  -exec jar -tf {} \\;").trim()
              echo "list: ${JAR_OUTPUT}"
              writeFile(file: '3.8/zip-list.txt', text: JAR_OUTPUT)

              JAR_OUTPUT = sh(returnStdout: true,
                              script: "find 3.8/.m2/org -name '*-7.18.0-SNAPSHOT.war' -print  -exec jar -tf {} \\;").trim()
              echo "list: ${JAR_OUTPUT}"
              writeFile(file: '3.8/war-list.txt', text: JAR_OUTPUT)

              JAR_OUTPUT = sh(returnStdout: true, 
                              script: "find 3.8/.m2/org -name '*-7.18.0-SNAPSHOT.tar.gz' -print  -exec jar -tf {} \\;").trim()
              echo "jar-list: ${JAR_OUTPUT}"
              writeFile(file: '3.8/tar-list.txt', text: JAR_OUTPUT)
              sh "ls"
              sh "ls 3.8"

              cambpmArchiveArtifacts('3.8/**-list.txt')
              cambpmStash("platform-lists-3.8",
                          "**/**-list.txt")
                        
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
            
            cambpmArchiveArtifacts(//'.m2/org/camunda/**/*-SNAPSHOT/**/*.jar,.m2/org/camunda/**/*-SNAPSHOT/**/*.pom,.m2/org/camunda/**/*-SNAPSHOT/**/*.xml,.m2/org/camunda/**/*-SNAPSHOT/**/*.txt',
                      '3.2/.m2/org/camunda/**/*-SNAPSHOT/**/camunda-webapp*frontend-sources.zip',
                      '3.2/.m2/org/camunda/**/*-SNAPSHOT/**/license-book*.zip',
                      '3.2/.m2/org/camunda/**/*-SNAPSHOT/**/camunda-*-assembly*.tar.gz',
                      '3.2/.m2/org/camunda/**/*-SNAPSHOT/**/camunda-webapp*.war',
                      '3.2/.m2/org/camunda/**/*-SNAPSHOT/**/',
                      '3.2/.m2/org/camunda/**/*-SNAPSHOT/**/camunda-example-invoice*.war',
                      '3.2/.m2/org/camunda/**/*-SNAPSHOT/**/camunda-h2-webapp*.war',
                      '3.2/.m2/org/camunda/**/*-SNAPSHOT/**/camunda-bpm-run-modules-swaggerui-*-run-swaggerui-license-book-json.json')

            script {
              //sh 'mkdir 3.2'
              def JAR_OUTPUT = sh(returnStdout: true,
                                  script: "find 3.2/.m2/org -name '*-7.18.0-SNAPSHOT.jar' -print  -exec jar -tf {} \\;").trim()
              echo "list: ${JAR_OUTPUT}"
              writeFile(file: '3.2/jar-list.txt', text: JAR_OUTPUT)

              JAR_OUTPUT = sh(returnStdout: true,
                              script: "find 3.2/.m2/org -name '*-7.18.0-SNAPSHOT.zip' -print  -exec jar -tf {} \\;").trim()
              echo "list: ${JAR_OUTPUT}"
              writeFile(file: '3.2/zip-list.txt', text: JAR_OUTPUT)

              JAR_OUTPUT = sh(returnStdout: true,
                              script: "find 3.2/.m2/org -name '*-7.18.0-SNAPSHOT.war' -print  -exec jar -tf {} \\;").trim()
              echo "list: ${JAR_OUTPUT}"
              writeFile(file: '3.2/war-list.txt', text: JAR_OUTPUT)

              JAR_OUTPUT = sh(returnStdout: true, 
                              script: "find 3.2/.m2/org -name '*-7.18.0-SNAPSHOT.tar.gz' -print  -exec jar -tf {} \\;").trim()
              echo "jar-list: ${JAR_OUTPUT}"
              writeFile(file: '3.2/tar-list.txt', text: JAR_OUTPUT)
              sh 'cd ..'
              sh "ls 3.2"

              cambpmArchiveArtifacts('3.2/**-list.txt')
              cambpmStash("platform-lists-3.2",
                          "**/**-list.txt")

            }
          }
        ])

      }
          }}
        }
                  stage('diff') {
          steps{
             script{
               //sh 'cd \${WORKSPACE}'
               cambpmUnstash("platform-lists-3.2")
               cambpmUnstash("platform-lists-3.8")
               sh 'ls'
               sh 'ls 3.2'
               sh 'ls 3.8'
               //sh 'diff -r ./3.2 ./3.8 >> artifact-diff.patch'
               OUTPUT = sh(returnStdout: false,
                           script: "diff -r ./3.2 ./3.8 ").trim()
               writeFile(file: 'artifact-diff.patch', text: OUTPUT)
               echo "diff: ${OUTPUT}"
              cambpmArchiveArtifacts('artifact-diff.patch')
             }

              
              
          }

          }
  }
}
