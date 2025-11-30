pipeline {
    agent any

    tools {
        jdk 'JDK17'
        maven 'Maven3'
    }

    environment {
        SONARQUBE_ENV = credentials('sonarqube-token-id')
    }

    stages {

        stage('Checkout') {
            steps {
                git branch: 'main', url: 'https://github.com/your/repo.git'
            }
        }

        stage('Build with Maven') {
            steps {
                sh "mvn -B clean compile"
            }
        }

        stage('Unit Tests (with mocks)') {
            steps {
                sh "mvn -B test"
            }
            post {
                always {
                    junit '**/target/surefire-reports/*.xml'
                }
            }
        }

        stage('SonarQube Analysis') {
            environment {
                SONAR_HOST_URL = 'https://your-sonarqube-url'
            }
            steps {
                withSonarQubeEnv('SonarQubeServer') {
                    sh """
                       mvn sonar:sonar \
                       -Dsonar.projectKey=your_project_key \
                       -Dsonar.host.url=$SONAR_HOST_URL \
                       -Dsonar.login=$SONARQUBE_ENV
                    """
                }
            }
        }

        stage('Quality Gate') {
            steps {
                timeout(time: 2, unit: 'MINUTES') {
                    waitForQualityGate abortPipeline: true
                }
            }
        }

        stage('Package') {
            steps {
                sh "mvn -B package"
            }
            post {
                success {
                    archiveArtifacts artifacts: 'target/*.jar', fingerprint: true
                }
            }
        }
    }
}
