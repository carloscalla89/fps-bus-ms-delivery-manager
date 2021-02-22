pipeline {
    agent { label 'slave1'}
    options {
        ansiColor('xterm')
    }
        stages {
            stage ('Build application') {
                steps {
                    sh "mvn clean install -Dmaven.test.skip=true"
                }
            }
           
            stage ('Create docker image') {
                steps {
                    sh '$(aws ecr get-login --no-include-email --region us-west-2)'
                    sh '''
                    VERSION=$(grep "version" target/maven-archiver/pom.properties|cut -d"=" -f2)
                    TAG=$(git rev-parse --short HEAD)
                    TAG="v$VERSION-bld-$TAG-$BUILD_NUMBER"
                    docker build -t $ECR_URL/deliverymanager-service:$TAG -f devops/Dockerfile .
                    docker push $ECR_URL/deliverymanager-service:$TAG
                    '''
                }

            }
        }
}