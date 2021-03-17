// https://github.com/camunda/jenkins-global-shared-library
// https://github.com/camunda/cambpm-jenkins-shared-library
@Library(['camunda-ci', 'cambpm-jenkins-shared-library']) _

def failedStageTypes = []

pipeline {
  agent {
    node {
      label 'jenkins-job-runner'
    }
  }
  environment {
    CAMBPM_LOGGER_LOG_LEVEL = 'DEBUG'
  }
  options {
    buildDiscarder(logRotator(numToKeepStr: '5'))
    copyArtifactPermission('*')
  }
  parameters {
    string name: 'EE_DOWNSTREAM', defaultValue: 'cambpm-ee-main-pr/master', description: 'The name of the EE branch/PR to run the EE pipeline on, e.g. cambpm-ee-main/PR-333'
  }
  stages {
    stage('ASSEMBLY') {
      when {
        expression {
          env.BRANCH_NAME == cambpmDefaultBranch() || !pullRequest.labels.contains('no-build')
        }
      }
      steps {
        cambpmConditionalRetry([
          agentLabel: 'h2_perf32',
          suppressErrors: false,
          runSteps: {
            script {
              platformVersion = env.JOB_NAME.split('/')[0]
              echo platformVersion
              echo 'my test'
              sh 'printenv'
              eeMainProjectBranch = "cambpm-ee-main-pr/master"

              cambpmTriggerDownstream(
                  platformVersion + "/cambpm-ce/cambpm-daily/${env.BRANCH_NAME}",
                  [string(name: 'UPSTREAM_PROJECT_NAME', value: env.JOB_NAME),
                  string(name: 'UPSTREAM_BUILD_NUMBER', value: env.BUILD_NUMBER)]
                )

              cambpmTriggerDownstream(
                  platformVersion + "/cambpm-ee/" + eeMainProjectBranch,
                  [string(name: 'UPSTREAM_PROJECT_NAME', value: env.JOB_NAME),
                  string(name: 'UPSTREAM_BUILD_NUMBER', value: env.BUILD_NUMBER)],
                  true, true, true
                )


            }
          }
        ])

      }
    }
   

  }
 }
