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
    string defaultValue: 'cambpm-ee-main-pr/master', description: 'The name of the EE branch/PR to run the EE pipeline on, e.g. cambpm-ee-main/PR-333', name: 'EE_DOWNSTREAM'
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
            cambpmRunMaven('.',
                'clean install source:jar -Pdistro,distro-ce,distro-wildfly,distro-webjar com.mycila:license-maven-plugin:check',
                withCatch: false,
                withNpm: true)

            // archive all .jar, .pom, .xml, .txt runtime artifacts + required .war/.zip/.tar.gz for EE pipeline
            // add a new line for each group of artifacts
            cambpmArchiveArtifacts('.m2/org/camunda/**/*-SNAPSHOT/**/*.jar,.m2/org/camunda/**/*-SNAPSHOT/**/*.pom,.m2/org/camunda/**/*-SNAPSHOT/**/*.xml,.m2/org/camunda/**/*-SNAPSHOT/**/*.txt',
                                  '.m2/org/camunda/**/*-SNAPSHOT/**/camunda-webapp*frontend-sources.zip',
                                  '.m2/org/camunda/**/*-SNAPSHOT/**/license-book*.zip',
                                  '.m2/org/camunda/**/*-SNAPSHOT/**/camunda-jboss-modules*.zip',
                                  '.m2/org/camunda/**/*-SNAPSHOT/**/camunda-*-assembly*.tar.gz',
                                  '.m2/org/camunda/**/*-SNAPSHOT/**/camunda-webapp*.war',
                                  '.m2/org/camunda/**/*-SNAPSHOT/**/camunda-engine-rest*.war',
                                  '.m2/org/camunda/**/*-SNAPSHOT/**/camunda-example-invoice*.war',
                                  '.m2/org/camunda/**/*-SNAPSHOT/**/camunda-h2-webapp*.war',
                                  '.m2/org/camunda/**/*-SNAPSHOT/**/camunda-bpm-run-modules-swaggerui-*-run-swaggerui-license-book-json.json')

            cambpmStash("platform-stash-runtime",
                        ".m2/org/camunda/**/*-SNAPSHOT/**",
                        "**/qa/**,**/*qa*/**,**/*.zip,**/*.tar.gz")
            cambpmStash("platform-stash-archives",
                        ".m2/org/camunda/bpm/**/*-SNAPSHOT/**/*.zip,.m2/org/camunda/bpm/**/*-SNAPSHOT/**/*.tar.gz")
            cambpmStash("platform-stash-qa",
                      ".m2/org/camunda/bpm/**/qa/**/*-SNAPSHOT/**,.m2/org/camunda/bpm/**/*qa*/**/*-SNAPSHOT/**",
                      "**/*.zip,**/*.tar.gz")

            script {
              if (env.BRANCH_NAME == cambpmDefaultBranch()) {
                // CE master triggers EE master
                // otherwise CE PR branch triggers EE PR branch
                eeMainProjectBranch = "cambpm-ee-main/pipeline-master"
              } else {
                eeMainProjectBranch = params.EE_DOWNSTREAM
              }

              if (cambpmWithLabels('webapp-integration', 'all-as', 'h2', 'websphere', 'weblogic', 'jbosseap', 'run', 'spring-boot', 'authorizations', 'e2e')) {
                cambpmTriggerDownstream(
                  "cambpm-ee/" + eeMainProjectBranch,
                  [string(name: 'UPSTREAM_PROJECT_NAME', value: env.JOB_NAME),
                  string(name: 'UPSTREAM_BUILD_NUMBER', value: env.BUILD_NUMBER)],
                  true, true, true
                )
              }

              if (cambpmWithLabels('all-db', 'cockroachdb', 'authorizations')) {
                cambpmTriggerDownstream(
                  "cambpm-ce/cambpm-sidetrack/${env.BRANCH_NAME}",
                  [string(name: 'UPSTREAM_PROJECT_NAME', value: env.JOB_NAME),
                  string(name: 'UPSTREAM_BUILD_NUMBER', value: env.BUILD_NUMBER)]
                )
              }

              if (cambpmWithLabels('daily', 'default-build', 'rolling-update', 'migration', 'all-db', 'h2', 'db2', 'mysql', 'oracle', 'mariadb', 'sqlserver', 'postgresql')) {
                cambpmTriggerDownstream(
                  "cambpm-ce/cambpm-daily/${env.BRANCH_NAME}",
                  [string(name: 'UPSTREAM_PROJECT_NAME', value: env.JOB_NAME),
                  string(name: 'UPSTREAM_BUILD_NUMBER', value: env.BUILD_NUMBER)]
                )
              }

              if (cambpmWithLabels()) {
                // only execute on `master`
                cambpmRunMaven('.',
                    'org.sonatype.plugins:nexus-staging-maven-plugin:deploy-staged -DaltStagingDirectory=${WORKSPACE}/staging -DskipStaging=true',
                    withCatch: false,
                    withNpm: true)
              }
            }
          }
        ])

      }
    }
    stage('h2 UNIT, engine IT, webapp IT') {
      parallel {
        stage('engine-UNIT-h2') {
          when {
            expression {
              cambpmWithLabels('h2', 'rolling-update', 'migration', 'all-db', 'default-build', 'authorizations')
            }
          }
          steps {
            cambpmConditionalRetry([
              agentLabel: 'h2',
              runSteps: {
                cambpmRunMavenByStageType('engine-unit', 'h2')
              },
              postAlways: {
                cambpmPublishTestResult()
              },
              postFailure: {
                cambpmAddFailedStageType(failedStageTypes, 'engine-unit')
              }
            ])
          }
        }
        stage('engine-UNIT-authorizations-h2') {
          when {
            expression {
              cambpmWithLabels('h2', 'authorizations')
            }
          }
          steps {
            cambpmConditionalRetry([
              agentLabel: 'h2',
              runSteps: {
                cambpmRunMavenByStageType('engine-unit-authorizations', 'h2')
              },
              postAlways: {
                cambpmPublishTestResult()
              },
              postFailure: {
                cambpmAddFailedStageType(failedStageTypes, 'engine-unit-authorizations')
              }
            ])
          }
        }
        stage('webapp-UNIT-h2') {
          when {
            expression {
              cambpmWithLabels('default-build')
            }
          }
          steps {
            cambpmConditionalRetry([
              agentLabel: 'h2',
              runSteps: {
                cambpmRunMavenByStageType('webapp-unit', 'h2')
              },
              postAlways: {
                cambpmPublishTestResult()
              },
              postFailure: {
                cambpmAddFailedStageType(failedStageTypes, 'webapp-unit')
              }
            ])
          }
        }
        stage('webapp-UNIT-authorizations-h2') {
          when {
            expression {
              cambpmWithLabels('default-build')
            }
          }
          steps {
            cambpmConditionalRetry([
              agentLabel: 'h2',
              runSteps: {
                cambpmRunMavenByStageType('webapp-unit-authorizations', 'h2')
              },
              postAlways: {
                cambpmPublishTestResult()
              },
              postFailure: {
                cambpmAddFailedStageType(failedStageTypes, 'webapp-unit-authorizations')
              }
            ])
          }
        }
        stage('engine-UNIT-historylevel-none') {
          when {
            expression {
              cambpmWithLabels('default-build')
            }
          }
          steps {
            cambpmConditionalRetry([
              agentLabel: 'h2',
              runSteps: {
                cambpmRunMaven('engine/', 'verify -Pcfghistorynone', runtimeStash: true)
              },
              postAlways: {
                cambpmPublishTestResult()
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
              postAlways: {
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
              postAlways: {
                cambpmPublishTestResult()
              }
            ])
          }
        }
        stage('engine-IT-tomcat-9-postgresql-96') {
          when {
            expression {
              cambpmWithLabels('all-as', 'tomcat')
            }
          }
          steps {
            cambpmConditionalRetry([
              agentLabel: 'postgresql_96',
              runSteps: {
                cambpmRunMaven('qa/', 'clean install -Ptomcat,postgresql,engine-integration', runtimeStash: true, archiveStash: true)
              },
              postAlways: {
                cambpmPublishTestResult()
              }
            ])
          }
        }
        stage('engine-IT-wildfly-postgresql-96') {
          when {
            expression {
              cambpmWithLabels('all-as', 'wildfly')
            }
          }
          steps {
            cambpmConditionalRetry([
              agentLabel: 'postgresql_96',
              runSteps: {
                cambpmRunMaven('qa/', 'clean install -Pwildfly,postgresql,engine-integration', runtimeStash: true, archiveStash: true)
              },
              postAlways: {
                cambpmPublishTestResult()
              },
              postFailure: {
                cambpmAddFailedStageType(failedStageTypes, 'engine-IT-wildfly')
                cambpmArchiveArtifacts('qa/wildfly-runtime/target/**/standalone/log/server.log')
              }
            ])
          }
        }
        stage('engine-IT-XA-wildfly-postgresql-96') {
          when {
            branch cambpmDefaultBranch();
          }
          steps {
            cambpmConditionalRetry([
              agentLabel: 'postgresql_96',
              runSteps: {
                cambpmRunMaven('qa/', 'clean install -Pwildfly,postgresql,postgresql-xa,engine-integration', runtimeStash: true, archiveStash: true)
              },
              postAlways: {
                cambpmPublishTestResult()
              },
              postFailure: {
                cambpmArchiveArtifacts('qa/wildfly-runtime/target/**/standalone/log/server.log')
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
              postAlways: {
                cambpmPublishTestResult()
              }
            ])
          }
        }
        stage('webapp-IT-wildfly-h2') {
          when {
            expression {
              cambpmWithLabels('webapp-integration', 'h2')
            }
          }
          steps {
            cambpmConditionalRetry([
              agentLabel: 'chrome_78',
              runSteps: {
                cambpmRunMaven('qa/', 'clean install -Pwildfly,h2,webapps-integration', runtimeStash: true, archiveStash: true)
              },
              postAlways: {
                cambpmPublishTestResult()
              }
            ])
          }
        }
        stage('webapp-IT-standalone-tomcat-9') {
          when {
            branch cambpmDefaultBranch();
          }
          steps {
            cambpmConditionalRetry([
              agentLabel: 'chrome_78',
              runSteps: {
                cambpmRunMaven('qa/', 'clean install -Ptomcat-vanilla,webapps-integration-sa', runtimeStash: true, archiveStash: true)
              },
              postAlways: {
                cambpmPublishTestResult()
              }
            ])
          }
        }
        stage('webapp-IT-standalone-wildfly') {
          when {
            branch cambpmDefaultBranch();
          }
          steps {
            cambpmConditionalRetry([
              agentLabel: 'chrome_78',
              runSteps: {
                cambpmRunMaven('qa/', 'clean install -Pwildfly-vanilla,webapps-integration-sa', runtimeStash: true, archiveStash: true)
              },
              postAlways: {
                cambpmPublishTestResult()
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
              postAlways: {
                cambpmPublishTestResult()
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
              postAlways: {
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
              cambpmIsNotFailedStageType(failedStageTypes, 'engine-unit') && cambpmWithLabels('all-db','h2','db2','mysql','oracle','mariadb','sqlserver','postgresql','cockroachdb')
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
