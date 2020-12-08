/*
  Copyright © 2018 Booz Allen Hamilton. All Rights Reserved.
  This software package is licensed under the Booz Allen Public License. The license can be found in the License file or at http://boozallen.github.io/licenses/bapl
*/

/*
  Validate library configuration
*/
@Validate
void call(context){
    String distributionConfig = config.distribution
    List options = [ "github", "github_enterprise", "gitlab" ]

    if (!options.contains(distributionConfig)) {
        error "Distribution can only be set to one of the following: github, github_enterprise, gitlab. Currently: ${distributionConfig}"
    }

    env.GIT_LIBRARY_DISTRUBITION = distributionConfig
    def dist = this.fetch()
    init_env()
}

// Initialize Git configuration of env vars
void init_env(){
    node{
        try{ unstash "workspace" }
        catch(ignored) { 
          println " 'workspace' stash not present. Skipping git library environment variable initialization. To change this behavior, ensure the 'sdp' library is loaded"
          return
        }

        env.GIT_URL = scm.getUserRemoteConfigs()[0].getUrl()
        env.GIT_CREDENTIAL_ID = scm.getUserRemoteConfigs()[0].credentialsId.toString()
        def parts = env.GIT_URL.split("/")
        for (part in parts){
            parts = parts.drop(1)
            if (part.contains(".")) break
        }
        env.ORG_NAME = parts.getAt(0)
        env.REPO_NAME = parts[1..-1].join("/") - ".git"
        env.GIT_SHA = sh(script: "git rev-parse HEAD", returnStdout: true).trim()

        if (env.CHANGE_TARGET){
            env.GIT_BUILD_CAUSE = "pr"
        } else {
            env.GIT_BUILD_CAUSE = sh (
              script: 'git rev-list HEAD --parents -1 | wc -w', // will have 2 shas if commit, 3 or more if merge
              returnStdout: true
            ).trim().toInteger() > 2 ? "merge" : "commit"
        }

        println "Found Git Build Cause: ${env.GIT_BUILD_CAUSE}"
    }
    return
}

def fetch(){
    return getBinding().getStep(env.GIT_LIBRARY_DISTRUBITION)
}
