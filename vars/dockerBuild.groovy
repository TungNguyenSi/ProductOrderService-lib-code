def call(Map args) {
    node {
        def docker = args.jsl.com.ifi.jenkins.Docker.new(this)
        docker.build("test");
    }
    return this
}