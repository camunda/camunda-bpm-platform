import groovy.json.JsonOutput

// https://github.com/camunda/jenkins-global-shared-library
// https://github.com/camunda/cambpm-jenkins-shared-library
@Library(['camunda-ci', 'cambpm-jenkins-shared-library']) _

String getAgent(String dockerImage = 'gcr.io/ci-30-162810/centos:v0.4.6', Integer cpuLimit = 4){
  String mavenForkCount = cpuLimit;
  String mavenMemoryLimit = cpuLimit * 2;
  """
metadata:
  labels:
    agent: ci-cambpm-camunda-cloud-build
spec:
  nodeSelector:
    cloud.google.com/gke-nodepool: agents-n1-standard-32-netssd-preempt
  tolerations:
  - key: "agents-n1-standard-32-netssd-preempt"
    operator: "Exists"
    effect: "NoSchedule"
  containers:
  - name: "jnlp"
    image: "${dockerImage}"
    args: ['\$(JENKINS_SECRET)', '\$(JENKINS_NAME)']
    tty: true
    env:
    - name: LIMITS_CPU
      value: ${mavenForkCount}
    - name: TZ
      value: Europe/Berlin
    resources:
      limits:
        cpu: ${cpuLimit}
        memory: ${mavenMemoryLimit}Gi
      requests:
        cpu: ${cpuLimit}
        memory: ${mavenMemoryLimit}Gi
    workingDir: "/home/work"
    volumeMounts:
      - mountPath: /home/work
        name: workspace-volume
  """
}

def failedStageTypes = []

pipeline {
  agent none
  options {
    buildDiscarder(logRotator(numToKeepStr: '5')) //, artifactNumToKeepStr: '30'
    copyArtifactPermission('*');
  }
  parameters {
      string defaultValue: defaultBranch(), description: 'The name of the EE branch to run the EE pipeline on', name: 'EE_BRANCH_NAME'
  }
  stages {
    stage('ASSEMBLY') {
      when {
        expression {
          env.BRANCH_NAME == defaultBranch() || !pullRequest.labels.contains('no-build')
        }
        beforeAgent true
      }
      agent {
        kubernetes {
          yaml getAgent('gcr.io/ci-30-162810/centos:v0.4.6', 16)
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
          if (env.BRANCH_NAME != defaultBranch()) {
            labels = JsonOutput.toJson(pullRequest.labels)
          }

          build job: "cambpm-jenkins-pipelines-ee/${params.EE_BRANCH_NAME}", parameters: [
                  string(name: 'copyArtifactSelector', value: '<TriggeredBuildSelector plugin="copyartifact@1.45.1">  <upstreamFilterStrategy>UseGlobalSetting</upstreamFilterStrategy>  <allowUpstreamDependencies>false</allowUpstreamDependencies></TriggeredBuildSelector>'),
                  booleanParam(name: 'STANDALONE', value: false),
                  string(name: 'CE_BRANCH_NAME', value: "${env.BRANCH_NAME}"),
                  string(name: 'PR_LABELS', value: labels)
          ], quietPeriod: 10, wait: false

          if (withLabels('all-db','cockroachdb','authorizations')) {
           build job: "cambpm-jenkins-pipelines-sidetrack/${env.BRANCH_NAME}", parameters: [
           string(name: 'copyArtifactSelector', value: '<TriggeredBuildSelector plugin="copyartifact@1.45.1">  <upstreamFilterStrategy>UseGlobalSetting</upstreamFilterStrategy>  <allowUpstreamDependencies>false</allowUpstreamDependencies></TriggeredBuildSelector>'),
               booleanParam(name: 'STANDALONE', value: false),
               string(name: 'PR_LABELS', value: labels)
           ], quietPeriod: 10, wait: false
          }

          if (withLabels('default-build','rolling-update','migration','all-db','h2','db2','mysql','oracle','mariadb','sqlserver','postgresql','cockroachdb','daily')) {
           build job: "cambpm-jenkins-pipelines-daily/${env.BRANCH_NAME}", parameters: [
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
            kubernetes {
              yaml getAgent('gcr.io/ci-30-162810/centos:v0.4.6', 16)
            }
          }
          steps {
            catchError(stageResult: 'FAILURE') {
              withMaven(jdk: 'jdk-8-latest', maven: 'maven-3.2-latest', mavenSettingsConfig: 'camunda-maven-settings', options: [artifactsPublisher(disabled: true), junitPublisher(disabled: true)]) {
                runMaven(true, false, false, getMavenProfileDir('engine-unit'), getMavenProfileCmd('engine-unit') + getDbProfiles('h2'))
              }
            }
          }
          post {
            always {
              cambpmPublishTestResult();
            }
            failure {
              addFailedStageType(failedStageTypes, 'engine-unit')
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
            kubernetes {
              yaml getAgent('gcr.io/ci-30-162810/centos:v0.4.6', 16)
            }
          }
          steps {
            catchError(stageResult: 'FAILURE') {
              withMaven(jdk: 'jdk-8-latest', maven: 'maven-3.2-latest', mavenSettingsConfig: 'camunda-maven-settings', options: [artifactsPublisher(disabled: true), junitPublisher(disabled: true)]) {
                runMaven(true, false, false, getMavenProfileDir('engine-unit-authorizations'), getMavenProfileCmd('engine-unit-authorizations') + getDbProfiles('h2'))
              }
            }
          }
          post {
            always {
              cambpmPublishTestResult();
            }
            failure {
              addFailedStageType(failedStageTypes, 'engine-unit-authorizations')
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
            kubernetes {
              yaml getAgent()
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
            kubernetes {
              yaml getAgent()
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
            kubernetes {
              yaml getAgent()
            }
          }
          steps {
            catchError(stageResult: 'FAILURE') {
              withMaven(jdk: 'jdk-8-latest', maven: 'maven-3.2-latest', mavenSettingsConfig: 'camunda-maven-settings', options: [artifactsPublisher(disabled: true), junitPublisher(disabled: true)]) {
                runMaven(true, false, false, getMavenProfileDir('webapps-unit'), getMavenProfileCmd('webapps-unit') + getDbProfiles('h2'))
              }
            }
          }
          post {
            always {
              cambpmPublishTestResult();
            }
            failure {
              addFailedStageType(failedStageTypes, 'webapps-unit')
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
            kubernetes {
              yaml getAgent()
            }
          }
          steps {
            catchError(stageResult: 'FAILURE') {
              withMaven(jdk: 'jdk-8-latest', maven: 'maven-3.2-latest', mavenSettingsConfig: 'camunda-maven-settings', options: [artifactsPublisher(disabled: true), junitPublisher(disabled: true)]) {
                runMaven(true, false, false, getMavenProfileDir('webapps-unit-authorizations'), getMavenProfileCmd('webapps-unit-authorizations') + getDbProfiles('h2'))
              }
            }
          }
          post {
            always {
              cambpmPublishTestResult();
            }
            failure {
              addFailedStageType(failedStageTypes, 'webapps-unit-authorizations')
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
            kubernetes {
              yaml getDbAgent('postgresql_96')
            }
          }
          steps {
            catchError(stageResult: 'FAILURE') {
              withMaven(jdk: 'jdk-8-latest', maven: 'maven-3.2-latest', mavenSettingsConfig: 'camunda-maven-settings', options: [artifactsPublisher(disabled: true), junitPublisher(disabled: true)]) {
                runMaven(true, true, false, 'qa/', 'clean install -Ptomcat,postgresql,engine-integration')
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
            kubernetes {
              yaml getDbAgent('postgresql_96')
            }
          }
          steps {
            catchError(stageResult: 'FAILURE') {
              withMaven(jdk: 'jdk-8-latest', maven: 'maven-3.2-latest', mavenSettingsConfig: 'camunda-maven-settings', options: [artifactsPublisher(disabled: true), junitPublisher(disabled: true)]) {
                runMaven(true, true, false, 'qa/', 'clean install -Pwildfly,postgresql,engine-integration')
              }
            }
          }
          post {
            always {
              cambpmPublishTestResult();
            }
            failure {
              addFailedStageType(failedStageTypes, 'engine-IT-wildfly')
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
            kubernetes {
              yaml getAgent('gcr.io/ci-30-162810/chrome:78v0.1.2')
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
            branch defaultBranch();
            beforeAgent true
          }
          agent {
            kubernetes {
              yaml getAgent('gcr.io/ci-30-162810/chrome:78v0.1.2')
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
            kubernetes {
              yaml getAgent('gcr.io/ci-30-162810/chrome:78v0.1.2', 16)
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
            kubernetes {
              yaml getAgent('gcr.io/ci-30-162810/chrome:78v0.1.2', 16)
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
            values 'postgresql_96', 'mariadb_103'
          }
          axis {
            name 'PROFILE'
            values 'engine-unit', 'engine-unit-authorizations', 'webapps-unit', 'webapps-unit-authorizations'
          }
        }
        when {
          expression {
            skipStageType(failedStageTypes, env.PROFILE) && (withLabels(getLabels(env.PROFILE)) || withDbLabels(env.DB))
          }
          beforeAgent true
        }
        agent {
          kubernetes {
            yaml getDbAgent(env.DB)
          }
        }
        stages {
          stage('UNIT test') {
            steps {
              echo("UNIT DB Test Stage: ${env.PROFILE}-${env.DB}")
              catchError(stageResult: 'FAILURE') {
                withMaven(jdk: 'jdk-8-latest', maven: 'maven-3.2-latest', mavenSettingsConfig: 'camunda-maven-settings', options: [artifactsPublisher(disabled: true), junitPublisher(disabled: true)]) {
                  runMaven(true, false, false, getMavenProfileDir(env.PROFILE), getMavenProfileCmd(env.PROFILE) + getDbProfiles(env.DB) + " " + getDbExtras(env.DB), true)
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
                skipStageType(failedStageTypes, 'engine-unit')
              }
              branch defaultBranch();
            }
            beforeAgent true
          }
          agent {
            kubernetes {
              yaml getAgent('gcr.io/ci-30-162810/centos:v0.4.6', 16)
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
                skipStageType(failedStageTypes, 'engine-unit')
              }
              branch defaultBranch();
            }
            beforeAgent true
          }
          agent {
            kubernetes {
              yaml getAgent('gcr.io/ci-30-162810/centos:v0.4.6', 16)
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
              skipStageType(failedStageTypes, 'engine-unit') && withLabels('all-db','h2','db2','mysql','oracle','mariadb','sqlserver','postgresql','cockroachdb') // TODO store as param
            }
            beforeAgent true
          }
          agent {
            kubernetes {
              yaml getAgent()
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
                skipStageType(failedStageTypes, 'webapps-unit')
              }
              branch defaultBranch();
            }
            beforeAgent true
          }
          agent {
            kubernetes {
              yaml getAgent()
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
                skipStageType(failedStageTypes, 'engine-unit')
              }
              branch defaultBranch();
            }
            beforeAgent true
          }
          agent {
            kubernetes {
              yaml getAgent()
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
              skipStageType(failedStageTypes, 'engine-IT-wildfly') && withLabels('wildfly')
            }
            beforeAgent true
          }
          agent {
            kubernetes {
              yaml getAgent()
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
              skipStageType(failedStageTypes, 'engine-IT-wildfly') && withLabels('wildfly')
            }
            beforeAgent true
          }
          agent {
            kubernetes {
              yaml getAgent()
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

String defaultBranch() {
  return 'pipeline-master'; // TODO
//  return 'master';
}

void runMaven(boolean runtimeStash, boolean archivesStash, boolean qaStash, String directory, String cmd, boolean singleThreaded = false) {
  if (runtimeStash) unstash "platform-stash-runtime"
  if (archivesStash) unstash "platform-stash-archives"
  if (qaStash) unstash "platform-stash-qa"
  String forkCount = singleThreaded? "-DforkCount=1" : '';
  configFileProvider([configFile(fileId: 'maven-nexus-settings', variable: 'MAVEN_SETTINGS_XML')]) {
    sh("mvn -s \$MAVEN_SETTINGS_XML ${forkCount} ${cmd} -nsu -Dmaven.repo.local=\${WORKSPACE}/.m2  -f ${directory}/pom.xml -B")
  }
}

boolean withLabels(List labels) { // TODO
  if (env.BRANCH_NAME != defaultBranch() && !pullRequest.labels.contains('no-build')) {
    return false;
  }

  if (env.BRANCH_NAME == defaultBranch()) {
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
  return withLabels(getDbType(dbLabel),'all-db')
}


String getDbAgent(String dbLabel, Integer cpuLimit = 4, Integer mavenForkCount = 1){
  Map dbInfo = getDbInfo(dbLabel)
  String mavenMemoryLimit = cpuLimit * 4;
  """
metadata:
  labels:
    name: "${dbLabel}"
    jenkins: "slave"
    jenkins/label: "jenkins-slave-${dbInfo.type}"
spec:
  containers:
  - name: "jnlp"
    image: "gcr.io/ci-30-162810/${dbInfo.type}:${dbInfo.version}"
    args: ['\$(JENKINS_SECRET)', '\$(JENKINS_NAME)']
    tty: true
    env:
    - name: LIMITS_CPU
      value: ${mavenForkCount}
    - name: TZ
      value: Europe/Berlin
    resources:
      limits:
        memory: ${mavenMemoryLimit}Gi
      requests:
        cpu: ${cpuLimit}
        memory: ${mavenMemoryLimit}Gi
    volumeMounts:
    - mountPath: "/home/work"
      name: "workspace-volume"
    workingDir: "/home/work"
    nodeSelector:
      cloud.google.com/gke-nodepool: "agents-n1-standard-4-netssd-preempt"
    restartPolicy: "Never"
    tolerations:
    - effect: "NoSchedule"
      key: "agents-n1-standard-4-netssd-preempt"
      operator: "Exists"
    volumes:
    - emptyDir:
        medium: ""
      name: "workspace-volume"
  """
}

Map getDbInfo(String databaseLabel) {
  Map SUPPORTED_DBS = [
    'h2': [
      type: 'h2',
      version: '',
      profiles: 'h2',
      extra: ''],
    'postgresql_96': [
      type: 'postgresql',
      version: '9.6v0.2.2',
      profiles: 'postgresql',
      extra: ''],
    'mariadb_103': [
      type: 'mariadb',
      version: '10.3v0.3.2',
      profiles: 'mariadb',
      extra: ''],
    'sqlserver_2017': [
      type: 'mssql',
      version: '2017v0.1.1',
      profiles: 'sqlserver',
      extra: '-Ddatabase.name=camunda -Ddatabase.username=sa -Ddatabase.password=cam_123$']
  ]

  return SUPPORTED_DBS[databaseLabel]
}



String getDbType(String dbLabel) {
  String[] database = dbLabel.split("_")
  return database[0]
}

String getDbProfiles(String dbLabel) {
  return getDbInfo(dbLabel).profiles
}

String getDbExtras(String dbLabel) {
  return getDbInfo(dbLabel).extra
}

String resolveMavenProfileInfo(String profile) {
  Map PROFILE_PATHS = [
      'engine-unit': [
          directory: 'engine/',
          command: 'clean test -P',
          labels: ['authorizations']],
      'engine-unit-authorizations': [
          directory: 'engine/',
          command: 'clean test -PcfgAuthorizationCheckRevokesAlways,',
          labels: ['authorizations']],
      'webapps-unit': [
          directory: 'webapps/',
          command: 'clean test -Dskip.frontend.build=true -P',
          labels: ['default-build']],
      'webapps-unit-authorizations': [
          directory: 'webapps/',
          command: 'clean test -Dskip.frontend.build=true -PcfgAuthorizationCheckRevokesAlways,',
          labels: ['default-build']]
  ]

  return PROFILE_PATHS[profile]
}

String getMavenProfileCmd(String profile) {
  return resolveMavenProfileInfo(profile).command
}

String getMavenProfileDir(String profile) {
  return resolveMavenProfileInfo(profile).directory
}

String[] getLabels(String profile) {
  return resolveMavenProfileInfo(profile).labels
}


void addFailedStageType(List failedStageTypesList, String stageType) {
  if (!failedStageTypesList.contains(stageType)) failedStageTypesList << stageType
}

boolean skipStageType(List failedStageTypesList, String stageType) {
  !failedStageTypesList.contains(stageType)
}