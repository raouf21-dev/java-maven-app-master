def gv

pipeline {   
    agent any
    tools {
        maven 'Maven'
    }
    environment {
        DOCKER_SERVER   = "705754325868.dkr.ecr.eu-west-3.amazonaws.com"
        DOCKER_REPO     = "${DOCKER_SERVER}/java-maven-app"
    }
    stages {
        stage("init") {
            steps {
                script {
                    gv = load "script.groovy"
                }
            }
        }
        stage("Increment version") {
            steps {
                script {
                    gv.incrementVersion()
                }
            }
        }
        stage("build jar") {
            steps {
                script {
                    gv.buildJar()
                }
            }
        }

        stage("build image") {
            steps {
                script {
                    gv.buildImage()
                }
            }
        }

        stage("deploy") {
            // steps {
            //     script {
            //         gv.deployApp()
            //     }
            // }
            
            environment {
                    AWS_ACCESS_KEY_ID = credentials('jenkins_aws_access_key_id')
                    AWS_SECRET_ACCESS_KEY = credentials('jenkins_aws_secret_access_key')
                    APP_NAME = 'java-maven-app'
                }
                steps {
                    script{
                        echo 'deploying the application...'
                        sh 'envsubst < k8s/deployment.yaml | kubectl apply -f -'
                        sh 'envsubst < k8s/service.yaml | kubectl apply -f -'
                    }
                }
        }    
        
        stage("Commit to Github"){
            steps{
                script{
                    gv.commitToGithub()
                }
            }
        }
    }
} 
