// https://github.com/camunda/jenkins-global-shared-library
// https://github.com/camunda/cambpm-jenkins-shared-library
@Library(['camunda-ci', 'cambpm-jenkins-shared-library@CAM-14829-debug']) _

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
    string name: 'EE_DOWNSTREAM', defaultValue: 'cambpm-ee-main-pr/' + cambpmDefaultBranch(), description: 'The name of the EE branch/PR to run the EE pipeline on, e.g. cambpm-ee-main/PR-333'
  }
  stages {
    stage('debug CAM-14829') {
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
            script {
              cambpmWithLabels(['postgresql'], 'postgresql')
            }
          }
        ])

      }
    }
  }
}
