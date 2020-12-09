// https://github.com/camunda/jenkins-global-shared-library
// https://github.com/camunda/cambpm-jenkins-shared-library
@Library(['camunda-ci', 'cambpm-jenkins-shared-library@pipeline-ahmed']) _

def failedStageTypes = []

pipeline {
  agent none
  options {
    buildDiscarder(logRotator(numToKeepStr: '5')) //, artifactNumToKeepStr: '30'
    copyArtifactPermission('*');
  }
  parameters {
      string defaultValue: cambpmDefaultBranch(), description: 'The name of the EE branch to run the EE pipeline on',
      name: 'EE_BRANCH_NAME'
  }

  stages {

    // For more details, please read the note in dbTasksMatrix method.
    stage("DB Matrix") {
      // Should be called in declarative "parallel" to make the visualization works in Jenkins Blue Ocean UI.
      parallel {
        stage('Run DB Matrix') {
          steps {
            script {
              parallel(dbTasksMatrix())
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
        stage('IT-wildfly-domain') {
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
        stage('IT-wildfly-servlet') {
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
                cambpmRunMaven('qa/', 'clean install -Pwildfly,wildfly-servlet,h2,engine-integration', runtimeStash: true, archiveStash: true, qaStash: true)
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

def dbTasksMatrix() {
  // This is a workaround to build a matrix programatically and run it in scripted pipeline style
  // till the Jenkins bug "Method code too large!" is fixed.
  // https://issues.jenkins.io/browse/JENKINS-37984
  // https://www.jenkins.io/blog/2019/12/02/matrix-building-with-scripted-pipeline

  Map matrix_axes = [
      DB: [
          'postgresql_96', 'postgresql_94', 'postgresql_107', 'postgresql_112', 'postgresql_122',
          'cockroachdb_201', 'mariadb_100', 'mariadb_102', 'mariadb_103', 'mariadb_galera', 'mysql_57',
          'oracle_11', 'oracle_12', 'oracle_18', 'oracle_19', 'db2_105', 'db2_111', 'sqlserver_2017', 'sqlserver_2019'
      ],
      PROFILE: [
          'engine-unit', 'engine-unit-authorizations', 'webapps-unit', 'webapps-unit-authorizations'
      ]
  ]

  List axes = cambpmGetMatrixAxes(matrix_axes)

  // Parallel task map.
  Map tasks = [failFast: false]

  for(int i = 0; i < axes.size(); i++) {

    // Convert the Axis into valid values for withEnv step.
    Map axis = axes[i]
    List axisEnv = axis.collect { k, v ->
      "${k}=${v}"
    }

    String agentNodeLabel = axis['DB']

    // This If statement works like 'when' in the declarative style.
    // It only adds the database to tasks list according to the PR label.
    if (
    true
    // skipStageType(failedStageTypes, env.PROFILE) &&
    // (withLabels(getLabels(env.PROFILE)) || withDbLabels(env.DB))
    ) {
      tasks[axisEnv.join(', ')] = { ->
        node(agentNodeLabel) {
          withEnv(axisEnv) {
            stage("QA test") {
              // The 'stage' here works like a 'step' in the declarative style.
              stage("Run Maven DB") {
                echo "QA DB Test Stage: ${PROFILE}-${DB}"
                // catchError(stageResult: 'FAILURE') {
                withMaven(jdk: 'jdk-8-latest', maven: 'maven-3.2-latest',
                    mavenSettingsConfig: 'camunda-maven-settings',
                    options: [artifactsPublisher(disabled: true), junitPublisher(disabled: true)]
                ) {
                  runMaven(true, false, isQaStashEnabled(env.PROFILE), getMavenProfileDir(env.PROFILE), getMavenProfileCmd(env.PROFILE) + cambpmGetDbProfiles(env.DB) + " " + cambpmGetDbExtras(env.DB), true)
                }
                //}
              }
              stage("PublishTestResult for DB") {
                cambpmPublishTestResult();
              }
            }
          }
        }
      }
    }
  }

  return tasks
}