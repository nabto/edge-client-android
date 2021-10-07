pipeline {
    agent {
        dockerfile {
            filename 'Dockerfile'
            dir 'build-container'
        }
    }
    environment {
        BINTRAY_USER = 'nabto'
    }
    options { disableConcurrentBuilds() }
    stages {
        stage('Build') {

            steps {
                checkout scm
                sh "./library/generate_swig.sh"
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
            steps {
                 withCredentials([string(credentialsId: "android_signing_key", variable: "ORG_GRADLE_PROJECT_GPG_SIGNING_KEY_BASE64"),
                                  string(credentialsId: "android_signing_key_password", variable: "ORG_GRADLE_PROJECT_GPG_SIGNING_PASSWORD"),
                                  string(credentialsId: "sonatype_ossrh_username", variable: "ORG_GRADLE_PROJECT_OSSRH_USERNAME"),
                                  string(credentialsId: "sonatype_ossrh_password", variable: "ORG_GRADLE_PROJECT_OSSRH_PASSWORD")]) {
                     sh "./gradlew publish"
                 }
            }
        }
    }
}
