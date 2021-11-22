#!/usr/bin/env groovy
def call(Map args){
    node ("agent1"){
        def docker = args.jsl.com.ifi.jenkins.Docker.new(this)
        def git = args.jsl.com.ifi.jenkins.Git.new(this)
    }
    return this
}

def buildImage() {
    node ("agent1") {
        stage("pull code") {
            steps {
                git.clone("https://github.com/TungNguyenSi/ProductOrderService.git")
            }
        }
        stage("build image") {
            steps {
                docker.build("gcr.io/jenkins-demo-330307/product-order-service:release-1.0")
            }
        }
    }
    return this
}