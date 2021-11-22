#!/usr/bin/env groovy
package com.ifi.jenkins

class Docker implements Serializable {

    private final def script

    Docker(def script) {
        this.script = script;
    }

    def build(String imageName) {
        def buildCommand = "docker build -t ${imageName} ."
        echo buildCommand
        this.script.sh(script: buildCommand, returnStdout: true)
    }

    def login() {
        withVault(configuration: [timeout: 60, vaultCredentialId: 'vault-jenkins-approle', vaultUrl: 'http://34.93.200.114:8200'],
            vaultSecrets: [
                [path: 'secrets/creds/nstung219-dockerhub', engineVersion: 1, secretValues: [
                    [envVar: 'dockerHubUsername', vaultKey: 'username'],
                    [envVar: 'dockerHubPassword', vaultKey: 'password']]
                ]
            ]) {
            this.script.sh("echo ${dockerHubPassword} | docker login -u ${dockerHubUsername} --password-stdin");
        }
    }
}
