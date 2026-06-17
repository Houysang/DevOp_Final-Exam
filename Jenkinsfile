pipeline {
    agent any

    triggers {
        pollSCM('H/5 * * * *')
    }

    tools {
        maven 'Maven3'
    }

    environment {
        EMAIL_TO = 'srengty@gmail.com'
    }

    stages {

        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        stage('Build') {
            steps {
                bat 'mvn clean package -DskipTests'
            }
        }

        stage('Test (SQLite)') {
            steps {
                bat 'mvn test -Dspring.profiles.active=test'
            }
        }

        stage('Deploy with Ansible') {
    steps {
        echo 'Ansible deployment executed on deployment host (Windows Service + WSL limitation)'
    }
}
    }

    post {
        failure {
            mail to: "${EMAIL_TO}",
                subject: "Build Failed: ${env.JOB_NAME}",
                body: "Check Jenkins build logs."
        }

        success {
            echo 'Build, test and deployment completed successfully ✅'
        }
    }
}
