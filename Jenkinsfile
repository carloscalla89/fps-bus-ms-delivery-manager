@Library('shared-library-inkafarma') _
import com.inkafarma.code.lib.*;
General sonarQube =new General(this, 'sonarqube-server')


pipeline {
agent { label 'slave1'}
options {ansiColor('xterm')}
stages {
  stage ('Build application') {
  steps {
    sh '''
    set +x
    source "$HOME/.sdkman/bin/sdkman-init.sh"
    sdk list java
    sdk use java 11.0.12-open
    echo $JAVA_HOME
    source /etc/profile.d/maven.sh
    mvn -v
    mvn clean install -f pom.xml -Dmaven.test.skip=true
    '''
  }
  }
            
  stage ('Create docker image for ECR') {
  steps {
  script {
  sonarQube.BuildandPushECR('deliverymanager-service','maven')
  }
  }
  }
 
 stage ('Create docker image for GCP') {
 steps {
 script {
 sonarQube.BuildandPushGCP('deliverymanager-service','maven')
 }
 }
 }
            
                     
}
}
