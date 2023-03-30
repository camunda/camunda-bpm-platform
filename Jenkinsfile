// https://github.com/camunda/jenkins-global-shared-library
// https://github.com/camunda/cambpm-jenkins-shared-library
@Library(['camunda-ci', 'cambpm-jenkins-shared-library@3295']) _

def failedStageTypes = []

pipeline {
  agent {
    node {
      label 'jenkins-job-runner'
    }
  }
  environment {
    LOGGER_LOG_LEVEL = 'DEBUG'
    MAVEN_VERSION = 'maven-3.8-latest'
  }
  options {
    buildDiscarder(logRotator(numToKeepStr: '5'))
    copyArtifactPermission('*')
    disableConcurrentBuilds(abortPrevious: true)
   }
  parameters {
    string name: 'EE_DOWNSTREAM', defaultValue: 'cambpm-ee-main-pr/' + cambpmDefaultBranch(), description: 'The name of the EE branch/PR to run the EE pipeline on, e.g. cambpm-ee-main/PR-333'
  }
  stages {
    stage('h2 UNIT, engine IT, webapp IT') {
      parallel {
        stage('db-UNIT-h2') {
          when {
            expression {
              cambpmWithLabels('h2', 'rolling-update', 'migration', 'all-db', 'default-build', 'authorizations')
            }
          }
          steps {
            cambpmConditionalRetry([
              agentLabel: 'h2',
              runSteps: {
                cambpmRunMavenByStageType('db-unit', 'h2')
              },
              postFailure: {
                cambpmPublishTestResult()
                cambpmAddFailedStageType(failedStageTypes, 'db-unit')
              }
            ])
          }
        }
        stage('db-UNIT-authorizations-h2') {
          when {
            expression {
              cambpmWithLabels('h2', 'authorizations')
            }
          }
          steps {
            cambpmConditionalRetry([
              agentLabel: 'h2',
              runSteps: {
                cambpmRunMavenByStageType('db-unit-authorizations', 'h2')
              },
              postFailure: {
                cambpmPublishTestResult()
                cambpmAddFailedStageType(failedStageTypes, 'db-unit-authorizations')
              }
            ])
          }
        }
        stage('engine-UNIT-historylevel-audit') {
          when {
            expression {
              cambpmWithLabels('default-build')
            }
          }
          steps {
            cambpmConditionalRetry([
              agentLabel: 'h2',
              runSteps: {
                cambpmRunMaven('engine/', 'verify -Pcfghistoryaudit', runtimeStash: true)
              },
              postFailure: {
                cambpmPublishTestResult()
              },
            ])
          }
        }
        stage('engine-UNIT-historylevel-activity') {
          when {
            expression {
              cambpmWithLabels('default-build')
            }
          }
          steps {
            cambpmConditionalRetry([
              agentLabel: 'h2',
              runSteps: {
                cambpmRunMaven('engine/', 'verify -Pcfghistoryactivity', runtimeStash: true)
              },
              postFailure: {
                cambpmPublishTestResult()
              }
            ])
          }
        }
        stage('engine-IT-tomcat-9-postgresql-142') {
          when {
            expression {
              cambpmWithLabels('all-as', 'tomcat')
            }
          }
          steps {
            cambpmConditionalRetry([
              agentLabel: 'postgresql_142',
              runSteps: {
                cambpmRunMaven('qa/', 'clean install -Ptomcat,postgresql,engine-integration', runtimeStash: true, archiveStash: true)
              },
              postFailure: {
                cambpmPublishTestResult()
              }
            ])
          }
        }
        stage('engine-IT-wildfly-postgresql-142') {
          when {
            expression {
              cambpmWithLabels('all-as', 'wildfly')
            }
          }
          steps {
            cambpmConditionalRetry([
              agentLabel: 'postgresql_142',
              runSteps: {
                cambpmRunMaven('qa/', 
                  'clean install -Pwildfly,postgresql,engine-integration-jakarta', 
                  runtimeStash: true, 
                  archiveStash: true,
                  // we need to use JDK 11 for WildFly 27+
                  jdkVersion: 'jdk-11-latest')
              },
              postFailure: {
                cambpmPublishTestResult()
                cambpmAddFailedStageType(failedStageTypes, 'engine-IT-wildfly')
                cambpmArchiveArtifacts('qa/wildfly-runtime/target/**/standalone/log/server.log')
              }
            ])
          }
        }
        stage('engine-IT-wildfly26-postgresql-142') {
          when {
            expression {
              cambpmWithLabels('all-as', 'wildfly')
            }
          }
          steps {
            cambpmConditionalRetry([
              agentLabel: 'postgresql_142',
              runSteps: {
                cambpmRunMaven('qa/', 'clean install -Pwildfly26,postgresql,engine-integration', runtimeStash: true, archiveStash: true)
              },
              postFailure: {
                cambpmPublishTestResult()
                cambpmAddFailedStageType(failedStageTypes, 'engine-IT-wildfly26')
                cambpmArchiveArtifacts('qa/wildfly26-runtime/target/**/standalone/log/server.log')
              }
            ])
          }
        }
        stage('engine-IT-XA-wildfly-postgresql-142') {
          when {
            expression {
              cambpmWithLabels('wildfly')
            }
          }
          steps {
            cambpmConditionalRetry([
              agentLabel: 'postgresql_142',
              runSteps: {
                cambpmRunMaven('qa/', 
                  'clean install -Pwildfly,postgresql,postgresql-xa,engine-integration-jakarta', 
                  runtimeStash: true, 
                  archiveStash: true,
                  // we need to use JDK 11 for WildFly 27+
                  jdkVersion: 'jdk-11-latest')
              },
              postFailure: {
                cambpmPublishTestResult()
                cambpmArchiveArtifacts('qa/wildfly-runtime/target/**/standalone/log/server.log')
              }
            ])
          }
        }
        stage('engine-IT-XA-wildfly26-postgresql-142') {
          when {
            expression {
              cambpmWithLabels('wildfly')
            }
          }
          steps {
            cambpmConditionalRetry([
              agentLabel: 'postgresql_142',
              runSteps: {
                cambpmRunMaven('qa/', 'clean install -Pwildfly26,postgresql,postgresql-xa,engine-integration', runtimeStash: true, archiveStash: true)
              },
              postFailure: {
                cambpmPublishTestResult()
                cambpmArchiveArtifacts('qa/wildfly26-runtime/target/**/standalone/log/server.log')
              }
            ])
          }
        }
        stage('webapp-IT-tomcat-9-h2') {
          when {
            expression {
              cambpmWithLabels('webapp-integration', 'h2')
            }
          }
          steps {
            cambpmConditionalRetry([
              agentLabel: 'chrome_78',
              runSteps: {
                cambpmRunMaven('qa/', 'clean install -Ptomcat,h2,webapps-integration', runtimeStash: true, archiveStash: true)
              },
              postFailure: {
                cambpmPublishTestResult()
                cambpmArchiveArtifacts('qa/integration-tests-webapps/shared-engine/target/selenium-screenshots/*')
              }
            ])
          }
        }
        stage('webapp-IT-wildfly-h2') {
          when {
            expression {
              cambpmWithLabels('webapp-integration', 'h2', 'wildfly')
            }
          }
          steps {
            cambpmConditionalRetry([
              agentLabel: 'chrome_78',
              runSteps: {
                cambpmRunMaven('qa/',
                  'clean install -Pwildfly,h2,webapps-integration',
                  runtimeStash: true,
                  archiveStash: true,
                  // we need to use JDK 11 for WildFly 27+
                  jdkVersion: 'jdk-11-latest')
              },
              postFailure: {
                cambpmPublishTestResult()
                cambpmArchiveArtifacts('qa/integration-tests-webapps/shared-engine/target/selenium-screenshots/*')
              }
            ])
          }
        }
        stage('webapp-IT-wildfly26-h2') {
          when {
            expression {
              cambpmWithLabels('webapp-integration', 'h2', 'wildfly')
            }
          }
          steps {
            cambpmConditionalRetry([
              agentLabel: 'chrome_78',
              runSteps: {
                cambpmRunMaven('qa/', 'clean install -Pwildfly26,h2,webapps-integration', runtimeStash: true, archiveStash: true)
              },
              postFailure: {
                cambpmPublishTestResult()
                cambpmArchiveArtifacts('qa/integration-tests-webapps/shared-engine/target/selenium-screenshots/*')
              }
            ])
          }
        }
        stage('webapp-IT-standalone-tomcat-9') {
          when {
            expression {
              cambpmWithLabels('tomcat', 'webapp-integration')
            }
          }
          steps {
            cambpmConditionalRetry([
              agentLabel: 'chrome_78',
              runSteps: {
                cambpmRunMaven('qa/', 'clean install -Ptomcat-vanilla,webapps-integration-sa', runtimeStash: true, archiveStash: true)
              },
              postFailure: {
                cambpmPublishTestResult()
                cambpmArchiveArtifacts('qa/integration-tests-webapps/standalone-engine/target/selenium-screenshots/*')
              }
            ])
          }
        }
        stage('webapp-IT-standalone-wildfly26') {
          when {
            expression {
              cambpmWithLabels('wildfly', 'webapp-integration')
            }
          }
          steps {
            cambpmConditionalRetry([
              agentLabel: 'chrome_78',
              runSteps: {
                cambpmRunMaven('qa/', 'clean install -Pwildfly26-vanilla,webapps-integration-sa', runtimeStash: true, archiveStash: true)
              },
              postFailure: {
                cambpmPublishTestResult()
                cambpmArchiveArtifacts('qa/integration-tests-webapps/standalone-engine/target/selenium-screenshots/*')
              }
            ])
          }
        }
        stage('camunda-run-IT') {
          when {
            expression {
              cambpmWithLabels('run')
            }
          }
          steps {
            cambpmConditionalRetry([
              agentLabel: 'chrome_78',
              runSteps: {
                cambpmRunMaven('distro/run/', 'clean install -Pintegration-test-camunda-run', runtimeStash: true, archiveStash: true, qaStash: true)
              },
              postFailure: {
                cambpmPublishTestResult()
                cambpmArchiveArtifacts('distro/run/qa/runtime/target/selenium-screenshots/*')
              }
            ])
          }
        }
        stage('spring-boot-starter-IT') {
          when {
            expression {
              cambpmWithLabels('spring-boot')
            }
          }
          steps {
            cambpmConditionalRetry([
              agentLabel: 'chrome_78',
              runSteps: {
                cambpmRunMaven('spring-boot-starter/', 'clean install -Pintegration-test-spring-boot-starter', runtimeStash: true, archiveStash: true, qaStash: true)
              },
              postFailure: {
                cambpmPublishTestResult()
              }
            ])
          }
        }
      }
    }
    stage('Engine Rest UNIT tests') {
      steps {
        script {
          // see the .ci/config/matrices.yaml for the stage generation values
          // see .ci/config/stage-types.yaml for the stage configurations
          parallel(cambpmGetMatrixStages('engine-rest', failedStageTypes, { stageInfo ->
            return cambpmWithLabels(stageInfo.allowedLabels)
          }))
        }
      }
    }
    stage('UNIT DB tests') {
      steps {
        script {
          // see the .ci/config/matrices.yaml for the stage generation values
          // see .ci/config/stage-types.yaml for the stage configurations
          parallel(cambpmGetMatrixStages('engine-webapp-unit', failedStageTypes, { stageInfo ->
            List allowedStageLabels = stageInfo.allowedLabels
            String dbLabel = stageInfo.nodeType
            return cambpmWithLabels(allowedStageLabels.minus('cockroachdb'), cambpmGetDbType(dbLabel))
          }))
        }
      }
    }
    stage('MISC tests') {
      parallel {
        stage('engine-api-compatibility') {
          when {
            expression {
              cambpmIsNotFailedStageType(failedStageTypes, 'engine-unit') && cambpmWithLabels()
            }
          }
          steps {
            cambpmConditionalRetry([
              agentLabel: 'h2',
              runSteps: {
                cambpmRunMaven('engine/', 'clean verify -Pcheck-api-compatibility', runtimeStash: true)
              }
            ])
          }
        }
        stage('engine-UNIT-plugins') {
          when {
            expression {
              cambpmIsNotFailedStageType(failedStageTypes, 'engine-unit') && cambpmWithLabels()
            }
          }
          steps {
            cambpmConditionalRetry([
              agentLabel: 'h2',
              runSteps: {
                cambpmRunMaven('engine/', 'clean test -Pcheck-plugins', runtimeStash: true)
              },
              postFailure: {
                cambpmPublishTestResult()
                // archive any heap dumps generated in the target folder
                cambpmArchiveArtifacts(false, '**/target/*.hprof')
              }
            ])
          }
        }
        stage('engine-UNIT-database-table-prefix') {
          when {
            expression {
              cambpmIsNotFailedStageType(failedStageTypes, 'engine-unit') && cambpmWithLabels('all-db','h2','db2','mysql','oracle','mariadb','sqlserver','postgresql','cockroachdb')
            }
          }
          steps {
            cambpmConditionalRetry([
              agentLabel: 'h2',
              runSteps: {
                cambpmRunMaven('engine/', 'clean test -Pdb-table-prefix', runtimeStash: true)
              },
              postFailure: {
                cambpmPublishTestResult()
              }
            ])
          }
        }
        stage('webapp-UNIT-database-table-prefix') {
          when {
            expression {
              cambpmIsNotFailedStageType(failedStageTypes, 'webapp-unit') && cambpmWithLabels()
            }
          }
          steps {
            cambpmConditionalRetry([
              agentLabel: 'h2',
              runSteps: {
                cambpmRunMaven('webapps/assembly', 'clean test -Pdb-table-prefix -Dskip.frontend.build=true', runtimeStash: true)
              },
              postFailure: {
                cambpmPublishTestResult()
              }
            ])
          }
        }
        stage('engine-UNIT-wls-compatibility') {
          when {
            expression {
              cambpmIsNotFailedStageType(failedStageTypes, 'engine-unit') && cambpmWithLabels('rest-api')
            }
          }
          steps {
            cambpmConditionalRetry([
              agentLabel: 'h2',
              runSteps: {
                cambpmRunMaven('.', 'clean verify -pl !engine-rest/docs/ -Pcheck-engine,wls-compatibility,jersey2', runtimeStash: true)
              },
              postFailure: {
                cambpmPublishTestResult()
                // archive any heap dumps generated in the target folder
                cambpmArchiveArtifacts(false, '**/target/*.hprof')
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
                cambpmRunMaven('qa/', 
                  'clean install -Pwildfly-domain,h2,engine-integration-jakarta',
                  runtimeStash: true,
                  archiveStash: true,
                  // we need to use JDK 11 for WildFly 27+
                  jdkVersion: 'jdk-11-latest')
              },
              postFailure: {
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
                cambpmRunMaven('qa/',
                  'clean install -Pwildfly,wildfly-servlet,h2,engine-integration-jakarta',
                  runtimeStash: true,
                  archiveStash: true,
                  // we need to use JDK 11 for WildFly 27+
                  jdkVersion: 'jdk-11-latest')
              },
              postFailure: {
                cambpmPublishTestResult()
              }
            ])
          }
        }
        stage('engine-IT-wildfly26-domain') {
          when {
            expression {
              cambpmIsNotFailedStageType(failedStageTypes, 'engine-IT-wildfly26') && cambpmWithLabels('wildfly')
            }
          }
          steps {
            cambpmConditionalRetry([
              agentLabel: 'h2',
              runSteps: {
                cambpmRunMaven('qa/', 'clean install -Pwildfly26-domain,h2,engine-integration', runtimeStash: true, archiveStash: true)
              },
              postFailure: {
                cambpmPublishTestResult()
              }
            ])
          }
        }
        stage('engine-IT-wildfly26-servlet') {
          when {
            expression {
              cambpmIsNotFailedStageType(failedStageTypes, 'engine-IT-wildfly26') && cambpmWithLabels('wildfly')
            }
          }
          steps {
            cambpmConditionalRetry([
              agentLabel: 'h2',
              runSteps: {
                cambpmRunMaven('qa/', 'clean install -Pwildfly26,wildfly26-servlet,h2,engine-integration', runtimeStash: true, archiveStash: true)
              },
              postFailure: {
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
    always {
      cambpmWithSpanAttributes()
    }
  }
}
