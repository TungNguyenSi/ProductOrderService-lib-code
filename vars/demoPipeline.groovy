#!/usr/bin/env groovy
import com.ifi.jenkins.Docker
import com.ifi.jenkins.K8s

def call() {
  def k8s = new K8s()
  podTemplate(
    annotations: [
      podAnnotation(key: 'vault.hashicorp.com/agent-inject', value: 'true'),
      podAnnotation(key: 'vault.hashicorp.com/role', value: 'webapp'),
      podAnnotation(key: 'vault.hashicorp.com/agent-inject-secret-gcloud.json', value: 'secrets/creds/gcloud-service-account'),
      podAnnotation(key: 'vault.hashicorp.com/agent-inject-template-gcloud.json', value: '{{ with secret "secrets/creds/gcloud-service-account" }}' +
        '{{ range $k, $v := .Data }}\n' +
        '{{ $v }}\n' +
        '{{ end }}' +
        '{{ end }}')
    ],
    cloud: 'kubernetes',
    label: "kaniko-agent",
    containers: [
      containerTemplate(
        image: 'gcr.io/kaniko-project/executor:debug', name: 'kaniko',
        envVars: [envVar(key: 'GOOGLE_APPLICATION_CREDENTIALS', value: '/vault/secrets/gcloud.json')],
        command: 'sleep',
        args: '999999'
      )],
    serviceAccount: 'vault-auth'
  ) {
    node ("kaniko-agent") {
      stage ("build and push image") {
        container(name: 'kaniko', shell: '/busybox/sh') {
          checkout scm
          sh '''#!/busybox/sh
            /kaniko/executor --context `pwd` --dockerfile `pwd`/Dockerfile --destination gcr.io/jenkins-demo-330307/product-order-service:release-1.0
          '''
        }
      }
    }
  }

  podTemplate(label: "kubepod", cloud: 'kubernetes', containers: [
    containerTemplate(name: 'jnlp', image: 'nstung219/k8s-agent:1.5')
  ]) {
    node ("kubepod") {
      stage("deploy") {
        checkout scm
        k8s.auth()
        sh 'kubectl apply -f mongo-deploy.yaml'
        sh 'kubectl apply -f product-order-service-deploy.yaml'

        k8s.verifyRunningPods("mongodb", "3m")
        k8s.verifyRunningPods("server", "1m")
      }
    }
  }
}


