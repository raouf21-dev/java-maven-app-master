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
def buildImage() {
    echo "building the docker image..."
    withCredentials([usernamePassword(credentialsId: 'docker-hub-repo', passwordVariable: 'PASS', usernameVariable: 'USER')]) {
        sh 'docker build -t santana20095/demo-app:jma-2.0 .'
        sh 'echo $PASS | docker login -u $USER --password-stdin'
        sh 'docker push santana20095/demo-app:jma-2.0'
    }
}

def deployApp() {
    echo 'deploying the application...'
}

def commitToGithub(){
    withCredentials([usernamePassword(credentialsId: 'github-credentials', passwordVariable: 'PASS', usernameVariable: 'USER')]) {
        sh 'git config --global user.email "raouf_devops@gmail.com"'
        sh 'git config --global user.name "Raouf"'
        sh '''
            git config --global credential.helper '!f() { echo username=$USER; echo password=$PASS; }; f'
            git remote set-url origin https://github.com/raouf21-dev/java-maven-app-master.git
            git add .
            git commit -m "ci: version bump"
            git push origin HEAD:main
        '''
    }
}

return this
