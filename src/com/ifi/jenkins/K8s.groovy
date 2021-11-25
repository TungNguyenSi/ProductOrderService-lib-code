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

def createSecretsFromLiteral(String secretName, Map secrets) {
  StringBuilder sb = new StringBuilder();
  sb.append("kubectl create secret generic ${secretName} --save-config --dry-run=client")
  for (Map.Entry<String, String> entry : secrets.entrySet()) {
    sb.append(entry.key + "=" + entry.value)
  }
  sb.append(" -o yaml | kubectl apply -f -")
  sh(sb.toString())
}

def apply(String fileName) {
  sh "kubectl apply -f ${fileName}"
}

def verifyRunningPods(String deploymentName, String statement){
  def podName = sh (
    script: "kubectl get pods | grep ${deploymentName} | awk '{print \$1}'",
    returnStatus: true
  )
  def verify = sh(
    script: "kubectl logs ${podName} ${deploymentName} | grep ${statement}",
    returnStatus: true
  )

  return verify != ""
}