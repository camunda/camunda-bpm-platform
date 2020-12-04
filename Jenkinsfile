import groovy.json.JsonOutput

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
      string defaultValue: cambpmDefaultBranch(), description: 'The name of the EE branch to run the EE pipeline on', name: 'EE_BRANCH_NAME'
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
             configFileProvider([configFile(fileId: 'maven-nexus-settings', variable: 'MAVEN_SETTINGS_XML')]) {
               sh """
                 mvn -s \$MAVEN_SETTINGS_XML clean install source:jar -Pdistro,distro-ce,distro-wildfly,distro-webjar -DskipTests -Dmaven.repo.local=\${WORKSPACE}/.m2 com.mycila:license-maven-plugin:check -B
               """
             }
          }

          // archive all .jar, .pom, .xml, .txt runtime artifacts + required .war/.zip/.tar.gz for EE pipeline
          // add a new line for each group of artifacts
          archiveArtifacts artifacts: '.m2/org/camunda/**/*-SNAPSHOT/**/*.jar,.m2/org/camunda/**/*-SNAPSHOT/**/*.pom,.m2/org/camunda/**/*-SNAPSHOT/**/*.xml,.m2/org/camunda/**/*-SNAPSHOT/**/*.txt', followSymlinks: false
          archiveArtifacts artifacts: '.m2/org/camunda/**/*-SNAPSHOT/**/camunda-webapp*frontend-sources.zip', followSymlinks: false
          archiveArtifacts artifacts: '.m2/org/camunda/**/*-SNAPSHOT/**/license-book*.zip', followSymlinks: false
          archiveArtifacts artifacts: '.m2/org/camunda/**/*-SNAPSHOT/**/camunda-jboss-modules*.zip', followSymlinks: false
          archiveArtifacts artifacts: '.m2/org/camunda/**/*-SNAPSHOT/**/camunda-*-assembly*.tar.gz', followSymlinks: false
          archiveArtifacts artifacts: '.m2/org/camunda/**/*-SNAPSHOT/**/camunda-webapp*.war', followSymlinks: false
          archiveArtifacts artifacts: '.m2/org/camunda/**/*-SNAPSHOT/**/camunda-engine-rest*.war', followSymlinks: false
          archiveArtifacts artifacts: '.m2/org/camunda/**/*-SNAPSHOT/**/camunda-example-invoice*.war', followSymlinks: false
          archiveArtifacts artifacts: '.m2/org/camunda/**/*-SNAPSHOT/**/camunda-h2-webapp*.war', followSymlinks: false

          stash name: "platform-stash-runtime", includes: ".m2/org/camunda/**/*-SNAPSHOT/**", excludes: "**/qa/**,**/*qa*/**,**/*.zip,**/*.tar.gz"
          stash name: "platform-stash-archives", includes: ".m2/org/camunda/bpm/**/*-SNAPSHOT/**/*.zip,.m2/org/camunda/bpm/**/*-SNAPSHOT/**/*.tar.gz"
          stash name: "platform-stash-qa", includes: ".m2/org/camunda/bpm/**/qa/**/*-SNAPSHOT/**,.m2/org/camunda/bpm/**/*qa*/**/*-SNAPSHOT/**", excludes: "**/*.zip,**/*.tar.gz"

        }
        

        script {
          String labels = '';
          if (env.BRANCH_NAME != cambpmDefaultBranch()) {
            labels = JsonOutput.toJson(pullRequest.labels)
          }

          build job: "cambpm-ee/cambpm-ee-main/${params.EE_BRANCH_NAME}", parameters: [
                  string(name: 'copyArtifactSelector', value: '<TriggeredBuildSelector plugin="copyartifact@1.45.1">  <upstreamFilterStrategy>UseGlobalSetting</upstreamFilterStrategy>  <allowUpstreamDependencies>false</allowUpstreamDependencies></TriggeredBuildSelector>'),
                  booleanParam(name: 'STANDALONE', value: false),
                  string(name: 'CE_BRANCH_NAME', value: "${env.BRANCH_NAME}"),
                  string(name: 'PR_LABELS', value: labels)
          ], quietPeriod: 10, wait: false

          if (withLabels('all-db','cockroachdb','authorizations')) {
           build job: "cambpm-ce/cambpm-sidetrack/${env.BRANCH_NAME}", parameters: [
           string(name: 'copyArtifactSelector', value: '<TriggeredBuildSelector plugin="copyartifact@1.45.1">  <upstreamFilterStrategy>UseGlobalSetting</upstreamFilterStrategy>  <allowUpstreamDependencies>false</allowUpstreamDependencies></TriggeredBuildSelector>'),
               booleanParam(name: 'STANDALONE', value: false),
               string(name: 'PR_LABELS', value: labels)
           ], quietPeriod: 10, wait: false
          }

          if (withLabels('default-build','rolling-update','migration','all-db','h2','db2','mysql','oracle','mariadb','sqlserver','postgresql','cockroachdb','daily')) {
           build job: "cambpm-ce/cambpm-daily/${env.BRANCH_NAME}", parameters: [
               string(name: 'copyArtifactSelector', value: '<TriggeredBuildSelector plugin="copyartifact@1.45.1">  <upstreamFilterStrategy>UseGlobalSetting</upstreamFilterStrategy>  <allowUpstreamDependencies>false</allowUpstreamDependencies></TriggeredBuildSelector>'),
               booleanParam(name: 'STANDALONE', value: false),
               string(name: 'PR_LABELS', value: labels)
           ], quietPeriod: 10, wait: false
          }

          if (env.BRANCH_NAME == 'master') {
            withMaven(jdk: 'jdk-8-latest', maven: 'maven-3.2-latest', mavenSettingsConfig: 'camunda-maven-settings', options: [artifactsPublisher(disabled: true), junitPublisher(disabled: true)]) {
              configFileProvider([configFile(fileId: 'maven-nexus-settings', variable: 'MAVEN_SETTINGS_XML')]) {
               sh 'mvn -s \$MAVEN_SETTINGS_XML org.sonatype.plugins:nexus-staging-maven-plugin:deploy-staged -DaltStagingDirectory=${WORKSPACE}/staging -Dmaven.repo.local=${WORKSPACE}/.m2 -DskipStaging=true -B'
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
              withLabels('h2', 'rolling-update', 'migration')
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
                runMaven(true, false, false, cambpmGetMavenTargetDir('engine-unit'), cambpmGetMavenTargetAndProfile('engine-unit') + cambpmGetDbProfiles('h2'))
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
              withLabels('h2','authorizations')
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
                runMaven(true, false, false, cambpmGetMavenTargetDir('engine-unit-authorizations'), cambpmGetMavenTargetAndProfile('engine-unit-authorizations') + cambpmGetDbProfiles('h2'))
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
        stage('engine-rest-UNIT-jersey-2') {
          when {
            expression {
              withLabels('rest-api')
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
                runMaven(true, false, false, 'engine-rest/engine-rest/', 'clean install -Pjersey2')
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
              withLabels('rest-api')
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
                runMaven(true, false, false, 'engine-rest/engine-rest/', 'clean install -Presteasy3')
              }
            }
          }
          post {
            always {
              cambpmPublishTestResult();
            }
          }
        }
        stage('webapp-UNIT-h2') {
          when {
            expression {
              withLabels('default-build')
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
                runMaven(true, false, false, cambpmGetMavenTargetDir('webapp-unit'), cambpmGetMavenTargetAndProfile('webapp-unit') + cambpmGetDbProfiles('h2'))
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
              withLabels('default-build')
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
                runMaven(true, false, false, cambpmGetMavenTargetDir('webapp-unit-authorizations'), cambpmGetMavenTargetAndProfile('webapp-unit-authorizations') + cambpmGetDbProfiles('h2'))
              }
            }
          }
          post {
            always {
              cambpmPublishTestResult();
            }
            failure {
              cambpmAddFailedStageType(failedStageTypes, 'webapps-unit-authorizations')
            }
          }
        }
        stage('engine-IT-tomcat-9-postgresql-96') {
          when {
            expression {
              withLabels('all-as','tomcat')
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
                runMaven(true, true, false, 'qa/', 'clean install -Ptomcat,postgresql,engine-integration', true)
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
              withLabels('all-as','wildfly')
            }
          }
          agent {
            node {
              label 'postgresql_96'
            }
          }
          steps {
            catchError(stageResult: 'FAILURE') {
              withMaven(jdk: 'jdk-8-latest', maven: 'maven-3.2-latest', mavenSettingsConfig: 'camunda-maven-settings', options: [artifactsPublisher(disabled: true), junitPublisher(disabled: true)]) {
                runMaven(true, true, false, 'qa/', 'clean install -Pwildfly,postgresql,engine-integration', true)
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
              withLabels('webapp-integration', 'h2')
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
                runMaven(true, true, false, 'qa/', 'clean install -Ptomcat,h2,webapps-integration')
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
                runMaven(true, true, false, 'qa/', 'clean install -Pwildfly-vanilla,webapps-integration-sa')
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
              withLabels('run')
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
                runMaven(true, true, true, 'distro/run/', 'clean install -Pintegration-test-camunda-run')
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
              withLabels('spring-boot')
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
                runMaven(true, true, true, 'spring-boot-starter/', 'clean install -Pintegration-test-spring-boot-starter')
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
    stage('UNIT DB tests') {
      matrix {
        axes {
          axis {
            name 'DB'
            values 'postgresql_96', 'postgresql_94', 'postgresql_107'
          }
          axis {
            name 'PROFILE'
            values 'engine-unit', 'engine-unit-authorizations', 'webapp-unit', 'webapp-unit-authorizations'
          }
        }
        when {
          expression {
            cambpmIsNotFailedStageType(failedStageTypes, env.PROFILE) && withLabels(cambpmGetLabels(env.PROFILE, 'cockroachdb'))
          }
          beforeAgent true
        }
        agent {
          node {
            label env.DB
          }
        }
        stages {
          stage('UNIT test') {
            steps {
              echo("UNIT DB Test Stage: ${env.PROFILE}-${env.DB}")
              catchError(stageResult: 'FAILURE') {
                withMaven(jdk: 'jdk-8-latest', maven: 'maven-3.2-latest', mavenSettingsConfig: 'camunda-maven-settings', options: [artifactsPublisher(disabled: true), junitPublisher(disabled: true)]) {
                  runMaven(true, false, false, cambpmGetMavenTargetDir(env.PROFILE), cambpmGetMavenTargetAndProfile(env.PROFILE) + cambpmGetDbProfiles(env.DB) + " " + cambpmGetDbExtras(env.DB), true)
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
                runMaven(true, false, false, 'engine/', 'clean verify -Pcheck-api-compatibility')
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
                runMaven(true, false, false, 'engine/', 'clean test -Pcheck-plugins')
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
              cambpmIsNotFailedStageType(failedStageTypes, 'engine-unit') && withLabels('all-db','h2','db2','mysql','oracle','mariadb','sqlserver','postgresql','cockroachdb') // TODO store as param
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
                runMaven(true, false, false, 'engine/', 'clean test -Pdb-table-prefix')
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
                  runMaven(true, false, false, 'webapps/', 'clean test -Pdb-table-prefix')
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
                runMaven(true, false, false, '.', 'clean verify -Pcheck-engine,wls-compatibility,jersey')
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
              cambpmIsNotFailedStageType(failedStageTypes, 'engine-IT-wildfly') && withLabels('wildfly')
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
                runMaven(true, true, false, 'qa/', 'clean install -Pwildfly-domain,h2,engine-integration')
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
              cambpmIsNotFailedStageType(failedStageTypes, 'engine-IT-wildfly') && withLabels('wildfly')
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
                runMaven(true, true, true, 'qa/', 'clean install -Pwildfly,wildfly-servlet,h2,engine-integration')
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

void runMaven(boolean runtimeStash, boolean archivesStash, boolean qaStash, String directory, String cmd, boolean singleThreaded = true) {
  if (runtimeStash) unstash "platform-stash-runtime"
  if (archivesStash) unstash "platform-stash-archives"
  if (qaStash) unstash "platform-stash-qa"
  String forkCount = singleThreaded? "-DforkCount=1" : '';
  configFileProvider([configFile(fileId: 'maven-nexus-settings', variable: 'MAVEN_SETTINGS_XML')]) {
    sh("mvn -s \$MAVEN_SETTINGS_XML ${forkCount} ${cmd} -nsu -Dmaven.repo.local=\${WORKSPACE}/.m2  -f ${directory}/pom.xml -B")
  }
}

boolean withLabels(List labels) { // TODO
  if (env.BRANCH_NAME != cambpmDefaultBranch() && !pullRequest.labels.contains('no-build')) {
    return false;
  }

  if (env.BRANCH_NAME == cambpmDefaultBranch()) {
    return !labels.contains('daily');
  } else if (changeRequest()) {
    for (l in labels) {
      if (pullRequest.labels.contains(l)) {
        return true;
      }
    }
  }

  return false;
}

boolean withLabels(String... labels) {
  return withLabels(Arrays.asList(labels));
}

boolean withDbLabels(String dbLabel) {
  return withLabels(cambpmGetDbType(dbLabel),'all-db')
}