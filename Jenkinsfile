#!/usr/bin/env groovy

// https://github.com/camunda/jenkins-global-shared-library
@Library('camunda-ci') _

String getAgent(Integer cpuLimit = 4){
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
    image: "gcr.io/ci-30-162810/centos:v0.4.6"
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
  stages {
    stage('ASSEMBLY') {
      agent {
        kubernetes {
          yaml getAgent()
        }
      }
      steps {
          withMaven(jdk: 'jdk-8-latest', maven: 'maven-3.2-latest', mavenSettingsConfig: 'maven-nexus-settings') {
            sh '''
              mvn --version
              java -version
            '''
            nodejs('nodejs-14.6.0'){
              sh '''
                npm version
              '''
            }
            configFileProvider([configFile(fileId: 'maven-nexus-settings', variable: 'MAVEN_SETTINGS_XML')]) {
              sh """
                mvn -s \$MAVEN_SETTINGS_XML -T\$LIMITS_CPU clean install source:jar -Pdistro,distro-ce,distro-wildfly,distro-webjar -DskipTests -Dmaven.repo.local=\$(pwd)/.m2 com.mycila:license-maven-plugin:check -B
              """
            }
            stash name: "platform-stash-runtime", includes: ".m2/org/camunda/**/*-SNAPSHOT/**", excludes: "**/qa/**,**/*qa*/**,**/*.zip,**/*.tar.gz"
            stash name: "platform-stash-qa", includes: ".m2/org/camunda/bpm/**/qa/**/*-SNAPSHOT/**,.m2/org/camunda/bpm/**/*qa*/**/*-SNAPSHOT/**", excludes: "**/*.zip,**/*.tar.gz"
            stash name: "platform-stash-distro", includes: ".m2/org/camunda/bpm/**/*-SNAPSHOT/**/*.zip,.m2/org/camunda/bpm/**/*-SNAPSHOT/**/*.tar.gz"
          }
      }
    }
    stage('h2 tests') {
      parallel {
        stage('engine-UNIT-h2') {
          agent {
            kubernetes {
              yaml getAgent(16)
            }
          }
          steps{
            withMaven(jdk: 'jdk-8-latest', maven: 'maven-3.2-latest') {
              unstash "platform-stash-runtime"
              configFileProvider([configFile(fileId: 'maven-nexus-settings', variable: 'MAVEN_SETTINGS_XML')]) {
                sh """
                  export MAVEN_OPTS="-Dmaven.repo.local=\$(pwd)/.m2"
                  cd engine && mvn -s \$MAVEN_SETTINGS_XML -B -T\$LIMITS_CPU test -Pdatabase,h2
                """
              }
            }
          }
        }
        stage('engine-UNIT-authorizations-h2') {
          agent {
            kubernetes {
              yaml getAgent(16)
            }
          }
          steps{
            withMaven(jdk: 'jdk-8-latest', maven: 'maven-3.2-latest') {
              unstash "platform-stash-runtime"
              configFileProvider([configFile(fileId: 'maven-nexus-settings', variable: 'MAVEN_SETTINGS_XML')]) {
                sh """
                  export MAVEN_OPTS="-Dmaven.repo.local=\$(pwd)/.m2"
                  cd engine && mvn -s \$MAVEN_SETTINGS_XML -B -T\$LIMITS_CPU test -Pdatabase,h2,cfgAuthorizationCheckRevokesAlways
                """
              }
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
            withMaven(jdk: 'jdk-8-latest', maven: 'maven-3.2-latest') {
              unstash "platform-stash-runtime"
              configFileProvider([configFile(fileId: 'maven-nexus-settings', variable: 'MAVEN_SETTINGS_XML')]) {
                sh """
                  export MAVEN_OPTS="-Dmaven.repo.local=\$(pwd)/.m2"
                  cd engine-rest/engine-rest/ && mvn -s \$MAVEN_SETTINGS_XML clean install -Pjersey2 -B
                """
              }
            }
          }
        }
        stage('webapp-UNIT-h2') {
          agent {
            kubernetes {
              yaml getAgent()
            }
          }
          steps{
            withMaven(jdk: 'jdk-8-latest', maven: 'maven-3.2-latest') {
              unstash "platform-stash-runtime"
              configFileProvider([configFile(fileId: 'maven-nexus-settings', variable: 'MAVEN_SETTINGS_XML')]) {
                sh """
                  export MAVEN_OPTS="-Dmaven.repo.local=\$(pwd)/.m2"
                  cd webapps/ && mvn -s \$MAVEN_SETTINGS_XML clean test -Pdatabase,h2 -Dskip.frontend.build=true -B
                """
              }
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
            withMaven(jdk: 'jdk-8-latest', maven: 'maven-3.2-latest') {
              unstash "platform-stash-runtime"
              unstash "platform-stash-distro"
              configFileProvider([configFile(fileId: 'maven-nexus-settings', variable: 'MAVEN_SETTINGS_XML')]) {
                sh """
                  export MAVEN_OPTS="-Dmaven.repo.local=\$(pwd)/.m2"
                  cd qa/ && mvn -s \$MAVEN_SETTINGS_XML clean install -Ptomcat,h2,engine-integration -B
                """
              }
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
              yaml getAgent()
            }
          }
          steps{
            withMaven(jdk: 'jdk-8-latest', maven: 'maven-3.2-latest') {
              unstash "platform-stash-runtime"
              configFileProvider([configFile(fileId: 'maven-nexus-settings', variable: 'MAVEN_SETTINGS_XML')]) {
                sh """
                  export MAVEN_OPTS="-Dmaven.repo.local=\$(pwd)/.m2"
                  cd engine && mvn -s \$MAVEN_SETTINGS_XML clean verify -Pcheck-api-compatibility -B
                """
              }
            }
          }
        }
        stage('engine-UNIT-plugins') {
          agent {
            kubernetes {
              yaml getAgent()
            }
          }
          steps{
            withMaven(jdk: 'jdk-8-latest', maven: 'maven-3.2-latest') {
              unstash "platform-stash-runtime"
              configFileProvider([configFile(fileId: 'maven-nexus-settings', variable: 'MAVEN_SETTINGS_XML')]) {
                sh """
                  export MAVEN_OPTS="-Dmaven.repo.local=\$(pwd)/.m2"
                  cd engine && mvn -s \$MAVEN_SETTINGS_XML clean verify -Pcheck-api-compatibility -B
                """
              }
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
            withMaven(jdk: 'jdk-8-latest', maven: 'maven-3.2-latest') {
              unstash "platform-stash-runtime"
              configFileProvider([configFile(fileId: 'maven-nexus-settings', variable: 'MAVEN_SETTINGS_XML')]) {
                sh """
                  export MAVEN_OPTS="-Dmaven.repo.local=\$(pwd)/.m2"
                   cd webapps/ && mvn -s \$MAVEN_SETTINGS_XML clean test -Pdb-table-prefix -Dskip.frontend.build=true -B
                """
              }
            }
          }
        }
        stage('EE-platform-DISTRO-dummy') {
          agent {
            kubernetes {
              yaml getAgent()
            }
          }
          steps{
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
          build job: currentBuild.projectName, propagate: false, quietPeriod: 60, wait: false
        }
      }
    }
  }
} 