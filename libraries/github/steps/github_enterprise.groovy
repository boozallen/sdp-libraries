package libraries.github

import org.kohsuke.github.GitHub

void call(){

}

def get_source_branch(){

  String ghUrl = "${env.GIT_URL.split("/")[0..-3].join("/")}/api/v3"
  def repo
  def org

  def cred_id = env.GIT_CREDENTIAL_ID

  withCredentials([usernamePassword(credentialsId: cred_id, passwordVariable: 'PAT', usernameVariable: 'USER')]) {
      return GitHub.connectToEnterprise(ghUrl, PAT).getRepository("${env.ORG_NAME}/${env.REPO_NAME}")
              .getPullRequest(env.CHANGE_ID.toInteger())
              .getHead()
              .getRef()
  }

}