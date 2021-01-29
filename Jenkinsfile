// https://github.com/camunda/jenkins-global-shared-library
// https://github.com/camunda/cambpm-jenkins-shared-library
@Library(['camunda-ci', 'cambpm-jenkins-shared-library@CAM-13050']) _

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
    string defaultValue: 'cambpm-ee-main-pr/master', description: 'The name of the EE branch/PR to run the EE pipeline on, e.g. cambpm-ee-main/PR-333', name: 'EE_DOWNSTREAM'
  }
  stages {
    stage('UNIT DB tests') {
      steps {
        script {
          parallel(cambpmGetMatrixStages('engine-webapp-unit', failedStageTypes, { allowedStageLabels, dbLabel ->
            return cambpmWithLabels(allowedStageLabels.minus('cockroachdb'), cambpmGetDbType(dbLabel))
          }))
        }
      }
    }
    stage('MISC tests') {
      parallel {
        stage('engine-api-compatibility') {
          when {
            allOf {
              expression {
                cambpmIsNotFailedStageType(failedStageTypes, 'engine-unit')
              }
              branch cambpmDefaultBranch();
            }
              }
          steps {
            cambpmConditionalRetry([
              agentLabel: 'h2',
              runSteps: {
                cambpmRunMaven('engine/', 'clean verify -Pcheck-api-compatibility', runtimeStash: true)
              },
              postAlways: {
                cambpmPublishTestResult()
              }
            ])
          }
        }
        stage('engine-UNIT-plugins') {
          when {
            allOf {
              expression {
                cambpmIsNotFailedStageType(failedStageTypes, 'engine-unit')
              }
              branch cambpmDefaultBranch();
            }
              }
          steps {
            cambpmConditionalRetry([
              agentLabel: 'h2',
              runSteps: {
                cambpmRunMaven('engine/', 'clean test -Pcheck-plugins', runtimeStash: true)
              },
              postAlways: {
                cambpmPublishTestResult()
              }
            ])
          }
        }
        stage('engine-UNIT-database-table-prefix') {
          when {
            expression {
              cambpmIsNotFailedStageType(failedStageTypes, 'engine-unit') && cambpmWithLabels('all-db','h2','db2','mysql','oracle','mariadb','sqlserver','postgresql','cockroachdb') // TODO store as param
            }
              }
          steps {
            cambpmConditionalRetry([
              agentLabel: 'h2',
              runSteps: {
                cambpmRunMaven('engine/', 'clean test -Pdb-table-prefix', runtimeStash: true)
              },
              postAlways: {
                cambpmPublishTestResult()
              }
            ])
          }
        }
        stage('webapp-UNIT-database-table-prefix') {
          when {
            allOf {
              expression {
                cambpmIsNotFailedStageType(failedStageTypes, 'webapp-unit')
              }
              branch cambpmDefaultBranch();
            }
              }
          steps {
            cambpmConditionalRetry([
              agentLabel: 'h2',
              runSteps: {
                cambpmRunMaven('webapps/', 'clean test -Pdb-table-prefix', true, runtimeStash: true)
              },
              postAlways: {
                cambpmPublishTestResult()
              }
            ])
          }
        }
        stage('engine-UNIT-wls-compatibility') {
          when {
            allOf {
              expression {
                cambpmIsNotFailedStageType(failedStageTypes, 'engine-unit')
              }
              branch cambpmDefaultBranch();
            }
              }
          steps {
            cambpmConditionalRetry([
              agentLabel: 'h2',
              runSteps: {
                cambpmRunMaven('.', 'clean verify -Pcheck-engine,wls-compatibility,jersey', runtimeStash: true)
              },
              postAlways: {
                cambpmPublishTestResult()
              }
            ])
          }
        }
        stage('engine-IT-wildfly-domain') {
          when {
            expression {
              cambpmIsNotFailedStageType(failedStageTypes, 'engine-IT-wildfly') && cambpmWithLabels('wildfly')
            }
              }
          steps {
            cambpmConditionalRetry([
              agentLabel: 'h2',
              runSteps: {
                cambpmRunMaven('qa/', 'clean install -Pwildfly-domain,h2,engine-integration', runtimeStash: true, archiveStash: true)
              },
              postAlways: {
                cambpmPublishTestResult()
              }
            ])
          }
        }
        stage('engine-IT-wildfly-servlet') {
          when {
            expression {
              cambpmIsNotFailedStageType(failedStageTypes, 'engine-IT-wildfly') && cambpmWithLabels('wildfly')
            }
              }
          steps {
            cambpmConditionalRetry([
              agentLabel: 'h2',
              runSteps: {
                cambpmRunMaven('qa/', 'clean install -Pwildfly,wildfly-servlet,h2,engine-integration', runtimeStash: true, archiveStash: true)
              },
              postAlways: {
                cambpmPublishTestResult()
              }
            ])
          }
        }
      }
    }
  }
  post {
    changed {
      script {
        if (!agentDisconnected()){
          cambpmSendEmailNotification()
        }
      }
    }
  }
}
