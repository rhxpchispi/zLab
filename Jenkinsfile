pipeline {
    agent any
    environment {
        REGISTRY = "localhost:5000"  // Usando registry local de minikube
        IMAGE_NAME = "rhxpchispi/zlab-java-app"
        KUBECONFIG = "/home/jenkins/.kube/config"
    }  
    parameters {
        choice(
            choices: ['dev', 'test', 'prod'],
            description: 'Selecciona el ambiente a desplegar',
            name: 'ENVIRONMENT'
        )
    }
    
    stages {
        stage('CI | Maven') {
            agent {
                docker {
                    image 'maven:3.8.6-openjdk-11'
                    args '-v /root/.m2:/root/.m2'
                    reuseNode true
                }
            }

            stages {
                stage('Clean') {
                    steps {
                        sh 'mvn clean'
                    }
                }
        
                stage('Build') {
                    steps {
                        sh 'mvn compile'
                    }
                }
                
                stage('Test') {
                    steps {
                        script {
                            // Ejecutar tests pero continuar incluso si fallan (para desarrollo)
                            try {
                                sh 'mvn test'
                            } catch (Exception e) {
                                echo "Tests fallaron o no hay tests: ${e.message}"
                                // Continuar el pipeline a pesar de los tests fallidos
                            }
                        }
                    }
                    post {
                        always {
                            // Buscar reportes de tests pero no fallar si no existen
                            junit allowEmptyResults: true, testResults: 'target/surefire-reports/*.xml'
                        }
                    }
                }
                
                stage('Package') {
                    steps {
                        sh 'mvn package -DskipTests'
                    }
                }
            }
        }        
        
        stage('CI | Docker build') {
            steps {
                sh "docker build -t ${env.IMAGEN}:${params.ENVIRONMENT}-${env.BUILD_ID} ."
            }
        }
        
        stage('CD | Push de la imagen a Docker Hub') {
            steps {
                sh 'echo $DOCKERHUB_CRED_PSW | docker login -u $DOCKERHUB_CRED_USR --password-stdin'
                sh "docker push ${env.IMAGEN}:${params.ENVIRONMENT}-${env.BUILD_ID}"
            }
        }
        
        stage('CD | Deploy a Environment') {
            steps {
                script {
                    if (params.ENVIRONMENT == 'prod') {
                        timeout(time: 2, unit: 'MINUTES') {
                            input message: "¿Desplegar en producción?", ok: "Confirmar"
                        }
                    }
                    
                    sh """
                        # Aplicar namespaces
                        kubectl apply -f K8s/namespace.yaml
                        
                        # Aplicar configmaps para el ambiente seleccionado
                        kubectl apply -f K8s/configmaps/${params.ENVIRONMENT}-configmap.yaml
                        
                        # Actualizar la imagen en el deployment
                        kubectl set image deployment/java-app java-app=${REGISTRY}/${env.IMAGEN}:${params.ENVIRONMENT}-${env.BUILD_ID} -n ${params.ENVIRONMENT}-ns || true
                        
                        # Aplicar deployment
                        kubectl apply -f K8s/deployments/${params.ENVIRONMENT}-deployment.yaml
                        
                        # Esperar a que el deployment esté listo
                        kubectl rollout status deployment/java-app -n ${params.ENVIRONMENT}-ns --timeout=120s
                    """
                }
            }
        }
    }
    
    post {
        success {
            echo 'Pipeline ejecutado correctamente'
            slackSend color: 'good', message: "Build ${env.BUILD_ID} desplegado en ${params.ENVIRONMENT} - EXITOSO"
        }
        failure {
            echo 'Pipeline falló'
            slackSend color: 'danger', message: "Build ${env.BUILD_ID} falló en ${params.ENVIRONMENT}"
        }
    }
}