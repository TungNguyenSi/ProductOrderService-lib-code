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

    void clone(def repo, def githubUsername, def githubPassword){
        script.sh("curl -u ${githubUsername}:${githubPassword} ${repo}")
        script.sh("git clone ${repo}")
    }
}
