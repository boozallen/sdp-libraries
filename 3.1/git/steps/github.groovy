/*
  Copyright © 2020 Booz Allen Hamilton. All Rights Reserved.
  This software package is licensed under the Booz Allen Public License. The license can be found in the License file or at http://boozallen.github.io/licenses/bapl
*/
package libraries.git.steps

// Import code required for GitHub functions
import org.kohsuke.github.GitHub

/*
    returns the name of the source branch in a Pull Request
    for example, in a MR from feature to development, the source branch
    would be "feature"
*/
def get_source_branch(){

  def cred_id = env.GIT_CREDENTIAL_ID

  withCredentials([usernamePassword(credentialsId: cred_id, passwordVariable: 'PAT', usernameVariable: 'USER')]) {
      return GitHub.connectUsingOAuth(PAT).
              getRepository("${env.ORG_NAME}/${env.REPO_NAME}")
              .getPullRequest(env.CHANGE_ID.toInteger())
              .getHead()
              .getRef()
  }
}
