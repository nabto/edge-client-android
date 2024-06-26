pipeline {
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
                sh "./build-scripts/android_ci.sh build"
            }
        }
        stage('Deploy') {
            steps {
                 withCredentials([string(credentialsId: "android_signing_key", variable: "ORG_GRADLE_PROJECT_GPG_SIGNING_KEY_BASE64"),
                                  string(credentialsId: "android_signing_key_password", variable: "ORG_GRADLE_PROJECT_GPG_SIGNING_PASSWORD"),
                                  string(credentialsId: "sonatype_ossrh_username", variable: "ORG_GRADLE_PROJECT_OSSRH_USERNAME"),
                                  string(credentialsId: "sonatype_ossrh_password", variable: "ORG_GRADLE_PROJECT_OSSRH_PASSWORD")]) {
                     sh "./build-scripts/android_ci.sh deploy"
                 }
            }
        }
    }
}
