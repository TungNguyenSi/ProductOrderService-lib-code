package com.ifi.jenkins

def auth() {
  withVault(
    configuration: [timeout: 60, vaultCredentialId: 'vault-jenkins-approle', vaultUrl: 'http://34.126.70.118:8200'],
    vaultSecrets: [
      [path: 'secrets/creds/gcloud-service-account', secretValues: [
        [envVar: 'gcloudAccount', vaultKey: 'data']]
      ]
    ]) {
    // some block
    writeFile(file: 'keyFile.json', text: gcloudAccount)
    sh '''
      gcloud auth activate-service-account --key-file keyFile.json
      gcloud container clusters get-credentials cluster-2 --zone asia-southeast1-c --project jenkins-demo-330307
    '''
  }
}

def createMongoSecrets() {
  withVault(
    configuration: [timeout: 60, vaultCredentialId: 'vault-jenkins-approle', vaultUrl: 'http://34.126.70.118:8200'],
    vaultSecrets: [
      [path: 'secrets/creds/mongodb', secretValues: [
        [envVar: 'mongoUser', vaultKey: 'username'],
        [envVar: 'mongoPassword', vaultKey: 'password']]
      ],
    ]) {
    sh "kubectl create secret generic mongodb-secret --save-config --dry-run=client --from-literal=username=\${mongoUser} --from-literal=password=\${mongoPassword} -o yaml | kubectl apply -f -"
  }

}

def apply(String command) {
  sh "kubectl apply ${command}"
}

def verifyRunningPods(String podName){
  def verify = sh(
    script: "kubectl get pods | grep ${podName} | grep Running",
    returnStatus: true
  )

  return verify != ""
}