// https://github.com/camunda/jenkins-global-shared-library
// https://github.com/camunda/cambpm-jenkins-shared-library
@Library(['camunda-ci', 'cambpm-jenkins-shared-library@retry-stage']) _

def failedStageTypes = []

pipeline {
  agent none
  options {
    buildDiscarder(logRotator(numToKeepStr: '5')) //, artifactNumToKeepStr: '30'
    copyArtifactPermission('*');
  }
  stages {
    stage('UNIT DB tests') {
      steps {
        script {
          parallel(cambpmGetMatrixStages('main-test', failedStageTypes, { return true }))
        }
      }
    }
  }
  post {
    always {
      script {
        if (agentDisconnected()) {// Retrigger the build if the slave disconnected
          echo 'AGENT WAS DISCONNECTED!'
          build job: currentBuild.projectName, propagate: false, quietPeriod: 60, wait: false
        }
      }
    }
  }
}
