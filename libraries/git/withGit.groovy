/*
  Copyright © 2018 Booz Allen Hamilton. All Rights Reserved.
  This software package is licensed under the Booz Allen Public License. The license can be found in the License file or at http://boozallen.github.io/licenses/bapl
*/

void call(Map args, Closure body){

  // check required parameters
  if (!args.url || !args.cred)
    error """
    withGit syntax error.
    Input Parameters:
      url: https git url to repository (required)
      cred: jenkins credential ID for github. (required)
      branch: branch in the repository to checkout. defaults to master. (optional)
    """

  withCredentials([usernamePassword(credentialsId: args.cred, passwordVariable: 'PASS', usernameVariable: 'USER')]) {
    repo = args.url.split("/").last() - ".git"
    withEnv(["git_url_with_creds=${args.url.replaceFirst("://","://${USER}:${PASS}@")}"]) {
      node {
        sh "rm -rf ${repo}"
        sh "set +x && git clone ${env.git_url_with_creds}"
        dir(repo){
          sh "git checkout ${args.branch ?: "master"}"
          push = "push"
          body.resolveStrategy = Closure.DELEGATE_FIRST
          body.delegate = this
          body.run()
        }
      }
    }
  }
}
