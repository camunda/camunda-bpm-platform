#!/usr/bin/env groovy

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
  stages {
        stage('engine-rest-UNIT-resteasy3') {
          agent {
            kubernetes {
              yaml getAgent()
            }
          }
          steps{
            withMaven(jdk: 'jdk-8-latest', maven: 'maven-3.2-latest') {
              runMaven(true, false,'engine-rest/engine-rest/', 'clean install -Presteasy3')
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
  //if (runtimeStash) unstash "platform-stash-runtime"
  //if (distroStash) unstash "platform-stash-distro"
  //configFileProvider([configFile(fileId: 'maven-nexus-settings', variable: 'MAVEN_SETTINGS_XML')]) {
    sh 'export MAVEN_OPTS="-Dmaven.repo.local=\${WORKSPACE}/.m2"'
    sh("cd ${directory} && mvn -s \${WORKSPACE}/settings.xml ${cmd} -nsu -B  -X")
  //}
}

void withLabels(String... labels) {
  for ( l in labels) {
    pullRequest.labels.contains(labelName)
  }
}