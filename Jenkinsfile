import groovy.json.JsonOutput

// https://github.com/camunda/jenkins-global-shared-library
@Library('camunda-ci') _

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

pipeline {
  agent none
  options {
    buildDiscarder(logRotator(numToKeepStr: '5')) //, artifactNumToKeepStr: '30'
    copyArtifactPermission('*');
  }
  parameters {
      string defaultValue: 'pipeline-master', description: 'The name of the EE branch to run the EE pipeline on', name: 'EE_BRANCH_NAME'
  }
  stages {
    stage('ASSEMBLY') {
      when {
        expression {
          return !pullRequest.labels.contains('no-build')
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
        
        build job: "cambpm-jenkins-pipelines-ee/${params.EE_BRANCH_NAME}", parameters: [
                string(name: 'copyArtifactSelector', value: '<TriggeredBuildSelector plugin="copyartifact@1.45.1">  <upstreamFilterStrategy>UseGlobalSetting</upstreamFilterStrategy>  <allowUpstreamDependencies>false</allowUpstreamDependencies></TriggeredBuildSelector>'),
                booleanParam(name: 'STANDALONE', value: false),
                string(name: 'CE_BRANCH_NAME', value: "${env.BRANCH_NAME}"),
                string(name: 'PR_LABELS', value: JsonOutput.toJson(pullRequest.labels))
        ], quietPeriod: 10, wait: false

        script {
          if (withLabels('default-build','rolling-update','migration','all-db','h2','db2','mysql','oracle','mariadb','sqlserver','postgresql','cockroachdb')) {
           build job: "cambpm-jenkins-pipelines-daily/${env.BRANCH_NAME}", parameters: [
               string(name: 'copyArtifactSelector', value: '<TriggeredBuildSelector plugin="copyartifact@1.45.1">  <upstreamFilterStrategy>UseGlobalSetting</upstreamFilterStrategy>  <allowUpstreamDependencies>false</allowUpstreamDependencies></TriggeredBuildSelector>'),
               booleanParam(name: 'STANDALONE', value: false),
               string(name: 'PR_LABELS', value: JsonOutput.toJson(pullRequest.labels))
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
              return withLabels('h2')
            }
            beforeAgent true
          }
          agent {
            kubernetes {
              yaml getAgent('gcr.io/ci-30-162810/centos:v0.4.6', 16)
            }
          }
          steps{
            withMaven(jdk: 'jdk-8-latest', maven: 'maven-3.2-latest', mavenSettingsConfig: 'camunda-maven-settings', options: [artifactsPublisher(disabled: true), junitPublisher(disabled: true)]) {
              runMaven(true, false, false, 'engine/', ' test -Pdatabase,h2')
            }
          }
        }
        stage('engine-UNIT-authorizations-h2') {
          when {
            expression {
              return withLabels('h2')
            }
            beforeAgent true
          }
          agent {
            kubernetes {
              yaml getAgent('gcr.io/ci-30-162810/centos:v0.4.6', 16)
            }
          }
          steps{
            withMaven(jdk: 'jdk-8-latest', maven: 'maven-3.2-latest', mavenSettingsConfig: 'camunda-maven-settings', options: [artifactsPublisher(disabled: true), junitPublisher(disabled: true)]) {
              runMaven(true, false, false, 'engine/', 'test -Pdatabase,h2,cfgAuthorizationCheckRevokesAlways')
            }
          }
        }
        stage('engine-rest-UNIT-jersey-2') {
          when {
            expression {
              return withLabels('rest-api')
            }
            beforeAgent true
          }
          agent {
            kubernetes {
              yaml getAgent()
            }
          }
          steps{
            withMaven(jdk: 'jdk-8-latest', maven: 'maven-3.2-latest', mavenSettingsConfig: 'camunda-maven-settings', options: [artifactsPublisher(disabled: true), junitPublisher(disabled: true)]) {
              runMaven(true, false, false, 'engine-rest/engine-rest/', 'clean install -Pjersey2')
            }
          }
        }
        stage('engine-rest-UNIT-resteasy3') {
          when {
            expression {
              return withLabels('rest-api')
            }
            beforeAgent true
          }
          agent {
            kubernetes {
              yaml getAgent()
            }
          }
          steps{
            withMaven(jdk: 'jdk-8-latest', maven: 'maven-3.2-latest', mavenSettingsConfig: 'camunda-maven-settings', options: [artifactsPublisher(disabled: true), junitPublisher(disabled: true)]) {
              runMaven(true, false, false, 'engine-rest/engine-rest/', 'clean install -Presteasy3')
            }
          }
        }
        stage('webapp-UNIT-h2') {
          when {
            expression {
              return withLabels('default-build')
            }
            beforeAgent true
          }
          agent {
            kubernetes {
              yaml getAgent()
            }
          }
          steps{
            withMaven(jdk: 'jdk-8-latest', maven: 'maven-3.2-latest', mavenSettingsConfig: 'camunda-maven-settings', options: [artifactsPublisher(disabled: true), junitPublisher(disabled: true)]) {
              runMaven(true, false, false, 'webapps/', 'clean test -Pdatabase,h2 -Dskip.frontend.build=true')
            }
          }
        }
        stage('engine-IT-tomcat-9-h2') {// TODO change it to `postgresql-96`
          when {
            expression {
              return withLabels('all-as','tomcat')
            }
            beforeAgent true
          }
          agent {
            kubernetes {
              yaml getAgent()
            }
          }
          steps{
            withMaven(jdk: 'jdk-8-latest', maven: 'maven-3.2-latest', mavenSettingsConfig: 'camunda-maven-settings', options: [artifactsPublisher(disabled: true), junitPublisher(disabled: true)]) {
              catchError(buildResult: 'SUCCESS', stageResult: 'FAILURE') {
                runMaven(true, true, false, 'qa/', 'clean install -Ptomcat,h2,engine-integration')
              }
            }
          }
          post {
            always {
              junit testResults: '**/target/*-reports/TEST-*.xml', keepLongStdio: true
            }
          }
        }
        stage('webapp-IT-tomcat-9-h2') {
          when {
            expression {
              return withLabels('webapp-integration', 'h2')
            }
            beforeAgent true
          }
          agent {
            kubernetes {
              yaml getAgent('gcr.io/ci-30-162810/chrome:78v0.1.2')
            }
          }
          steps{
            withMaven(jdk: 'jdk-8-latest', maven: 'maven-3.2-latest', mavenSettingsConfig: 'camunda-maven-settings', options: [artifactsPublisher(disabled: true), junitPublisher(disabled: true)]) {
              catchError(buildResult: 'SUCCESS', stageResult: 'FAILURE') {
                runMaven(true, true, false, 'qa/', 'clean install -Ptomcat,h2,webapps-integration')
              }
            }
          }
          post {
            always {
              junit testResults: '**/target/*-reports/TEST-*.xml', keepLongStdio: true
            }
          }
        }
        stage('webapp-IT-standalone-wildfly') {
          when {
            branch 'pipeline-master';
            beforeAgent true
          }
          agent {
            kubernetes {
              yaml getAgent('gcr.io/ci-30-162810/chrome:78v0.1.2')
            }
          }
          steps{
            withMaven(jdk: 'jdk-8-latest', maven: 'maven-3.2-latest', mavenSettingsConfig: 'camunda-maven-settings', options: [artifactsPublisher(disabled: true), junitPublisher(disabled: true)]) {
              catchError(buildResult: 'SUCCESS', stageResult: 'FAILURE') {
                runMaven(true, true, false, 'qa/', 'clean install -Pwildfly-vanilla,webapps-integration-sa')
              }
            }
          }
        }
        stage('camunda-run-IT') {
          when {
            expression {
              return withLabels('run')
            }
            beforeAgent true
          }
          agent {
            kubernetes {
              yaml getAgent('gcr.io/ci-30-162810/chrome:78v0.1.2', 16)
            }
          }
          steps{
            withMaven(jdk: 'jdk-8-latest', maven: 'maven-3.2-latest', mavenSettingsConfig: 'camunda-maven-settings', options: [artifactsPublisher(disabled: true), junitPublisher(disabled: true)]) {
              catchError(buildResult: 'SUCCESS', stageResult: 'FAILURE') {
                runMaven(true, true, true, 'distro/run/', 'clean install -Pintegration-test-camunda-run')
              }
            }
          }
          post {
            always {
              junit testResults: '**/target/*-reports/TEST-*.xml', keepLongStdio: true
            }
          }
        }
        stage('spring-boot-starter-IT') {
          when {
            expression {
              return withLabels('spring-boot')
            }
            beforeAgent true
          }
          agent {
            kubernetes {
              yaml getAgent('gcr.io/ci-30-162810/chrome:78v0.1.2', 16)
            }
          }
          steps{
            withMaven(jdk: 'jdk-8-latest', maven: 'maven-3.2-latest', mavenSettingsConfig: 'camunda-maven-settings', options: [artifactsPublisher(disabled: true), junitPublisher(disabled: true)]) {
              catchError(buildResult: 'SUCCESS', stageResult: 'FAILURE') {
                runMaven(true, true, true, 'spring-boot-starter/', 'clean install -Pintegration-test-spring-boot-starter')
              }
            }
          }
          post {
            always {
              junit testResults: '**/target/*-reports/TEST-*.xml', keepLongStdio: true
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
        }
        when {
          expression {
            return withLabels("all-db") || withDbLabel(env.DB)
          }
          beforeAgent true
        }
        agent {
          kubernetes {
            yaml getDbAgent(env.DB)
          }
        }
        stages {
          stage("engine-UNIT") {
            steps {
              withMaven(jdk: 'jdk-8-latest', maven: 'maven-3.2-latest', mavenSettingsConfig: 'camunda-maven-settings', options: [artifactsPublisher(disabled: true), junitPublisher(disabled: true)]) {
                runMaven(true, false, false, 'engine/', 'clean test -P' + getDbProfiles(env.DB) + " " + getDbExtras(env.DB), true)
              }
            }
            post {
              always {
                junit testResults: '**/target/*-reports/TEST-*.xml', keepLongStdio: true
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
            branch 'pipeline-master';
            beforeAgent true
          }
          agent {
            kubernetes {
              yaml getAgent('gcr.io/ci-30-162810/centos:v0.4.6', 16)
            }
          }
          steps{
            withMaven(jdk: 'jdk-8-latest', maven: 'maven-3.2-latest', mavenSettingsConfig: 'camunda-maven-settings', options: [artifactsPublisher(disabled: true), junitPublisher(disabled: true)]) {
              runMaven(true, false, false, 'engine/', 'clean verify -Pcheck-api-compatibility')
            }
          }
        }
        stage('engine-UNIT-plugins') {
          when {
            branch 'pipeline-master';
            beforeAgent true
          }
          agent {
            kubernetes {
              yaml getAgent('gcr.io/ci-30-162810/centos:v0.4.6', 16)
            }
          }
          steps{
            withMaven(jdk: 'jdk-8-latest', maven: 'maven-3.2-latest', mavenSettingsConfig: 'camunda-maven-settings', options: [artifactsPublisher(disabled: true), junitPublisher(disabled: true)]) {
              runMaven(true, false, false, 'engine/', 'clean test -Pcheck-plugins')
            }
          }
        }
        stage('engine-UNIT-database-table-prefix') {
          when {
            expression {
              return withLabels('all-db','h2','db2','mysql','oracle','mariadb','sqlserver','postgresql','cockroachdb') // TODO store as param
            }
            beforeAgent true
          }
          agent {
            kubernetes {
              yaml getAgent()
            }
          }
          steps{
            withMaven(jdk: 'jdk-8-latest', maven: 'maven-3.2-latest', mavenSettingsConfig: 'camunda-maven-settings', options: [artifactsPublisher(disabled: true), junitPublisher(disabled: true)]) {
              runMaven(true, false, false, 'engine/', 'clean test -Pdb-table-prefix')
            }
          }
        }
        stage('webapp-UNIT-database-table-prefix') {
          when {
            branch 'pipeline-master';
            beforeAgent true
          }
          agent {
            kubernetes {
              yaml getAgent()
            }
          }
          steps{
            withMaven(jdk: 'jdk-8-latest', maven: 'maven-3.2-latest', mavenSettingsConfig: 'camunda-maven-settings', options: [artifactsPublisher(disabled: true), junitPublisher(disabled: true)]) {
              nodejs('nodejs-14.6.0'){
                runMaven(true, false, false, 'webapps/', 'clean test -Pdb-table-prefix')
              }
            }
          }
        }
        stage('engine-UNIT-wls-compatibility') {
          when {
            branch 'pipeline-master';
            beforeAgent true
          }
          agent {
            kubernetes {
              yaml getAgent()
            }
          }
          steps{
            withMaven(jdk: 'jdk-8-latest', maven: 'maven-3.2-latest', mavenSettingsConfig: 'camunda-maven-settings', options: [artifactsPublisher(disabled: true), junitPublisher(disabled: true)]) {
              runMaven(true, false, false, '.', 'clean verify -Pcheck-engine,wls-compatibility,jersey')
            }
          }
        }
        stage('IT-wildfly-domain') {
          when {
            expression {
              return withLabels('wildfly')
            }
            beforeAgent true
          }
          agent {
            kubernetes {
              yaml getAgent()
            }
          }
          steps{
            withMaven(jdk: 'jdk-8-latest', maven: 'maven-3.2-latest', mavenSettingsConfig: 'camunda-maven-settings', options: [artifactsPublisher(disabled: true), junitPublisher(disabled: true)]) {
              runMaven(true, true, false, 'qa/', 'clean install -Pwildfly-domain,h2,engine-integration')
            }
          }
        }
        stage('IT-wildfly-servlet') {
          when {
            expression {
              return withLabels('wildfly')
            }
            beforeAgent true
          }
          agent {
            kubernetes {
              yaml getAgent()
            }
          }
          steps{
            withMaven(jdk: 'jdk-8-latest', maven: 'maven-3.2-latest', mavenSettingsConfig: 'camunda-maven-settings', options: [artifactsPublisher(disabled: true), junitPublisher(disabled: true)]) {
              runMaven(true, true, true, 'qa/', 'clean install -Pwildfly,wildfly-servlet,h2,engine-integration')
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


void runMaven(boolean runtimeStash, boolean archivesStash, boolean qaStash, String directory, String cmd, boolean singleThreaded = false) {
  if (runtimeStash) unstash "platform-stash-runtime"
  if (archivesStash) unstash "platform-stash-archives"
  if (qaStash) unstash "platform-stash-qa"
  String forkCount = singleThreaded? "-DforkCount=1" : '';
  configFileProvider([configFile(fileId: 'maven-nexus-settings', variable: 'MAVEN_SETTINGS_XML')]) {
    sh("mvn -s \$MAVEN_SETTINGS_XML ${forkCount} ${cmd} -nsu -Dmaven.repo.local=\${WORKSPACE}/.m2  -f ${directory}/pom.xml -B")
  }
}

boolean withLabels(String... labels) {
  if (pullRequest.labels.contains('no-build')) {
    return false;
  }

  if (env.BRANCH == 'pipeline-master') {
    return true;
  } else if (changeRequest()) {
    for (l in labels) {
      if (pullRequest.labels.contains(l)) {
        return true;  
      }
    }
  }

  return false;
}

void withDbLabel(String dbLabel) {
  withLabels(getDbType(dbLabel))
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
  Map SUPPORTED_DBS = ['postgresql_96': [
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
          command: 'clean test -P'],
      'engine-unit-authorizations': [
          directory: 'engine/',
          command: 'clean test -PcfgAuthorizationCheckRevokesAlways,'],
      'webapps-unit': [
          directory: 'webapps/',
          command: 'clean test -Dskip.frontend.build=true -P'],
      'webapps-unit-authorizations': [
          directory: 'webapps/',
          command: 'clean test -Dskip.frontend.build=true -PcfgAuthorizationCheckRevokesAlways,']
  ]

  return PROFILE_PATHS[profile]
}

String getMavenProfileCmd(String profile) {
  return resolveMavenProfileInfo(profile).command
}

String getMavenProfileDir(String profile) {
  return resolveMavenProfileInfo(profile).directory
}