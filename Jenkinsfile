#!/usr/bin/env groovy

String getMavenAgent(Integer mavenCpuLimit = 3){
  // assuming one core left for main maven thread
  String mavenForkCount = mavenCpuLimit - 1;
  // assuming 2Gig for each core
  String mavenMemoryLimit = mavenCpuLimit * 2;
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
  - name: maven
    image: maven:3.6.3-openjdk-8
    command: ["cat"]
    tty: true
    env:
    - name: LIMITS_CPU
      value: ${mavenForkCount}
    - name: TZ
      value: Europe/Berlin
    resources:
      limits:
        cpu: ${mavenCpuLimit}
        memory: ${mavenMemoryLimit}Gi
      requests:
        cpu: ${mavenCpuLimit}
        memory: ${mavenMemoryLimit}Gi
  """
}

pipeline{
  agent none
  stages{
    stage("Compile all the things!"){
      agent {
        kubernetes {
          yaml getMavenAgent()
        }
      }
      steps{
        container("maven"){
          // Install dependencies
          sh '''
            curl -s -O https://deb.nodesource.com/node_14.x/pool/main/n/nodejs/nodejs_14.6.0-1nodesource1_amd64.deb
            dpkg -i nodejs_14.6.0-1nodesource1_amd64.deb
            npm set unsafe-perm true
            apt -qq update && apt install -y g++ make
          '''
          // Run maven
          configFileProvider([configFile(fileId: 'maven-nexus-settings', variable: 'MAVEN_SETTINGS_XML')]) {
            sh """
              mvn -s \$MAVEN_SETTINGS_XML -B -T\$LIMITS_CPU clean source:jar install -D skipTests -Dmaven.repo.local=\$(pwd)/.m2
            """
          }
          stash name: "artifactStash", includes: ".m2/org/camunda/**/*-SNAPSHOT/**", excludes: "**/*.zip,**/*.tar.gz"
        }
      }
    }
    stage("Model API tests"){
      agent {
        kubernetes {
          yaml getMavenAgent()
        }
      }
      stages {
        stage('XML model') {
          steps{
            container("maven"){
              // Run maven
              unstash "artifactStash"
              configFileProvider([configFile(fileId: 'maven-nexus-settings', variable: 'MAVEN_SETTINGS_XML')]) {
                sh """
                  export MAVEN_OPTS="-Dmaven.repo.local=\$(pwd)/.m2"
                  cd model-api/xml-model && mvn -s \$MAVEN_SETTINGS_XML -B test
                """
              }
            }
          }
        }
        stage('MN model') {
          failFast true
          parallel {
            stage('BPMN model') {
              steps{
                container("maven"){
                  // Run maven
                  configFileProvider([configFile(fileId: 'maven-nexus-settings', variable: 'MAVEN_SETTINGS_XML')]) {
                    sh """
                      export MAVEN_OPTS="-Dmaven.repo.local=\$(pwd)/.m2"
                      cd model-api/bpmn-model && mvn -s \$MAVEN_SETTINGS_XML -B test
                    """
                  }
                }
              }
            }
            stage('DMN model') {
              steps{
                container("maven"){
                  // Run maven
                  configFileProvider([configFile(fileId: 'maven-nexus-settings', variable: 'MAVEN_SETTINGS_XML')]) {
                    sh """
                      export MAVEN_OPTS="-Dmaven.repo.local=\$(pwd)/.m2"
                      cd model-api/dmn-model && mvn -s \$MAVEN_SETTINGS_XML -B test
                    """
                  }
                }
              }
            }
            stage('CMMN model') {
              steps{
                container("maven"){
                  // Run maven
                  configFileProvider([configFile(fileId: 'maven-nexus-settings', variable: 'MAVEN_SETTINGS_XML')]) {
                    sh """
                      export MAVEN_OPTS="-Dmaven.repo.local=\$(pwd)/.m2"
                      cd model-api/cmmn-model && mvn -s \$MAVEN_SETTINGS_XML -B test
                    """
                  }
                }
              }
            }
          }
        }
      }
    }
    stage('Engine UNIT & QA Tests') {
      failFast true
      parallel {
        stage('Engine UNIT tests') {
          agent {
            kubernetes {
              yaml getMavenAgent()
            }
          }
          steps{
            container("maven"){
              // Run maven
              unstash "artifactStash"
              configFileProvider([configFile(fileId: 'maven-nexus-settings', variable: 'MAVEN_SETTINGS_XML')]) {
                sh """
                  export MAVEN_OPTS="-Dmaven.repo.local=\$(pwd)/.m2"
                  cd engine && mvn -s \$MAVEN_SETTINGS_XML -B -T\$LIMITS_CPU test -Pdatabase,h2-in-memory
                """
              }
            }
          }
        }
        stage("Engine UNIT: Authorizations Tests") {
          agent {
            kubernetes {
              yaml getMavenAgent()
            }
          }
          steps {
            container("maven") {
              // Run maven
              unstash "artifactStash"
              configFileProvider([configFile(fileId: 'maven-nexus-settings', variable: 'MAVEN_SETTINGS_XML')]) {
                sh """
                  export MAVEN_OPTS="-Dmaven.repo.local=\$(pwd)/.m2"
                  cd engine/ && mvn -s \$MAVEN_SETTINGS_XML test -Pdatabase,h2-in-memory,cfgAuthorizationCheckRevokesAlways -B
                """
              }
            }
          }
        }
        stage("Engine UNIT: History Level Activity Tests") {
          agent {
            kubernetes {
              yaml getMavenAgent()
            }
          }
          steps {
            container("maven") {
              // Run maven
              unstash "artifactStash"
              configFileProvider([configFile(fileId: 'maven-nexus-settings', variable: 'MAVEN_SETTINGS_XML')]) {
                sh """
                  export MAVEN_OPTS="-Dmaven.repo.local=\$(pwd)/.m2"
                  cd engine/ && mvn -s \$MAVEN_SETTINGS_XML verify -Pcfghistoryactivity -B
                """
              }
            }
          }
        }
        stage("Engine UNIT: History Level Audit Tests") {
          agent {
            kubernetes {
              yaml getMavenAgent()
            }
          }
          steps {
            container("maven") {
              // Run maven
              unstash "artifactStash"
              configFileProvider([configFile(fileId: 'maven-nexus-settings', variable: 'MAVEN_SETTINGS_XML')]) {
                sh """
                  export MAVEN_OPTS="-Dmaven.repo.local=\$(pwd)/.m2"
                  cd engine/ && mvn -s \$MAVEN_SETTINGS_XML verify -Pcfghistoryaudit -B
                """
              }
            }
          }
        }
        stage("Engine UNIT: History Level None Tests") {
          agent {
            kubernetes {
              yaml getMavenAgent()
            }
          }
          steps {
            container("maven") {
              // Run maven
              unstash "artifactStash"
              configFileProvider([configFile(fileId: 'maven-nexus-settings', variable: 'MAVEN_SETTINGS_XML')]) {
                sh """
                  export MAVEN_OPTS="-Dmaven.repo.local=\$(pwd)/.m2"
                  cd engine/ && mvn -s \$MAVEN_SETTINGS_XML verify -Pcfghistorynone -B
                """
              }
            }
          }
        }
        stage('Engine UNIT: DB-Table-Prefix tests') {
          agent {
            kubernetes {
              yaml getMavenAgent()
            }
          }
          steps {
            container("maven") {
              // Run maven
              unstash "artifactStash"
              configFileProvider([configFile(fileId: 'maven-nexus-settings', variable: 'MAVEN_SETTINGS_XML')]) {
                sh """
                  export MAVEN_OPTS="-Dmaven.repo.local=\$(pwd)/.m2"
                  cd engine/ && mvn -s \$MAVEN_SETTINGS_XML test -Pdb-table-prefix -B
                """
              }
            }
          }
        }
        stage("Engine UNIT: Spring Integration Tests") {
          agent {
            kubernetes {
              yaml getMavenAgent()
            }
          }
          steps {
            container("maven") {
              // Run maven
              unstash "artifactStash"
              configFileProvider([configFile(fileId: 'maven-nexus-settings', variable: 'MAVEN_SETTINGS_XML')]) {
                sh """
                  export MAVEN_OPTS="-Dmaven.repo.local=\$(pwd)/.m2"
                  cd engine-spring/ && mvn -s \$MAVEN_SETTINGS_XML test -Pdatabase,h2-in-memory -B
                """
              }
            }
          }
        }
        stage("Engine UNIT: CDI Integration Tests") {
          agent {
            kubernetes {
              yaml getMavenAgent()
            }
          }
          steps {
            container("maven") {
              // Run maven
              unstash "artifactStash"
              configFileProvider([configFile(fileId: 'maven-nexus-settings', variable: 'MAVEN_SETTINGS_XML')]) {
                sh """
                  export MAVEN_OPTS="-Dmaven.repo.local=\$(pwd)/.m2"
                  cd engine-cdi/ && mvn -s \$MAVEN_SETTINGS_XML test -Pdatabase,h2-in-memory -B
                """
              }
            }
          }
        }
        stage("Engine UNIT: Plugins Tests") {
          agent {
            kubernetes {
              yaml getMavenAgent()
            }
          }
          steps {
            container("maven") {
              // Run maven
              unstash "artifactStash"
              configFileProvider([configFile(fileId: 'maven-nexus-settings', variable: 'MAVEN_SETTINGS_XML')]) {
                sh """
                  export MAVEN_OPTS="-Dmaven.repo.local=\$(pwd)/.m2"
                  cd engine-plugins/ && mvn -s \$MAVEN_SETTINGS_XML test -Pdatabase,h2-in-memory -B
                """
              }
            }
          }
        }
        stage('Engine UNIT: Plugins-enabled Engine UNIT tests') {
          agent {
            kubernetes {
              yaml getMavenAgent()
            }
          }
          steps {
            container("maven") {
              // Run maven
              unstash "artifactStash"
              configFileProvider([configFile(fileId: 'maven-nexus-settings', variable: 'MAVEN_SETTINGS_XML')]) {
                sh """
                  export MAVEN_OPTS="-Dmaven.repo.local=\$(pwd)/.m2"
                  cd engine/ && mvn -s \$MAVEN_SETTINGS_XML test -Pcheck-plugins -B
                """
              }
            }
          }
        }
        stage('QA: Instance Migration Tests') {
          agent {
            kubernetes {
              yaml getMavenAgent()
            }
          }
          steps{
            container("maven"){
              // Run maven
              unstash "artifactStash"
              configFileProvider([configFile(fileId: 'maven-nexus-settings', variable: 'MAVEN_SETTINGS_XML')]) {
                sh """
                  export MAVEN_OPTS="-Dmaven.repo.local=\$(pwd)/.m2"
                  cd qa/test-db-instance-migration && mvn -s \$MAVEN_SETTINGS_XML -B verify -Pinstance-migration,h2
                """
              }
            }
          }
        }
        stage('QA: Rolling Update Tests') {
          agent {
            kubernetes {
              yaml getMavenAgent()
            }
          }
          steps{
            container("maven"){
              // Run maven
              unstash "artifactStash"
              configFileProvider([configFile(fileId: 'maven-nexus-settings', variable: 'MAVEN_SETTINGS_XML')]) {
                sh """
                  export MAVEN_OPTS="-Dmaven.repo.local=\$(pwd)/.m2"
                  cd qa/test-db-rolling-update && mvn -s \$MAVEN_SETTINGS_XML -B verify -Prolling-update,h2
                """
              }
            }
          }
        }
        stage('QA: Upgrade old engine from 7.13') {
          agent {
            kubernetes {
              yaml getMavenAgent()
            }
          }
          steps{
            container("maven"){
              // Run maven
              unstash "artifactStash"
              configFileProvider([configFile(fileId: 'maven-nexus-settings', variable: 'MAVEN_SETTINGS_XML')]) {
                sh """
                  export MAVEN_OPTS="-Dmaven.repo.local=\$(pwd)/.m2"
                  cd qa && mvn -s \$MAVEN_SETTINGS_XML -B -T\$LIMITS_CPU verify -Pold-engine,h2
                """
              }
            }
          }
        }
        stage('QA: Upgrade database from 7.13') {
          agent {
            kubernetes {
              yaml getMavenAgent()
            }
          }
          steps{
            container("maven"){
              // Run maven
              unstash "artifactStash"
              configFileProvider([configFile(fileId: 'maven-nexus-settings', variable: 'MAVEN_SETTINGS_XML')]) {
                sh """
                  export MAVEN_OPTS="-Dmaven.repo.local=\$(pwd)/.m2"
                  cd qa/test-db-upgrade && mvn -s \$MAVEN_SETTINGS_XML -B -T\$LIMITS_CPU verify -Pupgrade-db,h2
                """
              }
            }
          }
        }
      }
    }
    stage("Rest API & Webapps Tests"){
      failFast true
      parallel {
        stage('Rest API UNIT Jersey2 tests') {
          agent {
            kubernetes {
              yaml getMavenAgent()
            }
          }
          steps {
            container("maven") {
              // Run maven
              unstash "artifactStash"
              configFileProvider([configFile(fileId: 'maven-nexus-settings', variable: 'MAVEN_SETTINGS_XML')]) {
                sh """
                  export MAVEN_OPTS="-Dmaven.repo.local=\$(pwd)/.m2"
                  cd engine-rest/engine-rest/ && mvn -s \$MAVEN_SETTINGS_XML test -Pjersey2 -B
                """
              }
            }
          }
        }
        stage('Rest API UNIT Resteasy tests') {
          agent {
            kubernetes {
              yaml getMavenAgent()
            }
          }
          steps {
            container("maven") {
              // Run maven
              unstash "artifactStash"
              configFileProvider([configFile(fileId: 'maven-nexus-settings', variable: 'MAVEN_SETTINGS_XML')]) {
                sh """
                  export MAVEN_OPTS="-Dmaven.repo.local=\$(pwd)/.m2"
                  cd engine-rest/engine-rest/ && mvn -s \$MAVEN_SETTINGS_XML test -Presteasy -B
                """
              }
            }
          }
        }
        stage('Rest API UNIT Resteasy3 tests') {
          agent {
            kubernetes {
              yaml getMavenAgent()
            }
          }
          steps {
            container("maven") {
              // Run maven
              unstash "artifactStash"
              configFileProvider([configFile(fileId: 'maven-nexus-settings', variable: 'MAVEN_SETTINGS_XML')]) {
                sh """
                  export MAVEN_OPTS="-Dmaven.repo.local=\$(pwd)/.m2"
                  cd engine-rest/engine-rest/ && mvn -s \$MAVEN_SETTINGS_XML test -Presteasy3 -B
                """
              }
            }
          }
        }
        stage('Rest API UNIT CXF tests') {
          agent {
            kubernetes {
              yaml getMavenAgent()
            }
          }
          steps {
            container("maven") {
              // Run maven
              unstash "artifactStash"
              configFileProvider([configFile(fileId: 'maven-nexus-settings', variable: 'MAVEN_SETTINGS_XML')]) {
                sh """
                  export MAVEN_OPTS="-Dmaven.repo.local=\$(pwd)/.m2"
                  cd engine-rest/engine-rest/ && mvn -s \$MAVEN_SETTINGS_XML test -Pcxf -B
                """
              }
            }
          }
        }
        stage('Rest API UNIT Wink tests') {
          agent {
            kubernetes {
              yaml getMavenAgent()
            }
          }
          steps {
            container("maven") {
              // Run maven
              unstash "artifactStash"
              configFileProvider([configFile(fileId: 'maven-nexus-settings', variable: 'MAVEN_SETTINGS_XML')]) {
                sh """
                  export MAVEN_OPTS="-Dmaven.repo.local=\$(pwd)/.m2"
                  cd engine-rest/engine-rest/ && mvn -s \$MAVEN_SETTINGS_XML test -Pwink -B
                """
              }
            }
          }
        }
        stage("Rest API JAX-RS2 Jersey2 tests") {
          agent {
            kubernetes {
              yaml getMavenAgent()
            }
          }
          steps {
            container("maven") {
              // Run maven
              unstash "artifactStash"
              configFileProvider([configFile(fileId: 'maven-nexus-settings', variable: 'MAVEN_SETTINGS_XML')]) {
                sh """
                  export MAVEN_OPTS="-Dmaven.repo.local=\$(pwd)/.m2"
                  cd engine-rest/engine-rest-jaxrs2/ && mvn -s \$MAVEN_SETTINGS_XML test -Pjersey2 -B
                """
              }
            }
          }
        }
        stage("Rest API JAX-RS2 Resteasy3 tests") {
          agent {
            kubernetes {
              yaml getMavenAgent()
            }
          }
          steps {
            container("maven") {
              // Run maven
              unstash "artifactStash"
              configFileProvider([configFile(fileId: 'maven-nexus-settings', variable: 'MAVEN_SETTINGS_XML')]) {
                sh """
                  export MAVEN_OPTS="-Dmaven.repo.local=\$(pwd)/.m2"
                  cd engine-rest/engine-rest-jaxrs2/ && mvn -s \$MAVEN_SETTINGS_XML test -Presteasy3 -B
                """
              }
            }
          }
        }
        stage('WLS-compatibility tests') {
          agent {
            kubernetes {
              yaml getMavenAgent()
            }
          }
          steps {
            container("maven") {
              // Run maven
              unstash "artifactStash"
              configFileProvider([configFile(fileId: 'maven-nexus-settings', variable: 'MAVEN_SETTINGS_XML')]) {
                sh """
                  export MAVEN_OPTS="-Dmaven.repo.local=\$(pwd)/.m2"
                  mvn -s \$MAVEN_SETTINGS_XML verify -Pcheck-engine,wls-compatibility,jersey -B
                """
              }
            }
          }
        }
        stage('Wildfly-compatibility tests') {
          agent {
            kubernetes {
              yaml getMavenAgent()
            }
          }
          steps {
            container("maven") {
              // Run maven
              unstash "artifactStash"
              configFileProvider([configFile(fileId: 'maven-nexus-settings', variable: 'MAVEN_SETTINGS_XML')]) {
                sh """
                  export MAVEN_OPTS="-Dmaven.repo.local=\$(pwd)/.m2"
                  cd engine-rest/engine-rest/ && mvn -s \$MAVEN_SETTINGS_XML test -Pwildfly-compatibility,resteasy -B
                """
              }
            }
          }
        }
        stage('Webapp UNIT tests') {
          agent {
            kubernetes {
              yaml getMavenAgent()
            }
          }
          steps {
            container("maven") {
              // Run maven
              unstash "artifactStash"
              configFileProvider([configFile(fileId: 'maven-nexus-settings', variable: 'MAVEN_SETTINGS_XML')]) {
                sh """
                  export MAVEN_OPTS="-Dmaven.repo.local=\$(pwd)/.m2"
                  cd webapps/ && mvn -s \$MAVEN_SETTINGS_XML test -Pdatabase,h2-in-memory -Dskip.frontend.build=true -B
                """
              }
            }
          }
        }
        stage('Webapp UNIT: Authorizations tests') {
          agent {
            kubernetes {
              yaml getMavenAgent()
            }
          }
          steps {
            container("maven") {
              // Run maven
              unstash "artifactStash"
              configFileProvider([configFile(fileId: 'maven-nexus-settings', variable: 'MAVEN_SETTINGS_XML')]) {
                sh """
                  export MAVEN_OPTS="-Dmaven.repo.local=\$(pwd)/.m2"
                  cd webapps/ && mvn -s \$MAVEN_SETTINGS_XML test -Pdatabase,h2-in-memory,cfgAuthorizationCheckRevokesAlways -Dskip.frontend.build=true -B
                """
              }
            }
          }
        }
      }
    }
  }
}
