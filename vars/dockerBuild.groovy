def call(Map args) {
    node {
        agent {
            docker {
                image "nstung219/agent-image:1.2"
                label "agent1"
            }
        }
        def docker = args.jsl.com.ifi.jenkins.Docker.new(this)
        docker.build("test");
    }
    return this
}