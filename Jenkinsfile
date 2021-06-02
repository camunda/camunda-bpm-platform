// https://github.com/camunda/jenkins-global-shared-library
// https://github.com/camunda/cambpm-jenkins-shared-library
@Library(['camunda-ci', 'cambpm-jenkins-shared-library']) _

pipeline {
  agent {
    node {
      label 'jenkins-job-runner'
    }
  }
  parameters {
    string name: 'EE_DOWNSTREAM', defaultValue: 'cambpm-ee-main-pr/' + cambpmDefaultBranch(), description: 'The name of the EE branch/PR to run the EE pipeline on, e.g. cambpm-ee-main/PR-333'
  }
  options {
    buildDiscarder(logRotator(numToKeepStr: '5'))
    copyArtifactPermission('*')
  }
  stages {
    stage('ASSEMBLY') {
      agent {
        node {
          label 'h2_perf32'
        }
      }
      steps {
        withMaven(jdk: 'jdk-8-latest', maven: 'maven-3.2-latest', mavenSettingsConfig: 'camunda-maven-settings', publisherStrategy: 'EXPLICIT') {
          nodejs('nodejs-14.6.0'){
             configFileProvider([configFile(fileId: 'maven-nexus-settings', variable: 'MAVEN_SETTINGS_XML')]) {
               sh("mvn -s \$MAVEN_SETTINGS_XML clean source:jar deploy source:test-jar com.mycila:license-maven-plugin:check -Pdistro,distro-ce,distro-wildfly,distro-webjar -DaltStagingDirectory=\${WORKSPACE}/staging -DskipRemoteStaging=true -nsu -Dmaven.repo.local=\${WORKSPACE}/.m2 -f ./pom.xml -B -DskipTests")
             }
          }

          // archive all .jar, .pom, .xml, .txt runtime artifacts + required .war/.zip/.tar.gz
          archiveArtifacts artifacts: '.m2/org/camunda/**/*-SNAPSHOT/**/*.jar,.m2/org/camunda/**/*-SNAPSHOT/**/*.pom,.m2/org/camunda/**/*-SNAPSHOT/**/*.xml,.m2/org/camunda/**/*-SNAPSHOT/**/*.txt', followSymlinks: false
          archiveArtifacts artifacts: '.m2/org/camunda/**/*-SNAPSHOT/**/camunda-webapp*frontend-sources.zip', followSymlinks: false
          archiveArtifacts artifacts: '.m2/org/camunda/**/*-SNAPSHOT/**/license-book*.zip', followSymlinks: false
          archiveArtifacts artifacts: '.m2/org/camunda/**/*-SNAPSHOT/**/camunda-jboss-modules*.zip', followSymlinks: false
          archiveArtifacts artifacts: '.m2/org/camunda/**/*-SNAPSHOT/**/camunda-*-assembly*.tar.gz', followSymlinks: false
          archiveArtifacts artifacts: '.m2/org/camunda/**/*-SNAPSHOT/**/camunda-webapp*.war', followSymlinks: false
          archiveArtifacts artifacts: '.m2/org/camunda/**/*-SNAPSHOT/**/camunda-engine-rest*.war', followSymlinks: false
          archiveArtifacts artifacts: '.m2/org/camunda/**/*-SNAPSHOT/**/camunda-example-invoice*.war', followSymlinks: false
          archiveArtifacts artifacts: '.m2/org/camunda/**/*-SNAPSHOT/**/camunda-h2-webapp*.war', followSymlinks: false
          archiveArtifacts artifacts: '.m2/org/camunda/**/*-SNAPSHOT/**/camunda-bpm-run-modules-swaggerui-*-run-swaggerui-license-book-json.json', followSymlinks: false

          stash name: "platform-stash-runtime", includes: ".m2/org/camunda/**/*-SNAPSHOT/**", excludes: "**/qa/**,**/*qa*/**,**/*.zip,**/*.tar.gz"
          stash name: "platform-stash-archives", includes: ".m2/org/camunda/bpm/**/*-SNAPSHOT/**/*.zip,.m2/org/camunda/bpm/**/*-SNAPSHOT/**/*.tar.gz"
          stash name: "platform-stash-qa", includes: ".m2/org/camunda/bpm/**/qa/**/*-SNAPSHOT/**,.m2/org/camunda/bpm/**/*qa*/**/*-SNAPSHOT/**", excludes: "**/*.zip,**/*.tar.gz"
        }

        // triggers main-EE
        // ...

        // deploy snapshot artifacts
        //withMaven(jdk: 'jdk-8-latest', maven: 'maven-3.2-latest', mavenSettingsConfig: 'camunda-maven-settings', publisherStrategy: 'EXPLICIT') {
        //  configFileProvider([configFile(fileId: 'maven-nexus-settings', variable: 'MAVEN_SETTINGS_XML')]) {
        //   sh("mvn -s \$MAVEN_SETTINGS_XML org.sonatype.plugins:nexus-staging-maven-plugin:deploy-staged -DaltStagingDirectory=${WORKSPACE}/staging -Dmaven.repo.local=${WORKSPACE}/.m2 -DskipStaging=true -B")
        //  }
        //}
      }
      //post {
      //  always {
      //    junit testResults: '**/target/*-reports/TEST-*.xml', allowEmptyResults: false, skipPublishingChecks: true
      //  }
      //}
    }
    stage('REST UNIT') {
      parallel {
        stage('engine-rest-unit-resteasy') {
          agent {
            node {
              label 'h2'
            }
          }
          steps {
            unstash "platform-stash-runtime"
            withMaven(jdk: 'jdk-8-latest', maven: 'maven-3.2-latest', mavenSettingsConfig: 'camunda-maven-settings', publisherStrategy: 'EXPLICIT') {
              configFileProvider([configFile(fileId: 'maven-nexus-settings', variable: 'MAVEN_SETTINGS_XML')]) {
                sh("mvn -s \$MAVEN_SETTINGS_XML clean install -Presteasy -nsu -Dmaven.repo.local=\${WORKSPACE}/.m2 -f engine-rest/engine-rest/pom.xml -B")
              }
            }
          }
          post {
            always {
              junit testResults: '**/target/*-reports/TEST-*.xml', allowEmptyResults: false, skipPublishingChecks: true
            }
          }
        }
        stage('engine-rest-unit-resteasy3') {
          agent {
            node {
              label 'h2'
            }
          }
          steps {
            unstash "platform-stash-runtime"
            withMaven(jdk: 'jdk-8-latest', maven: 'maven-3.2-latest', mavenSettingsConfig: 'camunda-maven-settings', publisherStrategy: 'EXPLICIT') {
              configFileProvider([configFile(fileId: 'maven-nexus-settings', variable: 'MAVEN_SETTINGS_XML')]) {
                sh("mvn -s \$MAVEN_SETTINGS_XML clean install -Presteasy -nsu -Dmaven.repo.local=\${WORKSPACE}/.m2 -f engine-rest/engine-rest/pom.xml -B")
              }
            }
          }
          post {
            always {
              junit testResults: '**/target/*-reports/TEST-*.xml', allowEmptyResults: false, skipPublishingChecks: true
            }
          }
        }
      }
    }
    stage('UNIT history level') {
      parallel {
        stage('engine-UNIT-historylevel-none') {
          agent {
            node {
              label 'h2'
            }
          }
          steps {
            unstash "platform-stash-runtime"
            withMaven(jdk: 'jdk-8-latest', maven: 'maven-3.2-latest', mavenSettingsConfig: 'camunda-maven-settings', publisherStrategy: 'EXPLICIT') {
              configFileProvider([configFile(fileId: 'maven-nexus-settings', variable: 'MAVEN_SETTINGS_XML')]) {
                sh("mvn -s \$MAVEN_SETTINGS_XML verify -Pcfghistorynone -nsu -Dmaven.repo.local=\${WORKSPACE}/.m2 -f engine/pom.xml -B")
              }
            }
          }
          post {
            always {
              junit testResults: '**/target/*-reports/TEST-*.xml', allowEmptyResults: false, skipPublishingChecks: true
            }
          }
        }
        stage('engine-UNIT-historylevel-audit') {
          agent {
            node {
              label 'h2'
            }
          }
          steps {
            unstash "platform-stash-runtime"
            withMaven(jdk: 'jdk-8-latest', maven: 'maven-3.2-latest', mavenSettingsConfig: 'camunda-maven-settings', publisherStrategy: 'EXPLICIT') {
              configFileProvider([configFile(fileId: 'maven-nexus-settings', variable: 'MAVEN_SETTINGS_XML')]) {
                sh("mvn -s \$MAVEN_SETTINGS_XML verify -Pcfghistoryaudit -nsu -Dmaven.repo.local=\${WORKSPACE}/.m2 -f engine/pom.xml -B")
              }
            }
          }
          post {
            always {
              junit testResults: '**/target/*-reports/TEST-*.xml', allowEmptyResults: false, skipPublishingChecks: true
            }
          }
        }
      }
    }
  }
}
