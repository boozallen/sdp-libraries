/*
  Copyright © 2020 Booz Allen Hamilton. All Rights Reserved.
  This software package is licensed under the Booz Allen Public License. The license can be found in the License file or at http://boozallen.github.io/licenses/bapl
*/
package libraries.git.steps
// Import code required for GitLab functions


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
      
        return this.class.classLoader.loadClass( 'org.gitlab4j.api.GitLabApi', true, false )?.newInstance(gitlabUrl, PAT).getMergeRequestApi().getMergeRequest(projectName.toString(), env.CHANGE_ID.toInteger()).getSourceBranch()
    }
  }
}
