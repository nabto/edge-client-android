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
                sh "./gradlew :jni:build --rerun-tasks"
            }
            post {
                always {
                    archiveArtifacts artifacts: 'jni/build/outputs/aar/*.aar', onlyIfSuccessful: true
                    archiveArtifacts artifacts: 'jni/build/libs/jni-sources.jar', onlyIfSuccessful: true
                }
            }
        }
    }
}
