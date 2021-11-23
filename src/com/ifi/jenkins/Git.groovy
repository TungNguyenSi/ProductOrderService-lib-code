package com.ifi.jenkins
void checkout(def branch) {
  sh("git checkout ${branch}");
}

boolean isMain(){
  env.BRANCH_NAME == 'main'
}

void clone(def repo){
  withVault(configuration: [timeout: 60, vaultCredentialId: 'vault-jenkins-approle', vaultUrl: 'http://34.126.70.118:8200'],
    vaultSecrets: [
      [path: 'secrets/creds/Tung.NguyenSi-github', engineVersion: 1, secretValues: [
        [envVar: 'githubUsername', vaultKey: 'username'],
        [envVar: 'githubPassword', vaultKey: 'password']]
      ]
    ]) {
    sh("echo \${githubPassword} | git clone https://\${githubUsername}:@github.com/${repo} --password-stdin")
  }
}
