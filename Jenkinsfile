import java.nio.channels.ClosedChannelException

// https://github.com/camunda/jenkins-global-shared-library
// https://github.com/camunda/cambpm-jenkins-shared-library
@Library(['camunda-ci', 'cambpm-jenkins-shared-library@CAM-12757-stage-retries']) _

def failedStageTypes = []

pipeline {
  agent none
  options {
    buildDiscarder(logRotator(numToKeepStr: '5')) //, artifactNumToKeepStr: '30'
    copyArtifactPermission('*');
  }
  stages {
    stage('custom retries') {
      agent {
        label 'h2'
      }
      steps {
        cambpmRetry(3) {
          script {
            throw new ClosedChannelException()
          }
        }
      }
    }
  }
}
