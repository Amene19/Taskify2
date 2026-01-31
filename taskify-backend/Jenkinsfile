pipeline {
    agent any

    tools {
        maven 'Maven-3.9'
    }

    options {
        timestamps()
        timeout(time: 1, unit: 'HOURS')
        buildDiscarder(logRotator(numToKeepStr: '10'))
    }

    triggers {
        githubPush()  // Trigger on GitHub webhook push events
        // Fallback polling to keep builds running even if webhook breaks
        pollSCM('H/2 * * * *')
    }

    environment {
        APP_NAME = 'taskify-backend'
        DOCKER_REGISTRY = 'docker.io'
        DOCKER_IMAGE_NAME = "${DOCKER_REGISTRY}/${DOCKER_USERNAME}/${APP_NAME}"
        SONAR_PROJECT_KEY = 'taskify-backend'
        SLACK_CHANNEL = '#builds'

        // Application Configuration (from Jenkins credentials)
        SPRING_APPLICATION_NAME = 'taskify-backend'
        SERVER_PORT = '8080'

        // Database Configuration
        SPRING_DATASOURCE_URL = 'jdbc:h2:mem:testdb'
        SPRING_DATASOURCE_USERNAME = 'sa'
        SPRING_DATASOURCE_PASSWORD = ''

        // JWT Configuration (loaded from Jenkins credentials)
        JWT_SECRET = credentials('JWT_SECRET')
        JWT_EXPIRATION = '86400000'

        // Test Environment Variables
        TEST_JWT_SECRET = credentials('TEST_JWT_SECRET')
        TEST_JWT_EXPIRATION = '3600000'
        TEST_USER_EMAIL = 'test@example.com'
        TEST_USER_PASSWORD = 'password123'
        TEST_USER_NEW_EMAIL = 'newuser@example.com'
        TEST_USER_EXISTING_EMAIL = 'existing@example.com'
    }

    stages {
        stage('Detect OS') {
            steps {
                script {
                    // Detect if running on Windows or Unix
                    if (isUnix()) {
                        env.IS_UNIX = 'true'
                        echo "Detected Unix/Linux environment"
                    } else {
                        env.IS_UNIX = 'false'
                        echo "Detected Windows environment"
                    }
                }
            }
        }

        stage('Checkout') {
            steps {
                script {
                    echo "Checking out source code from SCM..."
                }
                checkout scm
                script {
                    echo "Loading .env file from Jenkins workspace..."

                    if (env.IS_UNIX == 'true') {
                        // Unix/Linux - use sh and echo
                        sh '''
                            echo "SPRING_APPLICATION_NAME=${SPRING_APPLICATION_NAME}" > .env
                            echo "SERVER_PORT=${SERVER_PORT}" >> .env
                            echo "SPRING_DATASOURCE_URL=${SPRING_DATASOURCE_URL}" >> .env
                            echo "SPRING_DATASOURCE_USERNAME=${SPRING_DATASOURCE_USERNAME}" >> .env
                            echo "SPRING_DATASOURCE_PASSWORD=${SPRING_DATASOURCE_PASSWORD}" >> .env
                            echo "JWT_SECRET=${JWT_SECRET}" >> .env
                            echo "JWT_EXPIRATION=${JWT_EXPIRATION}" >> .env
                            echo "TEST_JWT_SECRET=${TEST_JWT_SECRET}" >> .env
                            echo "TEST_JWT_EXPIRATION=${TEST_JWT_EXPIRATION}" >> .env
                            echo "TEST_USER_EMAIL=${TEST_USER_EMAIL}" >> .env
                            echo "TEST_USER_PASSWORD=${TEST_USER_PASSWORD}" >> .env
                            echo "TEST_USER_NEW_EMAIL=${TEST_USER_NEW_EMAIL}" >> .env
                            echo "TEST_USER_EXISTING_EMAIL=${TEST_USER_EXISTING_EMAIL}" >> .env
                        '''
                    } else {
                        // Windows - use bat and echo
                        bat """
                            @echo off
                            echo SPRING_APPLICATION_NAME=${SPRING_APPLICATION_NAME}> .env
                            echo SERVER_PORT=${SERVER_PORT}>> .env
                            echo SPRING_DATASOURCE_URL=${SPRING_DATASOURCE_URL}>> .env
                            echo SPRING_DATASOURCE_USERNAME=${SPRING_DATASOURCE_USERNAME}>> .env
                            echo SPRING_DATASOURCE_PASSWORD=${SPRING_DATASOURCE_PASSWORD}>> .env
                            echo JWT_SECRET=${JWT_SECRET}>> .env
                            echo JWT_EXPIRATION=${JWT_EXPIRATION}>> .env
                            echo TEST_JWT_SECRET=${TEST_JWT_SECRET}>> .env
                            echo TEST_JWT_EXPIRATION=${TEST_JWT_EXPIRATION}>> .env
                            echo TEST_USER_EMAIL=${TEST_USER_EMAIL}>> .env
                            echo TEST_USER_PASSWORD=${TEST_USER_PASSWORD}>> .env
                            echo TEST_USER_NEW_EMAIL=${TEST_USER_NEW_EMAIL}>> .env
                            echo TEST_USER_EXISTING_EMAIL=${TEST_USER_EXISTING_EMAIL}>> .env
                        """
                    }
                    echo ".env file created for build"
                }
            }
        }

        stage('Build') {
            steps {
                script {
                    echo "Building the Spring Boot application..."
                    runCommand('mvn clean compile')
                }
            }
        }

        stage('Test') {
            steps {
                script {
                    echo "Running unit tests with JUnit..."
                    runCommand('mvn test')
                }
            }
            post {
                always {
                    junit testResults: 'target/surefire-reports/**/*.xml', allowEmptyResults: true
                    script {
                        echo "Test reports generated"
                    }
                }
            }
        }

        stage('Code Coverage & Analysis') {
            steps {
                script {
                    echo "Running code coverage analysis with JaCoCo..."
                    runCommand('mvn jacoco:report')
                    echo "Coverage report generated in target/site/jacoco/index.html"
                }
            }
        }

        stage('SonarQube Analysis') {
            when {
                branch 'master'
            }
            steps {
                script {
                    echo "Performing SonarQube code quality analysis..."
                    // Uncomment when SonarQube is configured
                    // runCommand("mvn sonar:sonar -Dsonar.projectKey=${SONAR_PROJECT_KEY} -Dsonar.sources=src/main/java -Dsonar.tests=src/test/java -Dsonar.coverage.jacoco.xmlReportPaths=target/site/jacoco/jacoco.xml")
                    echo "SonarQube stage (commented out - configure credentials first)"
                }
            }
        }

        stage('Package') {
            steps {
                script {
                    echo "Packaging application as JAR..."
                    runCommand('mvn package -DskipTests')
                }
            }
            post {
                success {
                    archiveArtifacts artifacts: 'target/*.jar', fingerprint: true
                }
            }
        }

        stage('Docker Preflight') {
            steps {
                script {
                    echo "Checking Docker availability..."

                    def dockerAvailable = false

                    if (env.IS_UNIX == 'true') {
                        // Unix/Linux - check for docker socket and CLI
                        def hasSock = fileExists('/var/run/docker.sock')
                        def hasDocker = (sh(script: 'command -v docker >/dev/null 2>&1', returnStatus: true) == 0)

                        if (!hasDocker) {
                            echo "Docker CLI not found in Jenkins agent."
                        }
                        if (!hasSock) {
                            echo "Docker socket not mounted at /var/run/docker.sock."
                        }
                        dockerAvailable = hasDocker && hasSock
                    } else {
                        // Windows - check if docker command exists
                        def hasDocker = (bat(script: '@docker --version >nul 2>&1', returnStatus: true) == 0)

                        if (!hasDocker) {
                            echo "Docker CLI not found in Jenkins agent."
                        }
                        dockerAvailable = hasDocker
                    }

                    if (dockerAvailable) {
                        runCommand('docker version')
                        echo "Docker is available; enabling Docker stages."
                        env.DOCKER_AVAILABLE = 'true'
                    } else {
                        echo "To enable Docker build/push stages: ensure Docker is installed and accessible."
                        env.DOCKER_AVAILABLE = 'false'
                    }
                }
            }
        }

        stage('Build Docker Image') {
            when {
                environment name: 'DOCKER_AVAILABLE', value: 'true'
            }
            steps {
                script {
                    echo "Building Docker image..."
                    runCommand("docker build -t ${APP_NAME}:${BUILD_NUMBER} .")
                    runCommand("docker tag ${APP_NAME}:${BUILD_NUMBER} ${APP_NAME}:latest")
                    echo "Docker image built: ${APP_NAME}:${BUILD_NUMBER}"
                }
            }
        }

        stage('Push to Registry') {
            when {
                allOf {
                    environment name: 'DOCKER_AVAILABLE', value: 'true'
                    branch 'master'
                }
            }
            steps {
                script {
                    echo "Pushing Docker image to registry..."
                    withCredentials([usernamePassword(
                        credentialsId: 'dockerhub-credentials',
                        usernameVariable: 'DOCKER_USER',
                        passwordVariable: 'DOCKER_PASS'
                    )]) {
                        if (env.IS_UNIX == 'true') {
                            sh 'echo $DOCKER_PASS | docker login -u $DOCKER_USER --password-stdin'
                        } else {
                            bat 'docker login -u %DOCKER_USER% -p %DOCKER_PASS%'
                        }
                        runCommand("docker tag ${APP_NAME}:${BUILD_NUMBER} ${DOCKER_USER}/${APP_NAME}:${BUILD_NUMBER}")
                        runCommand("docker tag ${APP_NAME}:latest ${DOCKER_USER}/${APP_NAME}:latest")
                        runCommand("docker push ${DOCKER_USER}/${APP_NAME}:${BUILD_NUMBER}")
                        runCommand("docker push ${DOCKER_USER}/${APP_NAME}:latest")
                        echo "Docker images pushed to Docker Hub"
                    }
                }
            }
        }
    }

    post {
        always {
            script {
                echo "Cleaning up workspace..."
                cleanWs()
            }
        }

        success {
            script {
                echo "Pipeline execution SUCCESS!"
                // Uncomment when Slack integration is configured
                // slackSend(
                //     channel: "${SLACK_CHANNEL}",
                //     color: 'good',
                //     message: "Build SUCCESS: ${APP_NAME} #${BUILD_NUMBER}\nBranch: ${GIT_BRANCH}",
                //     webhookUrl: "${SLACK_WEBHOOK}"
                // )
            }
        }

        failure {
            script {
                echo "Pipeline execution FAILED!"
                // Uncomment when Slack integration is configured
                // slackSend(
                //     channel: "${SLACK_CHANNEL}",
                //     color: 'danger',
                //     message: "Build FAILED: ${APP_NAME} #${BUILD_NUMBER}\nBranch: ${GIT_BRANCH}\nCheck console output: ${BUILD_URL}console",
                //     webhookUrl: "${SLACK_WEBHOOK}"
                // )
            }
        }

        unstable {
            script {
                echo "Pipeline execution is UNSTABLE"
            }
        }
    }
}

// Helper function to run commands cross-platform
def runCommand(String command) {
    if (env.IS_UNIX == 'true') {
        sh command
    } else {
        bat command
    }
}
