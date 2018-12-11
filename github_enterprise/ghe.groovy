/*
  Copyright © 2018 Booz Allen Hamilton. All Rights Reserved.
  This software package is licensed under the Booz Allen Public License. The license can be found in the License file or at http://boozallen.github.io/licenses/bapl
*/

import org.kohsuke.github.*

void call() {
  error "Step ghe() is supported. Did you mean ghe.gh(), ghe.getRepo(), or ghe.pr()?"
  return
}

def gh() {
  withCredentials([usernamePassword(credentialsId: 'github', passwordVariable: 'PAT', usernameVariable: 'USER')]) {
    def ghUrl = "${env.GIT_URL.split("/")[0..-3].join("/")}/api/v3"
    return org.kohsuke.github.GitHub.connectToEnterprise(ghUrl, PAT)
  }
}

def getRepo() {
  return gh().getRepository("${env.ORG_NAME}/${env.REPO_NAME}")
}

def pr() {
  return getRepo().getPullRequest(env.CHANGE_ID.toInteger())
}
