pipeline {
    agent any

    tools {
        // Project uses Spring Boot 3.x and compiles with Java 21 locally.
        // Update this tool name to a JDK installation available in your Jenkins (e.g. 'JDK21').
        // If your Jenkins only provides a different name for JDK 21 (or 17), change it accordingly.
        jdk 'JDK21'
        maven 'Maven3'
    }

    environment {
        // Replace the placeholder below with your SonarQube server URL or set this value
        // in Jenkins global environment variables. Example: 'https://sonar.mycompany.com'
        SONAR_HOST_URL = 'http://localhost:9000'
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
                sh 'mvn -B clean compile'
            }
        }

        stage('Unit Tests (with mocks)') {
            steps {
                sh 'mvn -B test'
            }
            post {
                always {
                    junit '**/target/surefire-reports/*.xml'
                }
            }
        }

        stage('SonarQube Analysis') {
            steps {
                // Bind a secret text credential (create a Secret Text credential in Jenkins with id 'SonarQube_jenkins')
                withCredentials([string(credentialsId: 'SonarQube_jenkins', variable: 'SONAR_TOKEN')]) {
                    sh '''#!/bin/bash
set -euo pipefail

echo "Running Maven Sonar analysis..."
mvn -B sonar:sonar -Dsonar.host.url=${SONAR_HOST_URL} -Dsonar.login=${SONAR_TOKEN} 2>&1 | tee sonar-output.txt

# Extract the CE task URL from scanner output
TASK_ID=$(grep -o "api/ce/task?id=[A-Za-z0-9_-]*" sonar-output.txt | head -n1 | cut -d= -f2) || true

if [ -z "${TASK_ID}" ]; then
    echo "ERROR: Could not find Sonar CE task id in mvn output."
    cat sonar-output.txt
    exit 1
fi
echo "Found CE task id: ${TASK_ID}"

# Remove trailing slash from SONAR_HOST_URL if present
SONAR_HOST="${SONAR_HOST_URL%/}"

# Poll CE task until it is finished (max ~2 minutes)
MAX_ATTEMPTS=40
ATTEMPT=0
CE_STATUS=""

while [ $ATTEMPT -lt $MAX_ATTEMPTS ]; do
    ATTEMPT=$((ATTEMPT + 1))
    CE_JSON=$(curl -s -u "${SONAR_TOKEN}:" "${SONAR_HOST}/api/ce/task?id=${TASK_ID}")
    
    # Extract status using grep and cut (no backslashes needed)
    CE_STATUS=$(echo "$CE_JSON" | grep -o '"status":"[^"]*"' | head -n1 | cut -d: -f2 | tr -d '"')
    echo "CE status (attempt ${ATTEMPT}/${MAX_ATTEMPTS}): ${CE_STATUS}"
    
    if [ "${CE_STATUS}" = "SUCCESS" ]; then
        break
    fi
    if [ "${CE_STATUS}" = "FAILED" ] || [ "${CE_STATUS}" = "CANCELED" ]; then
        echo "ERROR: CE task finished with status ${CE_STATUS}"
        echo "$CE_JSON"
        exit 1
    fi
    sleep 3
done

if [ "${CE_STATUS}" != "SUCCESS" ]; then
    echo "ERROR: Timed out waiting for Sonar analysis to complete"
    exit 1
fi

# Extract analysisId using grep and cut
ANALYSIS_ID=$(echo "$CE_JSON" | grep -o '"analysisId":"[^"]*"' | head -n1 | cut -d: -f2 | tr -d '"')
if [ -z "${ANALYSIS_ID}" ]; then
    echo "ERROR: no analysisId available from CE task response"
    echo "$CE_JSON"
    exit 1
fi
echo "Analysis id: ${ANALYSIS_ID}"

# Query quality gate for the analysis
QG_JSON=$(curl -s -u "${SONAR_TOKEN}:" "${SONAR_HOST}/api/qualitygates/project_status?analysisId=${ANALYSIS_ID}")
QG_STATUS=$(echo "$QG_JSON" | grep -o '"status":"[^"]*"' | head -n1 | cut -d: -f2 | tr -d '"')
echo "Quality gate status: ${QG_STATUS}"

if [ "${QG_STATUS}" != "OK" ]; then
    echo "Quality Gate did not pass: ${QG_STATUS}"
    echo "$QG_JSON"
    exit 1
fi
echo "Quality Gate passed!"
'''
                }
            }
        }

        stage('Package') {
            steps {
                sh 'mvn -B package -DskipTests'
            }
            post {
                success {
                    archiveArtifacts artifacts: 'target/*.jar', fingerprint: true
                }
            }
        }
    }
}
