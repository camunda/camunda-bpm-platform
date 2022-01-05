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
    stage('ASSEMBLY') {
      when {
        expression {
          env.BRANCH_NAME == cambpmDefaultBranch() || (changeRequest() && !pullRequest.labels.contains('no-build'))
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
            cambpmRunMaven('.',
                'clean source:jar deploy source:test-jar com.mycila:license-maven-plugin:check -Pdistro,distro-ce,distro-wildfly,distro-webjar,h2-in-memory -DaltStagingDirectory=${WORKSPACE}/staging -DskipRemoteStaging=true',
                withCatch: false,
                withNpm: true,
                // we use JDK 11 to build the artifacts, as it is required by the Quarkus extension
                // the compiler source and target is set to JDK 8 in the release parents
                jdkVersion: 'jdk-11-latest')

            // archive all .jar, .pom, .xml, .txt runtime artifacts + required .war/.zip/.tar.gz for EE pipeline
            // add a new line for each group of artifacts
            cambpmArchiveArtifacts('.m2/org/camunda/**/*-SNAPSHOT/**/*.jar,.m2/org/camunda/**/*-SNAPSHOT/**/*.pom,.m2/org/camunda/**/*-SNAPSHOT/**/*.xml,.m2/org/camunda/**/*-SNAPSHOT/**/*.txt',
                                  '.m2/org/camunda/**/*-SNAPSHOT/**/camunda-webapp*frontend-sources.zip',
                                  '.m2/org/camunda/**/*-SNAPSHOT/**/license-book*.zip',
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
                eeMainProjectBranch = "cambpm-ee-main/" + cambpmDefaultBranch()
              } else {
                eeMainProjectBranch = params.EE_DOWNSTREAM
              }

              // JOB_NAME, e.g.: '7.15/cambpm-ce/cambpm-main/PR-1373'
              // keep leading slash for the abosolute project path
              platformVersionDir = "/" + env.JOB_NAME.split('/')[0]
              upstreamProjectName = "/" + env.JOB_NAME
              upstreamBuildNumber = env.BUILD_NUMBER

              if (cambpmWithLabels('webapp-integration', 'all-as', 'h2', 'websphere', 'weblogic', 'jbosseap', 'run', 'spring-boot', 'authorizations', 'e2e')) {
                cambpmTriggerDownstream(
                  platformVersionDir + "/cambpm-ee/" + eeMainProjectBranch,
                  [string(name: 'UPSTREAM_PROJECT_NAME', value: upstreamProjectName),
                  string(name: 'UPSTREAM_BUILD_NUMBER', value: upstreamBuildNumber)],
                  true, true, true
                )
              }

              // the sidetrack pipeline should be triggered on daily,
              // or PR builds only, master builds should be excluded.
              // The Sidetrack pipeline contains CRDB and Azure DB stages,
              // triggered with the cockroachdb and sqlserver PR labels.
              if (env.BRANCH_NAME != cambpmDefaultBranch() && cambpmWithLabels('all-db', 'cockroachdb', 'sqlserver', 'authorizations')) {
                cambpmTriggerDownstream(
                  platformVersionDir + "/cambpm-ce/cambpm-sidetrack/${env.BRANCH_NAME}",
                  [string(name: 'UPSTREAM_PROJECT_NAME', value: upstreamProjectName),
                  string(name: 'UPSTREAM_BUILD_NUMBER', value: upstreamBuildNumber)]
                )
              }

              if (cambpmWithLabels('daily', 'default-build', 'rolling-update', 'migration', 'wildfly', 'all-db', 'h2', 'db2', 'mysql', 'oracle', 'mariadb', 'sqlserver', 'postgresql')) {
                cambpmTriggerDownstream(
                  platformVersionDir + "/cambpm-ce/cambpm-daily/${env.BRANCH_NAME}",
                  [string(name: 'UPSTREAM_PROJECT_NAME', value: upstreamProjectName),
                  string(name: 'UPSTREAM_BUILD_NUMBER', value: upstreamBuildNumber)]
                )
              }

              // only execute on version (default) branch (e.g. master, 7.15)
              if (cambpmWithLabels()) {
                cambpmRunMaven('.',
                    'org.sonatype.plugins:nexus-staging-maven-plugin:deploy-staged -DaltStagingDirectory=${WORKSPACE}/staging -DskipStaging=true',
                    withCatch: false,
                    withNpm: true)
              }
            }
          },
          postFailure: {
            cambpmPublishTestResult()
          }
        ])

      }
    }
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
              postFailure: {
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
              postFailure: {
                cambpmPublishTestResult()
                cambpmAddFailedStageType(failedStageTypes, 'engine-IT-wildfly')
                cambpmArchiveArtifacts('qa/wildfly-runtime/target/**/standalone/log/server.log')
              }
            ])
          }
        }
        stage('engine-IT-XA-wildfly-postgresql-96') {
          when {
            expression {
              cambpmWithLabels('wildfly')
            }
          }
          steps {
            cambpmConditionalRetry([
              agentLabel: 'postgresql_96',
              runSteps: {
                cambpmRunMaven('qa/', 'clean install -Pwildfly,postgresql,postgresql-xa,engine-integration', runtimeStash: true, archiveStash: true)
              },
              postFailure: {
                cambpmPublishTestResult()
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
                cambpmRunMaven('qa/', 'clean install -Pwildfly,h2,webapps-integration', runtimeStash: true, archiveStash: true)
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
        stage('webapp-IT-standalone-wildfly') {
          when {
            expression {
              cambpmWithLabels('wildfly', 'webapp-integration')
            }
          }
          steps {
            cambpmConditionalRetry([
              agentLabel: 'chrome_78',
              runSteps: {
                cambpmRunMaven('qa/', 'clean install -Pwildfly-vanilla,webapps-integration-sa', runtimeStash: true, archiveStash: true)
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
              postFailure: {
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
              postFailure: {
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
                cambpmRunMaven('webapps/', 'clean test -Pdb-table-prefix -Dskip.frontend.build=true', runtimeStash: true)
              },
              postFailure: {
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
              postFailure: {
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
                cambpmRunMaven('qa/', 'clean install -Pwildfly,wildfly-servlet,h2,engine-integration', runtimeStash: true, archiveStash: true)
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
  }
}
