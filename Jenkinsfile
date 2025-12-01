pipeline {
    agent any

    tools {
        // Project uses Spring Boot 3.x and compiles with Java 21 locally.
        // Update this tool name to a JDK installation available in your Jenkins (e.g. 'JDK21').
        // If your Jenkins only provides a different name for JDK 21 (or 17), change it accordingly.
        jdk 'JDK21'
        maven 'Maven3'
    }

    // Sonar: two common options:
    // A) Configure a SonarQube installation in Jenkins (Manage Jenkins → Global Tool Configuration)
    //    and call `withSonarQubeEnv('your-installation-name')` (this is the existing approach
    //    but it requires the installation name to exist in Jenkins which caused the error).
    // B) (Preferred here) Don't rely on a Jenkins tool installation. Use Maven's Sonar goal and
    //    pass the Sonar host URL and a secret token from Jenkins credentials. This works even
    //    when Jenkins has no SonarQube installation configured.
    environment {
        // Replace the placeholder below with your SonarQube server URL or set this value
        // in Jenkins global environment variables. Example: 'https://sonar.mycompany.com'
        SONAR_HOST_URL = 'http://localhost:9000/'
        // Note: the SONAR_TOKEN is expected to be a Jenkins "Secret text" credential with id
        // 'SONAR_TOKEN'. We'll read it with `withCredentials` in the Sonar stage.
    }

    stages {
      stage('Print env / tool info (debug)') {
            steps {
                echo "=== Debug: Java & Maven versions and environment ==="
                sh 'java -version'
                sh 'javac -version'
                sh 'mvn -v'
                sh 'printenv'
            }
        }
        stage('Checkout') {
            steps {
                git branch: 'main', url: 'https://github.com/naderite/projet-devops.git'
            }
        }

        stage('Build with Maven') {
            steps {
                sh "mvn -B clean compile"
            }
        }

        stage('Unit Tests (with mocks)') {
            steps {
                sh "mvn -B test -DskipTests=false"
            }
            post {
                always {
                    junit '**/target/surefire-reports/*.xml'
                }
            }
        }

        stage('SonarQube Analysis') {  // use maven sonar:sonar with a token (no Jenkins Sonar tool required)
            steps {
                // Bind a secret text credential (create a Secret Text credential in Jenkins with id 'SonarQube_jenkins')
                withCredentials([string(credentialsId: 'SonarQube_jenkins', variable: 'SonarQube_jenkins')]) {
                    // Run Sonar via Maven plugin. This avoids depending on a configured SonarQube installation in Jenkins.
                    sh "mvn -B sonar:sonar -Dsonar.host.url=${env.SONAR_HOST_URL} -Dsonar.login=${SonarQube_jenkins}"
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
