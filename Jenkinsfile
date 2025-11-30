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

stage('SonarQube Analysis (hard-coded token)') {
  steps {
    sh """
      mvn clean verify sonar:sonar \
        -Dsonar.projectKey=projetdevops \
        -Dsonar.host.url=http://localhost:9000 \
        -Dsonar.login=sqa_3db0d9a3db4cda782af931dcde6678f99843cfe0
    """
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
