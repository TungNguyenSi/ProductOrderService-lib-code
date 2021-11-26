#!/usr/bin/env groovy
import com.ifi.jenkins.Docker
import com.ifi.jenkins.K8s

def call() {
  def gDocker = new Docker()
  def k8s = new K8s()
//  node("agent1") {
//    docker.image('nstung219/agent-image:1.2').inside {
//      stage("build image") {
//        checkout scm
//        gDocker.build("product-order-service:release-1.0")
//      }
//      stage("push image") {
//        gDocker.push("gcr.io/jenkins-demo-330307", "product-order-service:release-1.0")
//      }
//    }
//  }

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
    label: "test",
    containers: [
      containerTemplate(
        image: 'gcr.io/kaniko-project/executor:debug', name: 'kaniko',
        envVars: [envVar(key: 'GOOGLE_APPLICATION_CREDENTIALS', value: '/vault/secrets/gcloud.json')],
        command: 'sleep',
        args: '999999'
      )],
    serviceAccount: 'vault-auth'
  ) {
    node ("test") {
      container(name: 'kaniko', shell: '/busybox/sh') {
        checkout scm
        sh '''#!/busybox/sh
            /kaniko/executor --context `pwd` --dockerfile `pwd`/Dockerfile --destination gcr.io/jenkins-demo-330307/product-order-service:release-1.0
        '''
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
        k8s.apply("mongo-deploy.yaml")
        k8s.apply("product-order-service-deploy.yaml")

        def mongoVerify = k8s.verifyRunningPods("mongo")
        def serverVerify = k8s.verifyRunningPods("server")
        if (mongoVerify == false || serverVerify == false){
          currentBuild.result = "FAILURE"
        }
      }
    }
  }
}


