#!/usr/bin/env groovy
import com.ifi.jenkins.Docker
import com.ifi.jenkins.K8s

def call() {
  def gDocker = new Docker()
  def k8s = new K8s()
//  node("agent1") {
//    docker.image('nstung219/agent-image:1.2').inside {
//      stage("build image") {
//        checkout scm
//        gDocker.build("product-order-service:release-1.0")
//      }
//      stage("push image") {
//        gDocker.push("gcr.io/jenkins-demo-330307", "product-order-service:release-1.0")
//      }
//    }
//  }
  podTemplate(annotations: [podAnnotation(key: 'vault.hashicorp.com/agent-inject', value: 'true'), podAnnotation(key: 'vault.hashicorp.com/role', value: 'webapp'), podAnnotation(key: 'vault.hashicorp.com/agent-inject-secrets-gcloud.json', value: 'secrets/creds/gcloud-service-account')], cloud: 'kubernetes', label: "kaniko", containers: [containerTemplate(image: 'gcr.io/kaniko-project/executor:latest', name: 'kaniko', workingDir: '/home/jenkins/agent')], serviceAccount: 'vault-auth') {
    node ("kaniko") {
      container(name: "kaniko", shell: "/busybox/sh") {
        checkout scm
        sh 'ls -la'
        sh '''#!/busybox/sh
            echo "FROM jenkins/inbound-agent:latest" > Dockerfile
            /kaniko/executor --context `pwd` --destination=gcr.io/jenkins-demo-330307/product-order-service:release-1.0
        '''
      }
    }
  }

//  podTemplate(label: "kubepod", cloud: 'kubernetes', containers: [
//    containerTemplate(name: 'jnlp', image: 'nstung219/k8s-agent:1.5')
//  ]) {
//    node ("kubepod") {
//      stage("deploy") {
//        checkout scm
//        k8s.auth()
//        createMongoSecrets(k8s)
//        k8s.apply("-f mongo-deploy.yaml")
//        k8s.apply("-f product-order-service-deploy.yaml")
//
//        def mongoVerify = k8s.verifyRunningPods("mongo")
//        def serverVerify = k8s.verifyRunningPods("server")
//        if (mongoVerify == false || serverVerify == false){
//          currentBuild.result = "FAILURE"
//        }
//      }
//    }
//  }
}

def createMongoSecrets(K8s k8s){
  withVault(
    configuration: [timeout: 60, vaultCredentialId: 'vault-jenkins-approle', vaultUrl: 'http://34.126.70.118:8200'],
    vaultSecrets: [
      [path: 'secrets/creds/mongodb', secretValues: [
        [envVar: 'mongoUser', vaultKey: 'username'],
        [envVar: 'mongoPassword', vaultKey: 'password']]
      ],
    ]) {
    k8s.createSecretsFromLiteral("mongodb-secret", ["username=\${mongoUser}", "password=\${mongoPassword}"])
  }
}