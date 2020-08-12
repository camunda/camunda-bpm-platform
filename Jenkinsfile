
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
    image: ubuntu:20.04
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
    stage("First stage"){
      agent {
        kubernetes {
          yaml getMavenAgent()
        }
      }
      steps{
        container("maven"){
          // Install asdf
          sh """
            git clone https://github.com/asdf-vm/asdf.git ~/.asdf --branch v0.7.8
            echo '. /root/.asdf/asdf.sh' > ~/.bashrc
            . /root/.asdf/asdf.sh
            for plugin in \$(cat .tool-versions | awk '{print \$1}'); do
                asdf plugin add \${plugin};
            done
            asdf install
          """
          // Run maven
          configFileProvider([configFile(fileId: 'maven-nexus-settings', variable: 'MAVEN_SETTINGS_XML')]) {
            sh("mvn -s \$MAVEN_SETTINGS_XML -B -T3 clean install -D skipTests")
          }
        }
      }
    }
  }
}
