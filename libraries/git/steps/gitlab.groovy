/*
  Copyright © 2018 Booz Allen Hamilton. All Rights Reserved.
  This software package is licensed under the Booz Allen Public License. The license can be found in the License file or at http://boozallen.github.io/licenses/bapl
*/

// Import code required for GitLab functions
import org.gitlab4j.api.GitLabApi
import org.gitlab4j.api.MergeRequestApi
import org.gitlab4j.api.models.MergeRequest

// Validate GitLab configuration is valid
void validate_configuration(){
    node{
      unstash "workspace"

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

/*
    returns the name of the source branch in a Merge Request
    for example, in a MR from feature to development, the source branch
    would be "feature"
*/
def get_source_branch(){
  node{
    def gitlabUrl = "${env.GIT_URL.split("/")[0..-3].join("/")}"
    withCredentials([
        usernamePassword(
            credentialsId: env.GIT_CREDENTIAL_ID,
            passwordVariable: 'PAT',
            usernameVariable: 'USER'
        )
    ]) {
        if ((env.ORG_NAME).isEmpty()){
            projectName = "${env.REPO_NAME}"
        } else {
            projectName = "${env.ORG_NAME}/${env.REPO_NAME}"
        }
        GitLabApi gl = new GitLabApi(gitlabUrl, PAT)
        sourceBranch =  gl.getMergeRequestApi().getMergeRequest(projectName.toString(), env.CHANGE_ID.toInteger()).getSourceBranch()
        return sourceBranch
    }
  }
}
