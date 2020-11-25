
// https://github.com/camunda/jenkins-global-shared-library
@Library('camunda-ci') _

String getAgent(String dockerImage = 'gcr.io/ci-30-162810/centos:v0.4.6', Integer cpuLimit = 4){
  String mavenForkCount = cpuLimit;
  String mavenMemoryLimit = cpuLimit * 2;
  """
metadata:
  labels:
    agent: ci-cambpm-camunda-cloud-build
spec:
  nodeSelector:
    cloud.google.com/gke-nodepool: agents-n1-standard-32-netssd-preempt
  tolerations:
  - key: "agents-n1-standard-32-netssd-preempt"
    operator: "Exists"
    effect: "NoSchedule"
  containers:
  - name: "jnlp"
    image: "${dockerImage}"
    args: ['\$(JENKINS_SECRET)', '\$(JENKINS_NAME)']
    tty: true
    env:
    - name: LIMITS_CPU
      value: ${mavenForkCount}
    - name: TZ
      value: Europe/Berlin
    resources:
      limits:
        cpu: ${cpuLimit}
        memory: ${mavenMemoryLimit}Gi
      requests:
        cpu: ${cpuLimit}
        memory: ${mavenMemoryLimit}Gi
    workingDir: "/home/work"
    volumeMounts:
      - mountPath: /home/work
        name: workspace-volume
  """
}

pipeline {
  agent none
  options {
    buildDiscarder(logRotator(numToKeepStr: '5')) //, artifactNumToKeepStr: '30'
    copyArtifactPermission('*');
  }
  parameters {
      string defaultValue: 'pipeline-master', description: 'The name of the EE branch to run the EE pipeline on', name: 'EE_BRANCH_NAME'
  }
  stages {
    stage('ASSEMBLY') {
      agent {
        kubernetes {
          yaml getAgent('gcr.io/ci-30-162810/centos:v0.4.6', 16)
        }
      }
      steps {

        withMaven(jdk: 'jdk-8-latest', maven: 'maven-3.2-latest', mavenSettingsConfig: 'camunda-maven-settings', options: [artifactsPublisher(disabled: true), junitPublisher(disabled: true)]) {
          nodejs('nodejs-14.6.0'){
             configFileProvider([configFile(fileId: 'maven-nexus-settings', variable: 'MAVEN_SETTINGS_XML')]) {
               sh """
                 mvn -s \$MAVEN_SETTINGS_XML clean install source:jar -Pdistro,distro-ce,distro-wildfly,distro-webjar -DskipTests -Dmaven.repo.local=\${WORKSPACE}/.m2 com.mycila:license-maven-plugin:check -B
               """
             }
          }
        
          // archive all .jar, .pom, .xml, .txt runtime artifacts + required .war/.zip/.tar.gz for EE pipeline
          // add a new line for each group of artifacts
          archiveArtifacts artifacts: '.m2/org/camunda/**/*-SNAPSHOT/**/*.jar,.m2/org/camunda/**/*-SNAPSHOT/**/*.pom,.m2/org/camunda/**/*-SNAPSHOT/**/*.xml,.m2/org/camunda/**/*-SNAPSHOT/**/*.txt', followSymlinks: false
          archiveArtifacts artifacts: '.m2/org/camunda/**/*-SNAPSHOT/**/camunda-webapp*frontend-sources.zip', followSymlinks: false
          archiveArtifacts artifacts: '.m2/org/camunda/**/*-SNAPSHOT/**/license-book*.zip', followSymlinks: false
          archiveArtifacts artifacts: '.m2/org/camunda/**/*-SNAPSHOT/**/camunda-jboss-modules*.zip', followSymlinks: false
          archiveArtifacts artifacts: '.m2/org/camunda/**/*-SNAPSHOT/**/camunda-*-assembly*.tar.gz', followSymlinks: false
          archiveArtifacts artifacts: '.m2/org/camunda/**/*-SNAPSHOT/**/camunda-webapp*.war', followSymlinks: false
          archiveArtifacts artifacts: '.m2/org/camunda/**/*-SNAPSHOT/**/camunda-engine-rest*.war', followSymlinks: false
          archiveArtifacts artifacts: '.m2/org/camunda/**/*-SNAPSHOT/**/camunda-example-invoice*.war', followSymlinks: false
          archiveArtifacts artifacts: '.m2/org/camunda/**/*-SNAPSHOT/**/camunda-h2-webapp*.war', followSymlinks: false

          stash name: "platform-stash-runtime", includes: ".m2/org/camunda/**/*-SNAPSHOT/**", excludes: "**/qa/**,**/*qa*/**,**/*.zip,**/*.tar.gz"
          stash name: "platform-stash-archives", includes: ".m2/org/camunda/bpm/**/*-SNAPSHOT/**/*.zip,.m2/org/camunda/bpm/**/*-SNAPSHOT/**/*.tar.gz"
          stash name: "platform-stash-qa", includes: ".m2/org/camunda/bpm/**/qa/**/*-SNAPSHOT/**,.m2/org/camunda/bpm/**/*qa*/**/*-SNAPSHOT/**", excludes: "**/*.zip,**/*.tar.gz"
        }

        build job: "cambpm-jenkins-pipelines-ee/${env.EE_BRANCH_NAME}", parameters: [
            string(name: 'copyArtifactSelector', value: '<TriggeredBuildSelector plugin="copyartifact@1.45.1">  <upstreamFilterStrategy>UseGlobalSetting</upstreamFilterStrategy>  <allowUpstreamDependencies>false</allowUpstreamDependencies></TriggeredBuildSelector>'),
            booleanParam(name: 'STANDALONE', value: false),
            string(name: 'CE_BRANCH_NAME', value: "${BRANCH_NAME}")
        ], quietPeriod: 10, wait: false
        build job: "cambpm-jenkins-pipelines-daily/${env.BRANCH_NAME}", parameters: [
            string(name: 'copyArtifactSelector', value: '<TriggeredBuildSelector plugin="copyartifact@1.45.1">  <upstreamFilterStrategy>UseGlobalSetting</upstreamFilterStrategy>  <allowUpstreamDependencies>false</allowUpstreamDependencies></TriggeredBuildSelector>'),
            booleanParam(name: 'STANDALONE', value: false)
        ], quietPeriod: 10, wait: false
      }
    }
    stage('h2 tests') {
      parallel {
        stage('engine-UNIT-h2') {
          when {
            anyOf {
              branch 'pipeline-master';
              allOf {
                changeRequest();
                expression {
                  withLabels('h2')
                }
              }
            }
          }
          agent {
            kubernetes {
              yaml getAgent('gcr.io/ci-30-162810/centos:v0.4.6', 16)
            }
          }
          steps{
            withMaven(jdk: 'jdk-8-latest', maven: 'maven-3.2-latest', mavenSettingsConfig: 'camunda-maven-settings', options: [artifactsPublisher(disabled: true), junitPublisher(disabled: true)]) {
              runMaven(true, false, false, 'engine/', ' test -Pdatabase,h2')
            }
          }
        }
        stage('engine-UNIT-authorizations-h2') {
          when {
            anyOf {
              branch 'pipeline-master';
              allOf {
                changeRequest();
                expression {
                  withLabels('h2')
                }
              }
            }
          }
          agent {
            kubernetes {
              yaml getAgent('gcr.io/ci-30-162810/centos:v0.4.6', 16)
            }
          }
          steps{
            withMaven(jdk: 'jdk-8-latest', maven: 'maven-3.2-latest', mavenSettingsConfig: 'camunda-maven-settings', options: [artifactsPublisher(disabled: true), junitPublisher(disabled: true)]) {
              runMaven(true, false, false, 'engine/', 'test -Pdatabase,h2,cfgAuthorizationCheckRevokesAlways')
            }
          }
        }
        stage('engine-rest-UNIT-jersey-2') {
          when {
            anyOf {
              branch 'pipeline-master';
              allOf {
                changeRequest();
                expression {
                  withLabels('rest')
                }
              }
            }
          }
          agent {
            kubernetes {
              yaml getAgent()
            }
          }
          steps{
            withMaven(jdk: 'jdk-8-latest', maven: 'maven-3.2-latest', mavenSettingsConfig: 'camunda-maven-settings', options: [artifactsPublisher(disabled: true), junitPublisher(disabled: true)]) {
              runMaven(true, false, false, 'engine-rest/engine-rest/', 'clean install -Pjersey2')
            }
          }
        }
        stage('engine-rest-UNIT-resteasy3') {
          when {
            anyOf {
              branch 'pipeline-master';
              allOf {
                changeRequest();
                expression {
                  withLabels('rest')
                }
              }
            }
          }
          agent {
            kubernetes {
              yaml getAgent()
            }
          }
          steps{
            withMaven(jdk: 'jdk-8-latest', maven: 'maven-3.2-latest', mavenSettingsConfig: 'camunda-maven-settings', options: [artifactsPublisher(disabled: true), junitPublisher(disabled: true)]) {
              runMaven(true, false, false, 'engine-rest/engine-rest/', 'clean install -Presteasy3')
            }
          }
        }
        stage('webapp-UNIT-h2') {
          when {
            anyOf {
              branch 'pipeline-master';
              allOf {
                changeRequest();
                expression {
                  withLabels('webapps')
                }
              }
            }
          }
          agent {
            kubernetes {
              yaml getAgent()
            }
          }
          steps{
            withMaven(jdk: 'jdk-8-latest', maven: 'maven-3.2-latest', mavenSettingsConfig: 'camunda-maven-settings', options: [artifactsPublisher(disabled: true), junitPublisher(disabled: true)]) {
              runMaven(true, false, false, 'webapps/', 'clean test -Pdatabase,h2 -Dskip.frontend.build=true')
            }
          }
        }
        stage('engine-IT-tomcat-9-h2') {// TODO change it to `postgresql-96`
          when {
            anyOf {
              branch 'pipeline-master';
              allOf {
                changeRequest();
                expression {
                  withLabels('IT')
                }
              }
            }
          }
          agent {
            kubernetes {
              yaml getAgent()
            }
          }
          steps{
            withMaven(jdk: 'jdk-8-latest', maven: 'maven-3.2-latest', mavenSettingsConfig: 'camunda-maven-settings', options: [artifactsPublisher(disabled: true), junitPublisher(disabled: true)]) {
              catchError(buildResult: 'SUCCESS', stageResult: 'FAILURE') {
                runMaven(true, true, false, 'qa/', 'clean install -Ptomcat,h2,engine-integration')
              }
            }
          }
          post {
            always {
              junit testResults: '**/target/*-reports/TEST-*.xml', keepLongStdio: true
            }
          }
        }
        stage('webapp-IT-tomcat-9-h2') {
          when {
            anyOf {
              branch 'pipeline-master';
              allOf {
                changeRequest();
                expression {
                  withLabels('webapps', 'IT')
                }
              }
            }
          }
          agent {
            kubernetes {
              yaml getAgent('gcr.io/ci-30-162810/chrome:78v0.1.2')
            }
          }
          steps{
            withMaven(jdk: 'jdk-8-latest', maven: 'maven-3.2-latest', mavenSettingsConfig: 'camunda-maven-settings', options: [artifactsPublisher(disabled: true), junitPublisher(disabled: true)]) {
              catchError(buildResult: 'SUCCESS', stageResult: 'FAILURE') {
                runMaven(true, true, false, 'qa/', 'clean install -Ptomcat,h2,webapps-integration')
              }
            }
          }
          post {
            always {
              junit testResults: '**/target/*-reports/TEST-*.xml', keepLongStdio: true
            }
          }
        }
        stage('webapp-IT-standalone-wildfly') {
          when {
            anyOf {
              branch 'pipeline-master';
              allOf {
                changeRequest();
                expression {
                  withLabels('webapps', 'IT')
                }
              }
            }
          }
          agent {
            kubernetes {
              yaml getAgent('gcr.io/ci-30-162810/chrome:78v0.1.2')
            }
          }
          steps{
            withMaven(jdk: 'jdk-8-latest', maven: 'maven-3.2-latest', mavenSettingsConfig: 'camunda-maven-settings', options: [artifactsPublisher(disabled: true), junitPublisher(disabled: true)]) {
              catchError(buildResult: 'SUCCESS', stageResult: 'FAILURE') {
                runMaven(true, true, false, 'qa/', 'clean install -Pwildfly-vanilla,webapps-integration-sa')
              }
            }
          }
        }
        stage('camunda-run-IT') {
          when {
            anyOf {
              branch 'pipeline-master';
              allOf {
                changeRequest();
                expression {
                  withLabels('IT', 'run', 'spring-boot')
                }
              }
            }
          }
          agent {
            kubernetes {
              yaml getAgent('gcr.io/ci-30-162810/chrome:78v0.1.2', 16)
            }
          }
          steps{
            withMaven(jdk: 'jdk-8-latest', maven: 'maven-3.2-latest', mavenSettingsConfig: 'camunda-maven-settings', options: [artifactsPublisher(disabled: true), junitPublisher(disabled: true)]) {
              catchError(buildResult: 'SUCCESS', stageResult: 'FAILURE') {
                runMaven(true, true, true, 'distro/run/', 'clean install -Pintegration-test-camunda-run')
              }
            }
          }
          post {
            always {
              junit testResults: '**/target/*-reports/TEST-*.xml', keepLongStdio: true
            }
          }
        }
        stage('spring-boot-starter-IT') {
          when {
            anyOf {
              branch 'pipeline-master';
              allOf {
                changeRequest();
                expression {
                  withLabels('IT', 'spring-boot')
                }
              }
            }
          }
          agent {
            kubernetes {
              yaml getAgent('gcr.io/ci-30-162810/chrome:78v0.1.2', 16)
            }
          }
          steps{
            withMaven(jdk: 'jdk-8-latest', maven: 'maven-3.2-latest', mavenSettingsConfig: 'camunda-maven-settings', options: [artifactsPublisher(disabled: true), junitPublisher(disabled: true)]) {
              catchError(buildResult: 'SUCCESS', stageResult: 'FAILURE') {
                runMaven(true, true, true, 'spring-boot-starter/', 'clean install -Pintegration-test-spring-boot-starter')
              }
            }
          }
          post {
            always {
              junit testResults: '**/target/*-reports/TEST-*.xml', keepLongStdio: true
            }
          }
        }
      }
    }
    stage('db tests + CE webapps IT') {
      parallel {
        stage('engine-api-compatibility') {
          agent {
            kubernetes {
              yaml getAgent('gcr.io/ci-30-162810/centos:v0.4.6', 16)
            }
          }
          steps{
            withMaven(jdk: 'jdk-8-latest', maven: 'maven-3.2-latest', mavenSettingsConfig: 'camunda-maven-settings', options: [artifactsPublisher(disabled: true), junitPublisher(disabled: true)]) {
              runMaven(true, false, false, 'engine/', 'clean verify -Pcheck-api-compatibility')
            }
          }
        }
        stage('engine-UNIT-plugins') {
          agent {
            kubernetes {
              yaml getAgent('gcr.io/ci-30-162810/centos:v0.4.6', 16)
            }
          }
          steps{
            withMaven(jdk: 'jdk-8-latest', maven: 'maven-3.2-latest', mavenSettingsConfig: 'camunda-maven-settings', options: [artifactsPublisher(disabled: true), junitPublisher(disabled: true)]) {
              runMaven(true, false, false, 'engine/', 'clean test -Pcheck-plugins')
            }
          }
        }
        stage('webapp-UNIT-database-table-prefix') {
          agent {
            kubernetes {
              yaml getAgent()
            }
          }
          steps{
            withMaven(jdk: 'jdk-8-latest', maven: 'maven-3.2-latest', mavenSettingsConfig: 'camunda-maven-settings', options: [artifactsPublisher(disabled: true), junitPublisher(disabled: true)]) {
              nodejs('nodejs-14.6.0'){
                runMaven(true, false, false, 'webapps/', 'clean test -Pdb-table-prefix')
              }
            }
          }
        }
      }
    }
  }
  post {
    changed {
      script {
        if (!agentDisconnected()){ 
          // send email if the slave disconnected
        }
      }
    }
    always {
      script {
        if (agentDisconnected()) {// Retrigger the build if the slave disconnected
          //currentBuild.result = 'ABORTED'
          //currentBuild.description = "Aborted due to connection error"
          build job: currentBuild.projectName, propagate: false, quietPeriod: 60, wait: false
        }
      }
    }
  }
}

void runMaven(boolean runtimeStash, boolean archivesStash, boolean qaStash, String directory, String cmd, boolean singleThreaded = false) {
  if (runtimeStash) unstash "platform-stash-runtime"
  if (archivesStash) unstash "platform-stash-archives"
  if (qaStash) unstash "platform-stash-qa"
  String forkCount = singleThreaded? "-DforkCount=1" : '';
  configFileProvider([configFile(fileId: 'maven-nexus-settings', variable: 'MAVEN_SETTINGS_XML')]) {
    sh("mvn -s \$MAVEN_SETTINGS_XML ${forkCount} ${cmd} -nsu -Dmaven.repo.local=\${WORKSPACE}/.m2 -B -f ${directory}/pom.xml")
  }
}

void withLabels(String... labels) {
  for ( l in labels) {
    pullRequest.labels.contains(labelName)
  }
}
