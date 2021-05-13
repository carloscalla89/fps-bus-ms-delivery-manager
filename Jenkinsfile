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
            
  stage('view SonarQube parameters') {
  steps {   
  script {
  env.GIT_REPO_NAME= scm.getUserRemoteConfigs()[0].getUrl().tokenize('/')[3].split("\\.")[0]
  }
  withSonarQubeEnv('sonarqube-server') {     
  sh "printenv"
  }
  }  
  }
            
  stage('do SonarQube analysis') {
  steps {    
  withSonarQubeEnv('sonarqube-server') {
  sh "export JAVA_HOME='/usr/lib/jvm/java-11-openjdk-11.0.5.10-0.el7_7.x86_64' && /home/centos/sonarqube/sonar-scanner/bin/sonar-scanner -Dsonar.sources=./src/ -Dsonar.sourceEncoding=UTF-8 -Dsonar.java.source=8 -Dsonar.java.libraries.empty=true -Dsonar.java.binaries=target/classes -Dsonar.language=java -Dsonar.login=${env.SONAR_AUTH_TOKEN} -Dsonar.projectKey=${env.GIT_REPO_NAME} -Dsonar.projectName=${env.GIT_REPO_NAME} -Dsonar.host.url=${env.SONAR_HOST_URL}"
  } 
  }
  }  
            
            /*
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
            */
        }
}
