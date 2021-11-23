#!/usr/bin/env groovy
package com.ifi.jenkins

def build(String imageName) {
    Docker.image('nstung219/agent-image:1.2').inside {
        def buildCommand = "docker build -t ${imageName} ."
        sh(script: buildCommand, returnStdout: true)
    }
}

def login() {
    withVault(configuration: [timeout: 60, vaultCredentialId: 'vault-jenkins-approle', vaultUrl: 'http://34.126.70.118:8200'],
        vaultSecrets: [
            [path: 'secrets/creds/nstung219-dockerhub', engineVersion: 1, secretValues: [
                [envVar: 'dockerHubUsername', vaultKey: 'username'],
                [envVar: 'dockerHubPassword', vaultKey: 'password']]
            ]
        ]) {
        sh("echo \${dockerHubPassword} | docker login -u \${dockerHubUsername} --password-stdin");
    }
}

def push(){
    Docker.image('nstung219/agent-image:1.2').inside {
        login()
        sh "gcloud auth configure-docker"
    }
}
