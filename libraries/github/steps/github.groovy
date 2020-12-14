
package libraries.github

import org.kohsuke.github.GitHub

void call(){

}

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