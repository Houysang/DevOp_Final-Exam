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
                echo 'Running Ansible Playbook...'

                // If Ansible is installed on Windows/WSL:
                // bat 'ansible-playbook -i inventory deploy.yml'

                echo 'Ansible deployment executed (simulation mode for Windows Jenkins)'
            }
        }
    }

    post {

        success {
            echo 'Build, test and deployment completed successfully ✅'
        }

        failure {
            script {
                def authorEmail = env.CHANGE_AUTHOR_EMAIL ?: EMAIL_TO

                mail to: authorEmail,
                     cc: EMAIL_TO,
                     subject: "❌ Build Failed: ${env.JOB_NAME} #${env.BUILD_NUMBER}",
                     body: """
Jenkins Build Failed!

Job: ${env.JOB_NAME}
Build Number: ${env.BUILD_NUMBER}

Check logs:
${env.BUILD_URL}

Please fix the issue and commit again.
"""
            }
        }
    }
}