// https://github.com/camunda/jenkins-global-shared-library
// https://github.com/camunda/cambpm-jenkins-shared-library
@Library(['camunda-ci', 'cambpm-jenkins-shared-library@db-mssql']) _

def failedStageTypes = []

pipeline {
  agent none
  stages {
    stage('UNIT DB tests') {
      steps {
        script {
          parallel(cambpmGetMatrixStages('main-test', failedStageTypes, { stageType, dbLabel ->
            return true
          }))
        }
      }
    }
  }
}
