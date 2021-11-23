#!/usr/bin/env groovy
package com.ifi.jenkins

def build(String imageName) {
  docker.image('nstung219/agent-image:1.2').inside {
    def buildCommand = "docker build -t ${imageName} ."
    sh(script: buildCommand, returnStdout: true)
  }
}

def login(String path) {
  withVault(configuration: [timeout: 60, vaultCredentialId: 'vault-jenkins-approle', vaultUrl: 'http://34.126.70.118:8200'],
    vaultSecrets: [
      [path: path, engineVersion: 1, secretValues: [
        [envVar: 'dockerHubUsername', vaultKey: 'username'],
        [envVar: 'dockerHubPassword', vaultKey: 'password']]
      ]
    ]) {
    sh("echo \${dockerHubPassword} | docker login -u \${dockerHubUsername} --password-stdin");
  }
}

def push(String host, String imageName) {
  docker.image('nstung219/agent-image:1.2').inside {
    login("'secrets/creds/nstung219-dockerhub'")
    sh "gcloud auth configure-docker"
    sh "docker tag ${imageName} ${host}/${imageName}"
    sh "docker push ${host}/${imageName}"
  }
}
