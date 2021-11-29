package com.ifi.jenkins

def auth() {
  withVault(
    configuration: [timeout: 60, vaultCredentialId: 'vault-jenkins-approle', vaultUrl: 'http://34.126.70.118:8200'],
    vaultSecrets: [
      [path: 'secrets/creds/gcloud-service-account', secretValues: [
        [envVar: 'gcloudAccount', vaultKey: 'data']]
      ]
    ]) {
    writeFile(file: 'keyFile.json', text: gcloudAccount)
    sh '''
      gcloud auth activate-service-account --key-file keyFile.json
      gcloud container clusters get-credentials cluster-2 --zone asia-southeast1-c --project jenkins-demo-330307
    '''
  }
}
def verifyRunningPods(String deploymentName, String timeout){
  sh(
    script: "kubectl rollout status deployment ${deploymentName} --watch --timeout=${timeout}",
  )
}