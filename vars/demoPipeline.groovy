#!/usr/bin/env groovy

import com.ifi.jenkins.Docker

def call(Map args){
    node ("agent1"){
        def docker = new Docker()
//        def git = args.jsl.com.ifi.jenkins.Git.new(this)
        stage("build image") {
            docker.build("product-order-service:release-1.0")
        }
        stage("push image") {
            docker.push("gcr.io/jenkins-demo-330307", "product-order-service:release-1.0")
        }
    }
    return this
}

//def buildImage() {
//    node ("agent1") {
//        stage("pull code") {
//            git.clone("https://github.com/TungNguyenSi/ProductOrderService.git")
//        }
//        stage("build image") {
//            docker.build("gcr.io/jenkins-demo-330307/product-order-service:release-1.0")
//        }
//    }
//    return this
//}
