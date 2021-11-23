#!/usr/bin/env groovy
import com.ifi.jenkins.Docker
import com.ifi.jenkins.K8s
import com.ifi.jenkins.Git

def call(Map args) {
  def gDocker = new Docker()
  def k8s = new K8s()
//  node("agent1") {
//    docker.image('nstung219/agent-image:1.2').inside {
//      stage("build image") {
//        gDocker.build("product-order-service:release-1.0")
//      }
//      stage("push image") {
//        gDocker.push("gcr.io/jenkins-demo-330307", "product-order-service:release-1.0")
//      }
//    }
//  }

  podTemplate(label: "kubepod", cloud: 'kubernetes', containers: [
    containerTemplate(name: 'jnlp', image: 'nstung219/k8s-agent:1.5')
  ]) {
    node ("kubepod") {
      def git = new Git()
      stage("deploy") {
//        git.clone("TungNguyenSi/ProductOrderService.git")
        k8s.auth()
        k8s.createMongoSecrets()
        sh "ls"
        sh "kubectl apply -f mongo-deploy.yaml"
//        k8s.apply("-f mongo-deploy.yaml")
//        k8s.apply("-f product-order-service-deploy.yaml")
        def mongoVerify = k8s.verifyRunningPods("mongo")
        def serverVerify = k8s.verifyRunningPods("server")

        if (mongoVerify == false || serverVerify == false){
          currentBuild.result = "FAILURE"
        }
      }
    }
  }
}