#!/usr/bin/env groovy

String getMavenAgent(Integer mavenCpuLimit = 4, String dockerTag = '3.6.3-openjdk-8'){
  // assuming one core left for main maven thread
  String mavenForkCount = mavenCpuLimit;
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
    image: maven:${dockerTag}
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
    stage("Top Level Components"){
      agent {
        kubernetes {
          yaml getMavenAgent()
        }
      }
      stages {
        stage('Unstash') {
          steps{
            container("maven"){
              unstash "artifactStash"
            }
          }
        }
        stage('Top Level Components Tests') {
          failFast true
          parallel {
            stage('XML model') {
              steps{
                container("maven"){
                  // Run maven
                  configFileProvider([configFile(fileId: 'maven-nexus-settings', variable: 'MAVEN_SETTINGS_XML')]) {
                    sh """
                      export MAVEN_OPTS="-Dmaven.repo.local=\$(pwd)/.m2"
                      cd model-api/xml-model && mvn -s \$MAVEN_SETTINGS_XML -B test
                    """
                  }
                }
              }
            }
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
            stage('camunda-commons-typed-values tests') {
              steps{
                container("maven"){
                  // Run maven
                  configFileProvider([configFile(fileId: 'maven-nexus-settings', variable: 'MAVEN_SETTINGS_XML')]) {
                    sh """
                      export MAVEN_OPTS="-Dmaven.repo.local=\$(pwd)/.m2"
                      cd typed-values && mvn -s \$MAVEN_SETTINGS_XML -B test
                    """
                  }
                }
              }
            }
            stage('DMN engine tests') {
              steps{
                container("maven"){
                  // Run maven
                  configFileProvider([configFile(fileId: 'maven-nexus-settings', variable: 'MAVEN_SETTINGS_XML')]) {
                    sh """
                      export MAVEN_OPTS="-Dmaven.repo.local=\$(pwd)/.m2"
                      cd engine-dmn && mvn -s \$MAVEN_SETTINGS_XML -B verify
                    """
                  }
                }
              }
            }
            stage('sql-scripts') {
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
                    //sh """
                    //  errors=0
                    //
                    //  for create_script in engine/src/main/resources/org/camunda/bpm/engine/db/create/*.sql; do
                    //      drop_script=${create_script//create/drop}
                    //      created_indexes=$(grep -i '^\s*create \(unique \)\?index' $create_script | tr [A-Z] [a-z] | sed 's/^\s*create \(unique \)\?index \(\S\+\).*$/\2/' | sort)
                    //      dropped_indexes=$(grep -i '^\s*drop index' $drop_script | tr [A-Z] [a-z] | sed 's/^\s*drop index \([^.]\+\.\)\?\([^ ;]\+\).*$/\2/' | sort)
                    //      diff_indexes=$(diff <(echo \'$created_indexes\') <(echo \'$dropped_indexes\'))
                    //      if [ $? -ne 0 ]; then
                    //          echo 'Found index difference for:'
                    //          echo $create_script
                    //          echo $drop_script
                    //          echo -e \'${diff_indexes}\n\'
                    //          errors=$[errors + 1]
                    //      fi
                    //  done
                    //  
                    //  exit $errors
                    //"""
                    sh """
                      export MAVEN_OPTS="-Dmaven.repo.local=\$(pwd)/.m2"
                      cd distro/sql-script && mvn -s \$MAVEN_SETTINGS_XML -B install -Pcheck-sql,h2
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
        stage('Engine UNIT & Authorization tests') {
          agent {
            kubernetes {
              yaml getMavenAgent(16)
            }
          }
          stages {
            stage('Engine UNIT tests') {
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
              steps {
                container("maven") {
                  // Run maven
                  configFileProvider([configFile(fileId: 'maven-nexus-settings', variable: 'MAVEN_SETTINGS_XML')]) {
                    sh """
                      export MAVEN_OPTS="-Dmaven.repo.local=\$(pwd)/.m2"
                      cd engine/ && mvn -s \$MAVEN_SETTINGS_XML test -Pdatabase,h2-in-memory,cfgAuthorizationCheckRevokesAlways -B -T\$LIMITS_CPU
                    """
                  }
                }
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
        stage('Engine UNIT: CDI Integration / Plugins / Spring Integration Tests') {
          agent {
            kubernetes {
              yaml getMavenAgent()
            }
          }
          stages {
            stage("Engine UNIT: CDI Integration Tests") {
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
              steps {
                container("maven") {
                  // Run maven
                  configFileProvider([configFile(fileId: 'maven-nexus-settings', variable: 'MAVEN_SETTINGS_XML')]) {
                    sh """
                      export MAVEN_OPTS="-Dmaven.repo.local=\$(pwd)/.m2"
                      cd engine-plugins/ && mvn -s \$MAVEN_SETTINGS_XML test -Pdatabase,h2-in-memory -B
                    """
                  }
                }
              }
            }
            stage("Engine UNIT: Spring Integration Tests") {
              steps {
                container("maven") {
                  // Run maven
                  configFileProvider([configFile(fileId: 'maven-nexus-settings', variable: 'MAVEN_SETTINGS_XML')]) {
                    sh """
                      export MAVEN_OPTS="-Dmaven.repo.local=\$(pwd)/.m2"
                      cd engine-spring/ && mvn -s \$MAVEN_SETTINGS_XML test -Pdatabase,h2-in-memory -B
                    """
                  }
                }
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
        stage('QA: Instance Migration & Rolling Update Tests') {
          agent {
            kubernetes {
              yaml getMavenAgent()
            }
          }
          stages {
            stage('QA: Instance Migration Tests') {
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
              steps{
                container("maven"){
                  // Run maven
                  configFileProvider([configFile(fileId: 'maven-nexus-settings', variable: 'MAVEN_SETTINGS_XML')]) {
                    sh """
                      export MAVEN_OPTS="-Dmaven.repo.local=\$(pwd)/.m2"
                      cd qa/test-db-rolling-update && mvn -s \$MAVEN_SETTINGS_XML -B verify -Prolling-update,h2
                    """
                  }
                }
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
      when {
        anyOf {
          branch 'hackdays-master';
          allOf {
            changeRequest();
            expression {
              pullRequest.labels.contains('rest-api')
            }
          }
        }
      }
      failFast true
      parallel {
        stage('Rest API - Jersey2') {
          agent {
            kubernetes {
              yaml getMavenAgent()
            }
          }
          stages {
            stage('Rest API UNIT Jersey2 tests') {
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
            stage("Rest API JAX-RS2 Jersey2 tests") {
              steps {
                container("maven") {
                  // Run maven
                  configFileProvider([configFile(fileId: 'maven-nexus-settings', variable: 'MAVEN_SETTINGS_XML')]) {
                    sh """
                      export MAVEN_OPTS="-Dmaven.repo.local=\$(pwd)/.m2"
                      cd engine-rest/engine-rest-jaxrs2/ && mvn -s \$MAVEN_SETTINGS_XML test -Pjersey2 -B
                    """
                  }
                }
              }
            }
          }
        }
        stage('Rest API - Resteasy / Resteasy3') {
          agent {
            kubernetes {
              yaml getMavenAgent()
            }
          }
          stages {
            stage('Rest API UNIT Resteasy tests') {
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
              steps {
                container("maven") {
                  // Run maven
                  configFileProvider([configFile(fileId: 'maven-nexus-settings', variable: 'MAVEN_SETTINGS_XML')]) {
                    sh """
                      export MAVEN_OPTS="-Dmaven.repo.local=\$(pwd)/.m2"
                      cd engine-rest/engine-rest/ && mvn -s \$MAVEN_SETTINGS_XML test -Presteasy3 -B
                    """
                  }
                }
              }
            }
            stage("Rest API JAX-RS2 Resteasy3 tests") {
              steps {
                container("maven") {
                  // Run maven
                  configFileProvider([configFile(fileId: 'maven-nexus-settings', variable: 'MAVEN_SETTINGS_XML')]) {
                    sh """
                      export MAVEN_OPTS="-Dmaven.repo.local=\$(pwd)/.m2"
                      cd engine-rest/engine-rest-jaxrs2/ && mvn -s \$MAVEN_SETTINGS_XML test -Presteasy3 -B
                    """
                  }
                }
              }
            }
          }
        }
        stage('Rest API - CXF / Wink') {
          agent {
            kubernetes {
              yaml getMavenAgent()
            }
          }
          stages {
            stage('Rest API UNIT CXF tests') {
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
              steps {
                container("maven") {
                  // Run maven
                  configFileProvider([configFile(fileId: 'maven-nexus-settings', variable: 'MAVEN_SETTINGS_XML')]) {
                    sh """
                      export MAVEN_OPTS="-Dmaven.repo.local=\$(pwd)/.m2"
                      cd engine-rest/engine-rest/ && mvn -s \$MAVEN_SETTINGS_XML clean test -Pwink -B
                    """
                  }
                }
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
          when {
            anyOf {
              branch 'hackdays-master';
              allOf {
                changeRequest();
                expression {
                  pullRequest.labels.contains('h2')
                }
              }
            }
          }
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
        stage('Webapp') {
          when {
            anyOf {
              branch 'hackdays-master';
              allOf {
                changeRequest();
                expression {
                  pullRequest.labels.contains('h2')
                }
              }
            }
          }
          agent {
            kubernetes {
              yaml getMavenAgent()
            }
          }
          stages {
            stage('Webapp UNIT tests') {
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
            stage('Webapp UNIT: DB-Table-prefix tests') {
              steps {
                container("maven") {
                  // Run maven
                  configFileProvider([configFile(fileId: 'maven-nexus-settings', variable: 'MAVEN_SETTINGS_XML')]) {
                    sh """
                      export MAVEN_OPTS="-Dmaven.repo.local=\$(pwd)/.m2"
                      cd webapps/ && mvn -s \$MAVEN_SETTINGS_XML test -Pdb-table-prefix -Dskip.frontend.build=true -B
                    """
                  }
                }
              }
            }
            stage('Webapp UNIT: Authorizations tests') {
              steps {
                container("maven") {
                  // Run maven
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
    stage("JDK Tests") {
      failFast true
      parallel {
        // There are no Maven Docker Images with Oracle JDK versions.
        // We will probably need to provide our own custom OracleJDK-Maven Docker Images
        stage('OpenJDK 8 tests') {
          agent {
            kubernetes {
              yaml getMavenAgent( 3, '3.6.3-openjdk-8')
            }
          }
          steps {
            container("maven") {
              // Run maven
              unstash "artifactStash"
              configFileProvider([configFile(fileId: 'maven-nexus-settings', variable: 'MAVEN_SETTINGS_XML')]) {
                sh """
                  export MAVEN_OPTS="-Dmaven.repo.local=\$(pwd)/.m2"
                  mvn -s \$MAVEN_SETTINGS_XML -B -T\$LIMITS_CPU install source:jar source:test-jar -Pdistro,distro-ce,distro-wildfly -B
                """
              }
            }
          }
        }
        stage('OpenJDK 11 tests') {
          agent {
            kubernetes {
              yaml getMavenAgent( 3, '3.6.3-openjdk-11')
            }
          }
          steps {
            container("maven") {
              // Run maven
              unstash "artifactStash"
              configFileProvider([configFile(fileId: 'maven-nexus-settings', variable: 'MAVEN_SETTINGS_XML')]) {
                sh """
                  export MAVEN_OPTS="-Dmaven.repo.local=\$(pwd)/.m2"
                  mvn -s \$MAVEN_SETTINGS_XML -B -T\$LIMITS_CPU install source:jar source:test-jar -pl '!distro/jbossas7/subsystem' -Pdistro,distro-ce,distro-wildfly -B
                """
              }
            }
          }
        }
        stage('OpenJDK 14 tests') {
          agent {
            kubernetes {
              yaml getMavenAgent( 3, '3.6.3-openjdk-14')
            }
          }
          steps {
            container("maven") {
              // Run maven
              unstash "artifactStash"
              configFileProvider([configFile(fileId: 'maven-nexus-settings', variable: 'MAVEN_SETTINGS_XML')]) {
                sh """
                  export MAVEN_OPTS="-Dmaven.repo.local=\$(pwd)/.m2"
                  cd engine && mvn -s \$MAVEN_SETTINGS_XML -B -T\$LIMITS_CPU install source:jar source:test-jar -pl '!distro/jbossas7/subsystem' -Pdistro,distro-ce,distro-wildfly -B
                """
              }
            }
          }
        }
        stage('IBM JDK 8 tests') {
          agent {
            kubernetes {
              yaml getMavenAgent(3, '3.6.3-ibmjava-8')
            }
          }
          steps {
            container("maven") {
              // Run maven
              unstash "artifactStash"
              configFileProvider([configFile(fileId: 'maven-nexus-settings', variable: 'MAVEN_SETTINGS_XML')]) {
                sh """
                  export MAVEN_OPTS="-Dmaven.repo.local=\$(pwd)/.m2"
                  mvn -s \$MAVEN_SETTINGS_XML -B -T\$LIMITS_CPU install source:jar source:test-jar -Pdistro,distro-ce,distro-wildfly -B
                """
              }
            }
          }
        }
      }
    }
    stage("Engine & Webapps IT Tests") {
      failFast true
      parallel {
        stage('Engine IT: Tomcat tests') {
          agent {
            kubernetes {
              yaml getMavenAgent( 3, '3.6.3-openjdk-8')
            }
          }
          steps {
            container("maven") {
              // Run maven
              unstash "artifactStash"
              configFileProvider([configFile(fileId: 'maven-nexus-settings', variable: 'MAVEN_SETTINGS_XML')]) {
                sh """
                  export MAVEN_OPTS="-Dmaven.repo.local=\$(pwd)/.m2"
                  cd qa/ && mvn -s \$MAVEN_SETTINGS_XML -B -T\$LIMITS_CPU verify -Ptomcat,postgresql,engine-integration -B
                """
              }
            }
          }
        }
        stage('Engine IT: Wildfly tests') {
          agent {
            kubernetes {
              yaml getMavenAgent( 3, '3.6.3-openjdk-8')
            }
          }
          steps {
            container("maven") {
              // Run maven
              unstash "artifactStash"
              configFileProvider([configFile(fileId: 'maven-nexus-settings', variable: 'MAVEN_SETTINGS_XML')]) {
                sh """
                  export MAVEN_OPTS="-Dmaven.repo.local=\$(pwd)/.m2"
                  cd qa/ && mvn -s \$MAVEN_SETTINGS_XML -B -T\$LIMITS_CPU verify -Pwildfly,postgresql,engine-integration -B
                """
              }
            }
          }
        }
        stage('Engine IT: Wildfly XA tests') {
          agent {
            kubernetes {
              yaml getMavenAgent( 3, '3.6.3-openjdk-8')
            }
          }
          steps {
            container("maven") {
              // Run maven
              unstash "artifactStash"
              configFileProvider([configFile(fileId: 'maven-nexus-settings', variable: 'MAVEN_SETTINGS_XML')]) {
                sh """
                  export MAVEN_OPTS="-Dmaven.repo.local=\$(pwd)/.m2"
                  cd qa/ && mvn -s \$MAVEN_SETTINGS_XML -B -T\$LIMITS_CPU verify -Pwildfly,postgresql,postgresql-xa,engine-integration -B
                """
              }
            }
          }
        }
        stage('Engine IT: Wildfly Domain tests') {
          agent {
            kubernetes {
              yaml getMavenAgent( 3, '3.6.3-openjdk-8')
            }
          }
          steps {
            container("maven") {
              // Run maven
              unstash "artifactStash"
              configFileProvider([configFile(fileId: 'maven-nexus-settings', variable: 'MAVEN_SETTINGS_XML')]) {
                sh """
                  export MAVEN_OPTS="-Dmaven.repo.local=\$(pwd)/.m2"
                  cd qa/ && mvn -s \$MAVEN_SETTINGS_XML -B -T\$LIMITS_CPU verify -Pwildfly-domain,h2,engine-integration -B
                """
              }
            }
          }
        }
        stage('Engine IT: Wildfly Servlet tests') {
          agent {
            kubernetes {
              yaml getMavenAgent( 3, '3.6.3-openjdk-8')
            }
          }
          steps {
            container("maven") {
              // Run maven
              unstash "artifactStash"
              configFileProvider([configFile(fileId: 'maven-nexus-settings', variable: 'MAVEN_SETTINGS_XML')]) {
                sh """
                  export MAVEN_OPTS="-Dmaven.repo.local=\$(pwd)/.m2"
                  cd qa/ && mvn -s \$MAVEN_SETTINGS_XML -B -T\$LIMITS_CPU verify -Pwildfly,wildfly-servlet,h2,engine-integration -B
                """
              }
            }
          }
        }
        stage('Webapps Rest IT: Embedded Wildfly tests') {
          agent {
            kubernetes {
              yaml getMavenAgent( 3, '3.6.3-openjdk-8')
            }
          }
          steps {
            container("maven") {
              // Run maven
              unstash "artifactStash"
              configFileProvider([configFile(fileId: 'maven-nexus-settings', variable: 'MAVEN_SETTINGS_XML')]) {
                sh """
                  export MAVEN_OPTS="-Dmaven.repo.local=\$(pwd)/.m2"
                  cd qa/ && mvn -s \$MAVEN_SETTINGS_XML -B -T\$LIMITS_CPU verify -Pwildfly,h2,webapps-integration,embedded-engine-rest -B
                """
              }
            }
          }
        }
        stage('Distro: Wildfly Subsystem UNIT tests') {
          agent {
            kubernetes {
              yaml getMavenAgent( 3, '3.6.3-openjdk-8')
            }
          }
          steps {
            container("maven") {
              // Run maven
              unstash "artifactStash"
              configFileProvider([configFile(fileId: 'maven-nexus-settings', variable: 'MAVEN_SETTINGS_XML')]) {
                sh """
                  export MAVEN_OPTS="-Dmaven.repo.local=\$(pwd)/.m2"
                  cd distro/wildfly/subsystem && mvn -s \$MAVEN_SETTINGS_XML -B -T\$LIMITS_CPU test -B
                """
              }
            }
          }
        }
      }
    }
  }
}
