@Library('shared-library-inkafarma') _
import com.inkafarma.code.lib.*;
General sonarQube =new General(this, 'sonarqube-server')


pipeline {
agent { label 'slave1'}
options {ansiColor('xterm')}
stages {
  stage ('Build application') {
  steps {
  sh "mvn clean install -Dmaven.test.skip=true"
  }
  }
            
  stage('do SonarQube analysis') {
  steps {
  script {
  env.GIT_REPO_NAME= scm.getUserRemoteConfigs()[0].getUrl().tokenize('/')[3].split("\\.")[0]
  env.SONARPATH = "/home/centos/sonarqube/sonar-scanner/bin/sonar-scanner"
  env.EXTRAS = "-Dsonar.sources=./src/ -Dsonar.sourceEncoding=UTF-8 -Dsonar.java.source=8 -Dsonar.java.binaries=target/classes -Dsonar.language=java"   
  sonarQube.analyzeWith(SONARPATH)
  sonarQube.checkQualityGate('userpass-jenkins','userpass-sonar');    
  }
   
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
