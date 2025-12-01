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

                stage('SonarQube Analysis') {  // run mvn sonar:sonar, capture CE task, poll Sonar and check quality gate via API
                        steps {
                                // Bind a secret text credential (create a Secret Text credential in Jenkins with id 'SonarQube_jenkins')
                                withCredentials([string(credentialsId: 'SonarQube_jenkins', variable: 'SONAR_TOKEN')]) {
                                        sh '''#!/bin/bash
set -euo pipefail
echo "Running Maven Sonar analysis and capturing output..."
# run sonar and tee output so we can extract the CE task id
mvn -B sonar:sonar -Dsonar.host.url=${SONAR_HOST_URL} -Dsonar.login=${SONAR_TOKEN} 2>&1 | tee sonar-output.txt

# try to extract the CE task id from scanner output
TASK_ID=$(grep -oE 'api/ce/task\?id=[A-Za-z0-9_-]+' sonar-output.txt | head -n1 | sed 's/.*id=//') || true
if [ -z "${TASK_ID}" ]; then
    echo "ERROR: Could not find Sonar CE task id in mvn output. Full output:" >&2
    tail -n +1 sonar-output.txt >&2
    exit 1
fi
echo "Found CE task id: ${TASK_ID}"

# poll CE task until it's finished
CE_STATUS=""
SONAR_HOST="${SONAR_HOST_URL%/}"
while true; do
    CE_JSON=$(curl -s -u ${SONAR_TOKEN}: "${SONAR_HOST}/api/ce/task?id=${TASK_ID}")
    CE_STATUS=$(echo "$CE_JSON" | grep -oP '"status":"\K[^"]+') || true
    echo "CE status: ${CE_STATUS}"
    if [ "${CE_STATUS}" = "SUCCESS" ]; then
        break
    fi
    if [ "${CE_STATUS}" = "FAILED" ] || [ "${CE_STATUS}" = "CANCELED" ]; then
        echo "ERROR: CE task finished with status ${CE_STATUS}" >&2
        echo "$CE_JSON" >&2
        exit 1
    fi
    sleep 3
done

# get analysisId for quality gate check
ANALYSIS_ID=$(echo "$CE_JSON" | grep -oP '"analysisId":"\K[^"]+' || true)
if [ -z "${ANALYSIS_ID}" ]; then
    echo "ERROR: no analysisId available from CE task response" >&2
    exit 1
fi
echo "Analysis id: ${ANALYSIS_ID}"

# query quality gate for the analysis
QG_JSON=$(curl -s -u ${SONAR_TOKEN}: "${SONAR_HOST}/api/qualitygates/project_status?analysisId=${ANALYSIS_ID}")
QG_STATUS=$(echo "$QG_JSON" | grep -oP '"status":"\K[^"]+' || true)
echo "Quality gate status: ${QG_STATUS}"
if [ "${QG_STATUS}" != "OK" ]; then
    echo "Quality Gate did not pass: ${QG_STATUS}" >&2
    echo "$QG_JSON" >&2
    exit 1
fi
echo "Quality Gate passed: ${QG_STATUS}"
'''
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
