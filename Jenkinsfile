pipeline {
    agent none
    agent {
        dockerfile {
            filename 'Dockerfile'
            dir 'build-container'
        }
    }
    options { disableConcurrentBuilds() }
    stages {
        stage('Build') {

            steps {
                checkout scm
                sh "./gradlew :library:build --rerun-tasks"
            }
            post {
                always {
                    archiveArtifacts artifacts: 'library/build/outputs/aar/*.aar', onlyIfSuccessful: true
                    archiveArtifacts artifacts: 'library/build/libs/library-sources.jar', onlyIfSuccessful: true
                }
            }
        }
        stage('Deploy') {
            when {
                buildingTag()
            }
            steps {
                 withCredentials([string(credentialsId: "bintray_api_key", variable: "BINTRAY_API_KEY")]) {
                     sh "./gradlew :library:bintrayUpload"
                 }
            }
        }
    }
}
