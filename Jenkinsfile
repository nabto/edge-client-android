pipeline {
    agent none
    options { disableConcurrentBuilds() }
    stages {
        stage('Build') {
            agent {
                dockerfile {
                    filename 'Dockerfile'
                    dir 'build-container'
                }
            }

            environment {
                releaseDir = "linux-release"
                srcDir = pwd()
            }
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
    }
}
