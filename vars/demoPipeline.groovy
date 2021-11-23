#!/usr/bin/env groovy
import com.ifi.jenkins.Docker
import com.ifi.jenkins.K8s

def call(Map args) {
  def docker = new Docker()
  def k8s = new K8s()
  node("agent1") {
    docker.image('nstung219/agent-image:1.2').inside {
      stage("build image") {
        docker.build("product-order-service:release-1.0")
      }
      stage("push image") {
        docker.push("gcr.io/jenkins-demo-330307", "product-order-service:release-1.0")
      }
    }
    stage("deploy") {
      k8s.auth()
      k8s.createMongoSecrets()
      k8s.apply("-f mongo-deploy.yaml")
      k8s.apply("-f product-order-service-deploy.yaml")
      def mongoVerify = k8s.verifyRunningPods("mongo")
      def serverVerify = k8s.verifyRunningPods("server")

      if (mongoVerify == false || serverVerify == false){
        currentBuild.result = "FAILURE"
      }
    }
  }
}