#!/usr/bin/env groovy
def call(Map args){
    node ("agent1"){
        def docker = args.jsl.com.ifi.jenkins.Docker.new(this)
        def git = args.jsl.com.ifi.jenkins.Git.new(this)
        stage("pull code") {
            withVault(configuration: [timeout: 60, vaultCredentialId: 'vault-jenkins-approle', vaultUrl: 'http://34.126.70.118:8200'],
                vaultSecrets: [
                    [path: 'secrets/creds/Tung.NguyenSi-github', engineVersion: 1, secretValues: [
                        [envVar: 'githubUsername', vaultKey: 'username'],
                        [envVar: 'githubPassword', vaultKey: 'password']]
                    ]
                ]) {
                git.clone("TungNguyenSi/ProductOrderService.git", githubUsername, githubPassword)
            }

        }
        stage("build image") {
            docker.build("gcr.io/jenkins-demo-330307/product-order-service:release-1.0")
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