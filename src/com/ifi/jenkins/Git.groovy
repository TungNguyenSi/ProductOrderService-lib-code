package com.ifi.jenkins

class Git implements Serializable{
    private final def script

    Git (def script) {
        this.script = script
    }

    void checkout(def branch) {
        this.script.sh("git checkout ${branch}");
    }

    boolean isMain(){
        script.env.BRANCH_NAME == 'main'
    }

    def clone(){
        script.withVault(configuration: [timeout: 60, vaultCredentialId: 'vault-jenkins-approle', vaultUrl: 'http://34.126.70.118:8200'],
            vaultSecrets: [
                [path: 'secrets/creds/Tung.NguyenSi-github', engineVersion: 1, secretValues: [
                    [envVar: 'githubUsername', vaultKey: 'username'],
                    [envVar: 'githubPassword', vaultKey: 'password']]
                ]
            ]) {
            script.sh("git clone https://${githubUsername}:${githubPassword}@github.com/TungNguyenSi/ProductOrderService.git")
        }     
    }
}
