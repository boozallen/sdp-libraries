/*
  Copyright © 2020 Booz Allen Hamilton. All Rights Reserved.
  This software package is licensed under the Booz Allen Public License. The license can be found in the License file or at http://boozallen.github.io/licenses/bapl
*/
package libraries.git.steps

// Import code required for GitHub functions
import org.kohsuke.github.GitHub

/*
    fetches the name of the source branch in a Pull Request.
*/
def get_source_branch(){
    String ghUrl = "${env.GIT_URL.split("/")[0..-3].join("/")}/api/v3"
    def repo, org
    withCredentials([
        usernamePassword(credentialsId: env.GIT_CREDENTIAL_ID, passwordVariable: 'PAT', usernameVariable: 'USER')
    ]) {
        return GitHub.connectToEnterprise(ghUrl, PAT).getRepository("${env.ORG_NAME}/${env.REPO_NAME}")
            .getPullRequest(env.CHANGE_ID.toInteger())
            .getHead()
            .getRef()
    }
}
