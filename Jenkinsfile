
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
  stages {
    stage('ASSEMBLY') {
      agent {
        kubernetes {
          yaml getAgent('gcr.io/ci-30-162810/centos:v0.4.6', 16)
        }
      }
      steps {
        withMaven(jdk: 'jdk-8-latest', maven: 'maven-3.2-latest', mavenSettingsConfig: 'maven-nexus-settings') {
          nodejs('nodejs-14.6.0'){
             configFileProvider([configFile(fileId: 'maven-nexus-settings', variable: 'MAVEN_SETTINGS_XML')]) {
               sh """
                 cd internal-dependencies
                 mvn -s \$MAVEN_SETTINGS_XML clean install -Dmaven.repo.local=\$(pwd)/.m2 
               """
             }
          }
    
          archiveArtifacts artifacts: '.m2/org/camunda/**/*-SNAPSHOT/**', excludes: '**/*.zip,**/*.tar.gz', followSymlinks: false
    
          stash name: "platform-stash-runtime", includes: ".m2/org/camunda/**/*-SNAPSHOT/**", excludes: "**/qa/**,**/*qa*/**,**/*.zip,**/*.tar.gz"
          // stash name: "platform-stash-qa", includes: ".m2/org/camunda/bpm/**/qa/**/*-SNAPSHOT/**,.m2/org/camunda/bpm/**/*qa*/**/*-SNAPSHOT/**", excludes: "**/*.zip,**/*.tar.gz"
          //stash name: "platform-stash-distro", includes: ".m2/org/camunda/bpm/**/*-SNAPSHOT/**/*.zip,.m2/org/camunda/bpm/**/*-SNAPSHOT/**/*.tar.gz"
         }
    
        build job: 'cambpm-jenkins-pipelines-ee/pipeline-stash', parameters: [string(name: 'copyArtifactSelector', value: '<TriggeredBuildSelector plugin="copyartifact@1.45.1">  <upstreamFilterStrategy>UseGlobalSetting</upstreamFilterStrategy>  <allowUpstreamDependencies>false</allowUpstreamDependencies></TriggeredBuildSelector>'), booleanParam(name: 'STANDALONE', value: false)], quietPeriod: 10, wait: false
    
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
            withMaven(jdk: 'jdk-8-latest', maven: 'maven-3.2-latest', mavenSettingsConfig: 'maven-nexus-settings') {
              runMaven(true, false,'engine/', ' test -Pdatabase,h2')
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
            withMaven(jdk: 'jdk-8-latest', maven: 'maven-3.2-latest', mavenSettingsConfig: 'maven-nexus-settings') {
              runMaven(true, false,'engine/', 'test -Pdatabase,h2,cfgAuthorizationCheckRevokesAlways')
            }
          }
        }
        stage('engine-rest-UNIT-jersey-2') {
          agent {
            kubernetes {
              yaml getAgent()
            }
          }
          steps{
            withMaven(jdk: 'jdk-8-latest', maven: 'maven-3.2-latest', mavenSettingsConfig: 'maven-nexus-settings') {
              runMaven(true, false,'engine-rest/engine-rest/', 'clean install -Pjersey2')
            }
          }
        }
        stage('engine-rest-UNIT-resteasy3') {
          agent {
            kubernetes {
              yaml getAgent()
            }
          }
          steps{
            withMaven(jdk: 'jdk-8-latest', maven: 'maven-3.2-latest', mavenSettingsConfig: 'maven-nexus-settings') {
              runMaven(true, false,'engine-rest/engine-rest/', 'clean install -Presteasy3')
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
            withMaven(jdk: 'jdk-8-latest', maven: 'maven-3.2-latest', mavenSettingsConfig: 'maven-nexus-settings') {
              runMaven(true, false,'webapps/', 'clean test -Pdatabase,h2 -Dskip.frontend.build=true')
            }
          }
        }
        stage('engine-IT-tomcat-9-h2') {// TODO change it to `postgresql-96`
          agent {
            kubernetes {
              yaml getAgent()
            }
          }
          steps{
            withMaven(jdk: 'jdk-8-latest', maven: 'maven-3.2-latest', mavenSettingsConfig: 'maven-nexus-settings') {
              catchError(buildResult: 'SUCCESS', stageResult: 'FAILURE') {
                runMaven(true, true, 'qa/', 'clean install -Ptomcat,h2,engine-integration')
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
          agent {
            kubernetes {
              yaml getAgent('gcr.io/ci-30-162810/chrome:78v0.1.2')
            }
          }
          steps{
            withMaven(jdk: 'jdk-8-latest', maven: 'maven-3.2-latest', mavenSettingsConfig: 'maven-nexus-settings') {
              catchError(buildResult: 'SUCCESS', stageResult: 'FAILURE') {
                runMaven(true, true,'qa/', 'clean install -Ptomcat,h2,webapps-integration')
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
          agent {
            kubernetes {
              yaml getAgent('gcr.io/ci-30-162810/chrome:78v0.1.2')
            }
          }
          steps{
            withMaven(jdk: 'jdk-8-latest', maven: 'maven-3.2-latest', mavenSettingsConfig: 'maven-nexus-settings') {
              catchError(buildResult: 'SUCCESS', stageResult: 'FAILURE') {
                runMaven(true, true,'qa/', 'clean install -Pwildfly-vanilla,webapps-integration-sa')
              }
            }
          }
        }
        stage('camunda-run-IT') {
          agent {
            kubernetes {
              yaml getAgent('gcr.io/ci-30-162810/chrome:78v0.1.2', 16)
            }
          }
          steps{
            withMaven(jdk: 'jdk-8-latest', maven: 'maven-3.2-latest', mavenSettingsConfig: 'maven-nexus-settings') {
              catchError(buildResult: 'SUCCESS', stageResult: 'FAILURE') {
                runMaven(true, true,'distro/run/', 'clean install -Pintegration-test-camunda-run')
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
          agent {
            kubernetes {
              yaml getAgent('gcr.io/ci-30-162810/chrome:78v0.1.2', 16)
            }
          }
          steps{
            withMaven(jdk: 'jdk-8-latest', maven: 'maven-3.2-latest', mavenSettingsConfig: 'maven-nexus-settings') {
              catchError(buildResult: 'SUCCESS', stageResult: 'FAILURE') {
                runMaven(true, true,'spring-boot-starter/', 'clean install -Pintegration-test-spring-boot-starter')
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
    stage('db tests + CE webapps IT + EE platform') {
      parallel {
        stage('engine-api-compatibility') {
          agent {
            kubernetes {
              yaml getAgent('gcr.io/ci-30-162810/centos:v0.4.6', 16)
            }
          }
          steps{
            withMaven(jdk: 'jdk-8-latest', maven: 'maven-3.2-latest', mavenSettingsConfig: 'maven-nexus-settings') {
              runMaven(true, false,'engine/', 'clean verify -Pcheck-api-compatibility')
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
            withMaven(jdk: 'jdk-8-latest', maven: 'maven-3.2-latest', mavenSettingsConfig: 'maven-nexus-settings') {
              runMaven(true, false,'engine/', 'clean test -Pcheck-plugins')
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
            withMaven(jdk: 'jdk-8-latest', maven: 'maven-3.2-latest', mavenSettingsConfig: 'maven-nexus-settings') {
              nodejs('nodejs-14.6.0'){
                runMaven(true, false,'webapps/', 'clean test -Pdb-table-prefix')
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

void runMaven(boolean runtimeStash, boolean distroStash, String directory, String cmd) {
  if (runtimeStash) unstash "platform-stash-runtime"
  //if (distroStash) unstash "platform-stash-distro"
  sh 'export MAVEN_OPTS="-Dmaven.repo.local=\${WORKSPACE}/.m2"'
  configFileProvider([configFile(fileId: 'maven-nexus-settings', variable: 'MAVEN_SETTINGS_XML')]) {
    sh(" cd ${directory} && mvn -s \$MAVEN_SETTINGS_XML ${cmd} -nsu -B -X")
  }
}

void withLabels(String... labels) {
  for ( l in labels) {
    pullRequest.labels.contains(labelName)
  }
}
