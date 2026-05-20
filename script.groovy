def buildJar() {
    echo 'building the application...'
    sh 'mvn package'
}

def incrementVersion(){
    echo 'incrementing app version...'
            sh 'mvn build-helper:parse-version versions:set \
                -DnewVersion=\\\${parsedVersion.majorVersion}.\\\${parsedVersion.minorVersion}.\\\${parsedVersion.nextIncrementalVersion} \
                versions:commit'
            def matcher = readFile('pom.xml') =~ '<version>(.+)</version>'
            def version = matcher[0][1]
            env.IMAGE_NAME = "$version-$BUILD_NUMBER"
}

// for docker hub
// def buildImage() {
//     echo "building the docker image..."
//     withCredentials([usernamePassword(credentialsId: 'docker-hub-repo', passwordVariable: 'PASS', usernameVariable: 'USER')]) {
//         sh "docker build -t santana20095/demo-app:${env.IMAGE_NAME} ."
//         sh 'echo $PASS | docker login -u $USER --password-stdin'
//         sh "docker push santana20095/demo-app:${env.IMAGE_NAME}"
//     }
// }

// for AWS ECR
def buildImage() {
    echo "building the docker image..."
    withCredentials([usernamePassword(credentialsId: 'ecr-credentials', passwordVariable: 'PASS', usernameVariable: 'USER')]) {
        sh "docker build -t java-maven-app ."
        sh "docker tag java-maven-app:latest 705754325868.dkr.ecr.eu-west-3.amazonaws.com/java-maven-app:${env.IMAGE_NAME}"
        // sh 'echo $PASS | docker login -u $USER --password-stdin'
        sh "docker push 705754325868.dkr.ecr.eu-west-3.amazonaws.com/java-maven-app:${env.IMAGE_NAME}"
    }
}


def deployApp() {
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

def commitToGithub(){
    withCredentials([usernamePassword(credentialsId: 'eks-github', passwordVariable: 'PASS', usernameVariable: 'USER')]) {
        sh 'git config --global user.email "raouf_devops@gmail.com"'
        sh 'git config --global user.name "Raouf"'
        sh '''
            git config --global credential.helper '!f() { echo username=$USER; echo password=$PASS; }; f'
            git remote set-url origin https://github.com/raouf21-dev/java-maven-app-master.git
            git pull origin jenkins-jobs
            git add .
            git commit -m "ci: version bump"
            git push origin HEAD:jenkins-jobs
        '''
    }
}

return this
