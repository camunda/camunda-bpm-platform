// https://github.com/camunda/jenkins-global-shared-library
// https://github.com/camunda/cambpm-jenkins-shared-library
@Library(['camunda-ci', 'cambpm-jenkins-shared-library']) _

def failedStageTypes = []

pipeline {
  agent none
  options {
    buildDiscarder(logRotator(numToKeepStr: '5')) //, artifactNumToKeepStr: '30'
    copyArtifactPermission('*');
  }
  parameters {
    string defaultValue: 'cambpm-ee-main-pr/pipeline-master', description: 'The name of the EE branch to run the EE pipeline on, e.g. cambpm-ee-main/PR-333', name: 'EE_BRANCH_NAME'
  }
  stages {
    stage('ASSEMBLY') {
      when {
        expression {
          env.BRANCH_NAME == cambpmDefaultBranch() || !pullRequest.labels.contains('no-build')
        }
        beforeAgent true
      }
      agent {
        node {
          label 'h2_perf32'
        }
      }
      steps {
        withMaven(jdk: 'jdk-8-latest', maven: 'maven-3.2-latest', mavenSettingsConfig: 'camunda-maven-settings', options: [artifactsPublisher(disabled: true), junitPublisher(disabled: true)]) {
          nodejs('nodejs-14.6.0'){
            cambpmRunMaven('.', 'clean install source:jar -Pdistro,distro-ce,distro-wildfly,distro-webjar com.mycila:license-maven-plugin:check')
          }
        }

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
                               '.m2/org/camunda/**/*-SNAPSHOT/**/camunda-h2-webapp*.war')

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
            eeBranch = "cambpm-ee-main/pipeline-master"
          } else {
            eeBranch = params.EE_BRANCH_NAME
          }

          if (cambpmWithLabels('webapp-integration','all-as','h2','websphere','weblogic','jbosseap','run','spring-boot','authorizations')) {
            cambpmTriggerDownstream("cambpm-ee/" + eeBranch, true, true, true)
          }

          if (cambpmWithLabels('all-db','cockroachdb','authorizations')) {
            cambpmTriggerDownstream("cambpm-ce/cambpm-sidetrack/${env.BRANCH_NAME}")
          }

          if (cambpmWithLabels('daily','default-build','rolling-update','migration','all-db','h2','db2','mysql','oracle','mariadb','sqlserver','postgresql','cockroachdb')) {
            cambpmTriggerDownstream("cambpm-ce/cambpm-daily/${env.BRANCH_NAME}")
          }

          if (cambpmWithLabels('master')) {
            withMaven(jdk: 'jdk-8-latest', maven: 'maven-3.2-latest', mavenSettingsConfig: 'camunda-maven-settings', options: [artifactsPublisher(disabled: true), junitPublisher(disabled: true)]) {
              nodejs('nodejs-14.6.0'){
                cambpmRunMaven('.', 'org.sonatype.plugins:nexus-staging-maven-plugin:deploy-staged -DaltStagingDirectory=${WORKSPACE}/staging -DskipStaging=true')
              }
            }
          }
        }

      }
    }
    stage('h2 tests') {
      parallel {
        stage('engine-UNIT-h2') {
          when {
            expression {
              cambpmWithLabels('h2', 'rolling-update', 'migration')
            }
            beforeAgent true
          }
          agent {
            node {
              label 'h2'
            }
          }
          steps {
            catchError(stageResult: 'FAILURE') {
              withMaven(jdk: 'jdk-8-latest', maven: 'maven-3.2-latest', mavenSettingsConfig: 'camunda-maven-settings', options: [artifactsPublisher(disabled: true), junitPublisher(disabled: true)]) {
                cambpmRunMavenByStageType('engine-unit', 'h2')
              }
            }
          }
          post {
            always {
              cambpmPublishTestResult();
            }
            failure {
              cambpmAddFailedStageType(failedStageTypes, 'engine-unit')
            }
          }
        }
        stage('engine-UNIT-authorizations-h2') {
          when {
            expression {
              cambpmWithLabels('h2','authorizations')
            }
            beforeAgent true
          }
          agent {
            node {
              label 'h2'
            }
          }
          steps {
            catchError(stageResult: 'FAILURE') {
              withMaven(jdk: 'jdk-8-latest', maven: 'maven-3.2-latest', mavenSettingsConfig: 'camunda-maven-settings', options: [artifactsPublisher(disabled: true), junitPublisher(disabled: true)]) {
                cambpmRunMavenByStageType('engine-unit-authorizations', 'h2')
              }
            }
          }
          post {
            always {
              cambpmPublishTestResult();
            }
            failure {
              cambpmAddFailedStageType(failedStageTypes, 'engine-unit-authorizations')
            }
          }
        }
        stage('webapp-UNIT-h2') {
          when {
            expression {
              cambpmWithLabels('default-build')
            }
            beforeAgent true
          }
          agent {
            node {
              label 'h2'
            }
          }
          steps {
            catchError(stageResult: 'FAILURE') {
              withMaven(jdk: 'jdk-8-latest', maven: 'maven-3.2-latest', mavenSettingsConfig: 'camunda-maven-settings', options: [artifactsPublisher(disabled: true), junitPublisher(disabled: true)]) {
                cambpmRunMavenByStageType('webapp-unit', 'h2')
              }
            }
          }
          post {
            always {
              cambpmPublishTestResult();
            }
            failure {
              cambpmAddFailedStageType(failedStageTypes, 'webapp-unit')
            }
          }
        }
        stage('webapp-UNIT-authorizations-h2') {
          when {
            expression {
              cambpmWithLabels('default-build')
            }
          }
          agent {
            node {
              label 'h2'
            }
          }
          steps {
            catchError(stageResult: 'FAILURE') {
              withMaven(jdk: 'jdk-8-latest', maven: 'maven-3.2-latest', mavenSettingsConfig: 'camunda-maven-settings', options: [artifactsPublisher(disabled: true), junitPublisher(disabled: true)]) {
                cambpmRunMavenByStageType('webapp-unit-authorizations', 'h2')
              }
            }
          }
          post {
            always {
              cambpmPublishTestResult();
            }
            failure {
              cambpmAddFailedStageType(failedStageTypes, 'webapp-unit-authorizations')
            }
          }
        }
        stage('engine-rest-UNIT-jersey-2') {
          when {
            expression {
              cambpmWithLabels('rest-api')
            }
            beforeAgent true
          }
          agent {
            node {
              label 'h2'
            }
          }
          steps {
            catchError(stageResult: 'FAILURE') {
              withMaven(jdk: 'jdk-8-latest', maven: 'maven-3.2-latest', mavenSettingsConfig: 'camunda-maven-settings', options: [artifactsPublisher(disabled: true), junitPublisher(disabled: true)]) {
                cambpmRunMaven('engine-rest/engine-rest/', 'clean install -Pjersey2', runtimeStash: true)
              }
            }
          }
          post {
            always {
              cambpmPublishTestResult();
            }
          }
        }
        stage('engine-rest-UNIT-resteasy') {
          when {
            expression {
              cambpmWithLabels('rest-api')
            }
            beforeAgent true
          }
          agent {
            node {
              label 'h2'
            }
          }
          steps {
            catchError(stageResult: 'FAILURE') {
              withMaven(jdk: 'jdk-8-latest', maven: 'maven-3.2-latest', mavenSettingsConfig: 'camunda-maven-settings', options: [artifactsPublisher(disabled: true), junitPublisher(disabled: true)]) {
                cambpmRunMaven('engine-rest/engine-rest/', 'clean install -Presteasy', runtimeStash: true)
              }
            }
          }
          post {
            always {
              cambpmPublishTestResult();
            }
          }
        }
        stage('engine-rest-UNIT-resteasy3') {
          when {
            expression {
              cambpmWithLabels('rest-api')
            }
            beforeAgent true
          }
          agent {
            node {
              label 'h2'
            }
          }
          steps {
            catchError(stageResult: 'FAILURE') {
              withMaven(jdk: 'jdk-8-latest', maven: 'maven-3.2-latest', mavenSettingsConfig: 'camunda-maven-settings', options: [artifactsPublisher(disabled: true), junitPublisher(disabled: true)]) {
                cambpmRunMaven('engine-rest/engine-rest/', 'clean install -Presteasy3', runtimeStash: true)
              }
            }
          }
          post {
            always {
              cambpmPublishTestResult();
            }
          }
        }
        stage('engine-rest-UNIT-cxf') {
          when {
            expression {
              cambpmWithLabels('rest-api')
            }
            beforeAgent true
          }
          agent {
            node {
              label 'h2'
            }
          }
          steps {
            catchError(stageResult: 'FAILURE') {
              withMaven(jdk: 'jdk-8-latest', maven: 'maven-3.2-latest', mavenSettingsConfig: 'camunda-maven-settings', options: [artifactsPublisher(disabled: true), junitPublisher(disabled: true)]) {
                cambpmRunMaven('engine-rest/engine-rest/', 'clean install -Pcxf', runtimeStash: true)
              }
            }
          }
          post {
            always {
              cambpmPublishTestResult();
            }
          }
        }
        stage('engine-rest-UNIT-wink') {
          when {
            expression {
              cambpmWithLabels('rest-api')
            }
            beforeAgent true
          }
          agent {
            node {
              label 'h2'
            }
          }
          steps {
            catchError(stageResult: 'FAILURE') {
              withMaven(jdk: 'jdk-8-latest', maven: 'maven-3.2-latest', mavenSettingsConfig: 'camunda-maven-settings', options: [artifactsPublisher(disabled: true), junitPublisher(disabled: true)]) {
                cambpmRunMaven('engine-rest/engine-rest/', 'clean install -Pwink', runtimeStash: true)
              }
            }
          }
          post {
            always {
              cambpmPublishTestResult();
            }
          }
        }
        stage('engine-rest-jaxrs2-UNIT-jersey2') {
          when {
            expression {
              cambpmWithLabels('rest-api')
            }
            beforeAgent true
          }
          agent {
            node {
              label 'h2'
            }
          }
          steps {
            catchError(stageResult: 'FAILURE') {
              withMaven(jdk: 'jdk-8-latest', maven: 'maven-3.2-latest', mavenSettingsConfig: 'camunda-maven-settings', options: [artifactsPublisher(disabled: true), junitPublisher(disabled: true)]) {
                cambpmRunMaven('engine-rest/engine-rest-jaxrs2/', 'clean install -Pjersey2', runtimeStash: true)
              }
            }
          }
          post {
            always {
              cambpmPublishTestResult();
            }
          }
        }
        stage('engine-rest-jaxrs2-UNIT-resteasy3') {
          when {
            expression {
              cambpmWithLabels('rest-api')
            }
            beforeAgent true
          }
          agent {
            node {
              label 'h2'
            }
          }
          steps {
            catchError(stageResult: 'FAILURE') {
              withMaven(jdk: 'jdk-8-latest', maven: 'maven-3.2-latest', mavenSettingsConfig: 'camunda-maven-settings', options: [artifactsPublisher(disabled: true), junitPublisher(disabled: true)]) {
                cambpmRunMaven('engine-rest/engine-rest-jaxrs2/', 'clean install -Presteasy3', runtimeStash: true)
              }
            }
          }
          post {
            always {
              cambpmPublishTestResult();
            }
          }
        }
        stage('engine-rest-UNIT-compatibility-wildfly') {
          when {
            expression {
              cambpmWithLabels('rest-api')
            }
            beforeAgent true
          }
          agent {
            node {
              label 'h2'
            }
          }
          steps {
            catchError(stageResult: 'FAILURE') {
              withMaven(jdk: 'jdk-8-latest', maven: 'maven-3.2-latest', mavenSettingsConfig: 'camunda-maven-settings', options: [artifactsPublisher(disabled: true), junitPublisher(disabled: true)]) {
                cambpmRunMaven('engine-rest/engine-rest/', 'clean install -Pwildfly-compatibility,resteasy', runtimeStash: true)
              }
            }
          }
          post {
            always {
              cambpmPublishTestResult();
            }
          }
        }
        stage('engine-rest-IT-embedded-wildfly') {
          when {
            expression {
              cambpmWithLabels('rest-api')
            }
            beforeAgent true
          }
          agent {
            node {
              label 'h2'
            }
          }
          steps {
            catchError(stageResult: 'FAILURE') {
              withMaven(jdk: 'jdk-8-latest', maven: 'maven-3.2-latest', mavenSettingsConfig: 'camunda-maven-settings', options: [artifactsPublisher(disabled: true), junitPublisher(disabled: true)]) {
                cambpmRunMaven('qa/', 'clean install -Pwildfly,h2,webapps-integration,embedded-engine-rest', runtimeStash: true, archiveStash: true)
              }
            }
          }
          post {
            always {
              cambpmPublishTestResult();
            }
          }
        }
        stage('engine-IT-tomcat-9-postgresql-96') {
          when {
            expression {
              cambpmWithLabels('all-as','tomcat')
            }
            beforeAgent true
          }
          agent {
            node {
              label 'postgresql_96'
            }
          }
          steps {
            catchError(stageResult: 'FAILURE') {
              withMaven(jdk: 'jdk-8-latest', maven: 'maven-3.2-latest', mavenSettingsConfig: 'camunda-maven-settings', options: [artifactsPublisher(disabled: true), junitPublisher(disabled: true)]) {
                cambpmRunMaven('qa/', 'clean install -Ptomcat,postgresql,engine-integration', runtimeStash: true, archiveStash: true)
              }
            }
          }
          post {
            always {
              cambpmPublishTestResult();
            }
          }
        }
        stage('engine-IT-wildfly-postgresql-96') {
          when {
            expression {
              cambpmWithLabels('all-as','wildfly')
            }
            beforeAgent true
          }
          agent {
            node {
              label 'postgresql_96'
            }
          }
          steps {
            catchError(stageResult: 'FAILURE') {
              withMaven(jdk: 'jdk-8-latest', maven: 'maven-3.2-latest', mavenSettingsConfig: 'camunda-maven-settings', options: [artifactsPublisher(disabled: true), junitPublisher(disabled: true)]) {
                cambpmRunMaven('qa/', 'clean install -Pwildfly,postgresql,engine-integration', runtimeStash: true, archiveStash: true)
              }
            }
          }
          post {
            always {
              cambpmPublishTestResult();
            }
            failure {
              cambpmAddFailedStageType(failedStageTypes, 'engine-IT-wildfly')
              cambpmArchiveArtifacts('qa/wildfly-runtime/target/**/standalone/log/server.log')
            }
          }
        }
        stage('engine-IT-XA-wildfly-postgresql-96') {
          when {
            branch cambpmDefaultBranch();
            beforeAgent true
          }
          agent {
            node {
              label 'postgresql_96'
            }
          }
          steps {
            catchError(stageResult: 'FAILURE') {
              withMaven(jdk: 'jdk-8-latest', maven: 'maven-3.2-latest', mavenSettingsConfig: 'camunda-maven-settings', options: [artifactsPublisher(disabled: true), junitPublisher(disabled: true)]) {
                cambpmRunMaven('qa/', 'clean install -Pwildfly,postgresql,postgresql-xa,engine-integration', runtimeStash: true, archiveStash: true)
              }
            }
          }
          post {
            always {
              cambpmPublishTestResult();
            }
            failure {
              cambpmArchiveArtifacts('qa/wildfly-runtime/target/**/standalone/log/server.log')
            }
          }
        }
        stage('webapp-IT-tomcat-9-h2') {
          when {
            expression {
              cambpmWithLabels('webapp-integration', 'h2')
            }
            beforeAgent true
          }
          agent {
            node {
              label 'chrome_78'
            }
          }
          steps {
            catchError(stageResult: 'FAILURE') {
              withMaven(jdk: 'jdk-8-latest', maven: 'maven-3.2-latest', mavenSettingsConfig: 'camunda-maven-settings', options: [artifactsPublisher(disabled: true), junitPublisher(disabled: true)]) {
                cambpmRunMaven('qa/', 'clean install -Ptomcat,h2,webapps-integration', runtimeStash: true, archiveStash: true)
              }
            }
          }
          post {
            always {
              cambpmPublishTestResult();
            }
          }
        }
        stage('webapp-IT-wildfly-h2') {
          when {
            expression {
              cambpmWithLabels('webapp-integration', 'h2')
            }
            beforeAgent true
          }
          agent {
            node {
              label 'chrome_78'
            }
          }
          steps {
            catchError(stageResult: 'FAILURE') {
              withMaven(jdk: 'jdk-8-latest', maven: 'maven-3.2-latest', mavenSettingsConfig: 'camunda-maven-settings', options: [artifactsPublisher(disabled: true), junitPublisher(disabled: true)]) {
                cambpmRunMaven('qa/', 'clean install -Pwildfly,h2,webapps-integration', runtimeStash: true, archiveStash: true)
              }
            }
          }
          post {
            always {
              cambpmPublishTestResult();
            }
          }
        }
        stage('webapp-IT-standalone-tomcat-9') {
          when {
            branch cambpmDefaultBranch();
            beforeAgent true
          }
          agent {
            node {
              label 'chrome_78'
            }
          }
          steps {
            catchError(stageResult: 'FAILURE') {
              withMaven(jdk: 'jdk-8-latest', maven: 'maven-3.2-latest', mavenSettingsConfig: 'camunda-maven-settings', options: [artifactsPublisher(disabled: true), junitPublisher(disabled: true)]) {
                cambpmRunMaven('qa/', 'clean install -Ptomcat-vanilla,webapps-integration-sa', runtimeStash: true, archiveStash: true)
              }
            }
          }
          post {
            always {
              cambpmPublishTestResult();
            }
          }
        }
        stage('webapp-IT-standalone-wildfly') {
          when {
            branch cambpmDefaultBranch();
            beforeAgent true
          }
          agent {
            node {
              label 'chrome_78'
            }
          }
          steps {
            catchError(stageResult: 'FAILURE') {
              withMaven(jdk: 'jdk-8-latest', maven: 'maven-3.2-latest', mavenSettingsConfig: 'camunda-maven-settings', options: [artifactsPublisher(disabled: true), junitPublisher(disabled: true)]) {
                cambpmRunMaven('qa/', 'clean install -Pwildfly-vanilla,webapps-integration-sa', runtimeStash: true, archiveStash: true)
              }
            }
          }
          post {
            always {
              cambpmPublishTestResult();
            }
          }
        }
        stage('camunda-run-IT') {
          when {
            expression {
              cambpmWithLabels('run')
            }
            beforeAgent true
          }
          agent {
            node {
              label 'chrome_78'
            }
          }
          steps {
            catchError(stageResult: 'FAILURE') {
              withMaven(jdk: 'jdk-8-latest', maven: 'maven-3.2-latest', mavenSettingsConfig: 'camunda-maven-settings', options: [artifactsPublisher(disabled: true), junitPublisher(disabled: true)]) {
                cambpmRunMaven('distro/run/', 'clean install -Pintegration-test-camunda-run', runtimeStash: true, archiveStash: true, qaStash: true)
              }
            }
          }
          post {
            always {
              cambpmPublishTestResult();
            }
          }
        }
        stage('spring-boot-starter-IT') {
          when {
            expression {
              cambpmWithLabels('spring-boot')
            }
            beforeAgent true
          }
          agent {
            node {
              label 'chrome_78'
            }
          }
          steps {
            catchError(stageResult: 'FAILURE') {
              withMaven(jdk: 'jdk-8-latest', maven: 'maven-3.2-latest', mavenSettingsConfig: 'camunda-maven-settings', options: [artifactsPublisher(disabled: true), junitPublisher(disabled: true)]) {
                cambpmRunMaven('spring-boot-starter/', 'clean install -Pintegration-test-spring-boot-starter', runtimeStash: true, archiveStash: true, qaStash: true)
              }
            }
          }
          post {
            always {
              cambpmPublishTestResult();
            }
          }
        }
      }
    }
//    stage('UNIT DB tests') {
//      matrix {
//        axes {
//          axis {
//            name 'DB'
//            values 'postgresql_96', 'postgresql_94', 'postgresql_107'
//          }
//          axis {
//            name 'STAGE_TYPE'
//            values 'engine-unit', 'engine-unit-authorizations', 'webapp-unit', 'webapp-unit-authorizations'
//          }
//        }
//        when {
//          expression {
//            cambpmIsNotFailedStageType(failedStageTypes, env.STAGE_TYPE) && cambpmWithLabelsList(cambpmGetLabels(env.STAGE_TYPE, 'cockroachdb'))
//          }
//          beforeAgent true
//        }
//        agent {
//          node {
//            label env.DB
//          }
//        }
//        stages {
//          stage('UNIT test') {
//            steps {
//              echo("UNIT DB Test Stage: ${env.STAGE_TYPE}-${env.DB}")
//              catchError(stageResult: 'FAILURE') {
//                withMaven(jdk: 'jdk-8-latest', maven: 'maven-3.2-latest', mavenSettingsConfig: 'camunda-maven-settings', options: [artifactsPublisher(disabled: true), junitPublisher(disabled: true)]) {
//                  cambpmRunMavenByStageType(env.STAGE_TYPE, env.DB)
//                }
//              }
//            }
//            post {
//              always {
//                cambpmPublishTestResult();
//              }
//            }
//          }
//        }
//      }
//    }
    stage('db tests + CE webapps IT') {
      parallel {
        stage('engine-api-compatibility') {
          when {
            allOf {
              expression {
                cambpmIsNotFailedStageType(failedStageTypes, 'engine-unit')
              }
              branch cambpmDefaultBranch();
            }
            beforeAgent true
          }
          agent {
            node {
              label 'h2'
            }
          }
          steps {
            catchError(stageResult: 'FAILURE') {
              withMaven(jdk: 'jdk-8-latest', maven: 'maven-3.2-latest', mavenSettingsConfig: 'camunda-maven-settings', options: [artifactsPublisher(disabled: true), junitPublisher(disabled: true)]) {
                cambpmRunMaven('engine/', 'clean verify -Pcheck-api-compatibility', runtimeStash: true)
              }
            }
          }
          post {
            always {
              cambpmPublishTestResult();
            }
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
            beforeAgent true
          }
          agent {
            node {
              label 'h2'
            }
          }
          steps {
            catchError(stageResult: 'FAILURE') {
              withMaven(jdk: 'jdk-8-latest', maven: 'maven-3.2-latest', mavenSettingsConfig: 'camunda-maven-settings', options: [artifactsPublisher(disabled: true), junitPublisher(disabled: true)]) {
                cambpmRunMaven('engine/', 'clean test -Pcheck-plugins', runtimeStash: true)
              }
            }
          }
          post {
            always {
              cambpmPublishTestResult();
            }
          }
        }
        stage('engine-UNIT-database-table-prefix') {
          when {
            expression {
              cambpmIsNotFailedStageType(failedStageTypes, 'engine-unit') && cambpmWithLabels('all-db','h2','db2','mysql','oracle','mariadb','sqlserver','postgresql','cockroachdb') // TODO store as param
            }
            beforeAgent true
          }
          agent {
            node {
              label 'h2'
            }
          }
          steps {
            catchError(stageResult: 'FAILURE') {
              withMaven(jdk: 'jdk-8-latest', maven: 'maven-3.2-latest', mavenSettingsConfig: 'camunda-maven-settings', options: [artifactsPublisher(disabled: true), junitPublisher(disabled: true)]) {
                cambpmRunMaven('engine/', 'clean test -Pdb-table-prefix', runtimeStash: true)
              }
            }
          }
          post {
            always {
              cambpmPublishTestResult();
            }
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
            beforeAgent true
          }
          agent {
            node {
              label 'h2'
            }
          }
          steps {
            catchError(stageResult: 'FAILURE') {
              withMaven(jdk: 'jdk-8-latest', maven: 'maven-3.2-latest', mavenSettingsConfig: 'camunda-maven-settings', options: [artifactsPublisher(disabled: true), junitPublisher(disabled: true)]) {
                nodejs('nodejs-14.6.0') {
                  cambpmRunMaven('webapps/', 'clean test -Pdb-table-prefix', runtimeStash: true)
                }
              }
            }
          }
          post {
            always {
              cambpmPublishTestResult();
            }
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
            beforeAgent true
          }
          agent {
            node {
              label 'h2'
            }
          }
          steps {
            catchError(stageResult: 'FAILURE') {
              withMaven(jdk: 'jdk-8-latest', maven: 'maven-3.2-latest', mavenSettingsConfig: 'camunda-maven-settings', options: [artifactsPublisher(disabled: true), junitPublisher(disabled: true)]) {
                cambpmRunMaven('.', 'clean verify -Pcheck-engine,wls-compatibility,jersey', runtimeStash: true)
              }
            }
          }
          post {
            always {
              cambpmPublishTestResult();
            }
          }
        }
        stage('engine-IT-wildfly-domain') {
          when {
            expression {
              cambpmIsNotFailedStageType(failedStageTypes, 'engine-IT-wildfly') && cambpmWithLabels('wildfly')
            }
            beforeAgent true
          }
          agent {
            node {
              label 'h2'
            }
          }
          steps {
            catchError(stageResult: 'FAILURE') {
              withMaven(jdk: 'jdk-8-latest', maven: 'maven-3.2-latest', mavenSettingsConfig: 'camunda-maven-settings', options: [artifactsPublisher(disabled: true), junitPublisher(disabled: true)]) {
                cambpmRunMaven('qa/', 'clean install -Pwildfly-domain,h2,engine-integration', runtimeStash: true, archiveStash: true)
              }
            }
          }
          post {
            always {
              cambpmPublishTestResult();
            }
          }
        }
        stage('engine-IT-wildfly-servlet') {
          when {
            expression {
              cambpmIsNotFailedStageType(failedStageTypes, 'engine-IT-wildfly') && cambpmWithLabels('wildfly')
            }
            beforeAgent true
          }
          agent {
            node {
              label 'h2'
            }
          }
          steps {
            catchError(stageResult: 'FAILURE') {
              withMaven(jdk: 'jdk-8-latest', maven: 'maven-3.2-latest', mavenSettingsConfig: 'camunda-maven-settings', options: [artifactsPublisher(disabled: true), junitPublisher(disabled: true)]) {
                cambpmRunMaven('qa/', 'clean install -Pwildfly,wildfly-servlet,h2,engine-integration', runtimeStash: true, archiveStash: true)
              }
            }
          }
          post {
            always {
              cambpmPublishTestResult();
            }
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
