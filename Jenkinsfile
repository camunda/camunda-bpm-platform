
def getMavenAgent(){
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
    - name: TZ
      value: Europe/Berlin
    resources:
      limits:
        cpu: 3
        memory: 8Gi
      requests:
        cpu: 3
        memory: 8Gi
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
          // Install asdf
          sh '''
            curl -s -O https://deb.nodesource.com/node_14.x/pool/main/n/nodejs/nodejs_14.6.0-1nodesource1_amd64.deb
            dpkg -i nodejs_14.6.0-1nodesource1_amd64.deb
            npm set unsafe-perm true
            apt -qq update && apt install -y g++ make
          '''
          // Run maven
          configFileProvider([configFile(fileId: 'maven-nexus-settings', variable: 'MAVEN_SETTINGS_XML')]) {
            sh """
              mvn -s \$MAVEN_SETTINGS_XML -B -T3 clean source:jar install -D skipTests
            """
          }
        }
      }
    }
    stage("Tests"){
      failFast true
      parallel {
        stage('Engine unit tests') {
          agent {
            kubernetes {
              yaml getMavenAgent()
            }
          }
          steps{
            container("maven"){
              // Run maven
              configFileProvider([configFile(fileId: 'maven-nexus-settings', variable: 'MAVEN_SETTINGS_XML')]) {
                sh """
                  cd model-api/xml-model && mvn -s \$MAVEN_SETTINGS_XML -B test
                """
                sh """
                  cd model-api/bpmn-model && mvn -s \$MAVEN_SETTINGS_XML -B test
                """
                sh """
                  cd model-api/dmn-model && mvn -s \$MAVEN_SETTINGS_XML -B test
                """
                sh """
                  cd model-api/cmmn-model && mvn -s \$MAVEN_SETTINGS_XML -B test
                """
                sh """
                  cd engine && mvn -s \$MAVEN_SETTINGS_XML -B -T3 test -Pdatabase,h2
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
              configFileProvider([configFile(fileId: 'maven-nexus-settings', variable: 'MAVEN_SETTINGS_XML')]) {
                sh """
                  cd qa/test-db-instance-migration && mvn -s \$MAVEN_SETTINGS_XML -B -T3 verify -Pinstance-migration,h2
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
              configFileProvider([configFile(fileId: 'maven-nexus-settings', variable: 'MAVEN_SETTINGS_XML')]) {
                sh """
                  cd qa/test-db-rolling-update && mvn -s \$MAVEN_SETTINGS_XML -B -T3 verify -Prolling-update,h2
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
              configFileProvider([configFile(fileId: 'maven-nexus-settings', variable: 'MAVEN_SETTINGS_XML')]) {
                sh """
                  cd qa && mvn -s \$MAVEN_SETTINGS_XML -B -T3 verify -Pold-engine,h2
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
              configFileProvider([configFile(fileId: 'maven-nexus-settings', variable: 'MAVEN_SETTINGS_XML')]) {
                sh """
                  cd qa/test-db-upgrade && mvn -s \$MAVEN_SETTINGS_XML -B -T3 verify -Pupgrade-db,h2
                """
              }
            }
          }
        }
      }
    }
  }
}
