/*
  Copyright © 2018 Booz Allen Hamilton. All Rights Reserved.
  This software package is licensed under the Booz Allen Public License. The license can be found in the License file or at http://boozallen.github.io/licenses/bapl
*/
def call(pr_comment){
  stage("Commenting on PR") { 
    def git_cred = pipelineConfig.github_credential ?:
                   config.github_credential ?:
                   {error "GitHub Credential For Pull Request Comment Not Defined"}()
    // Need to incorporate
    def github_api = "${env.GIT_URL.split("/")[0..-3].join("/")}/api/v3"
    def github_pr = "${github_api}/repos/${env.ORG_NAME}/${env.REPO_NAME}/issues/${env.CHANGE_ID}/comments"                   

    withCredentials([usernamePassword(credentialsId: config.github_credential, passwordVariable: 'PAT',usernameVariable: 'user')]) {
          withEnv(["PAT=${PAT}","PR=${github_pr}","API=${github_api}","PR_COMMENT=${pr_comment}"]) {
            node {
            this.create_comment()
            }
          }
      }
  }
}

void create_comment(){
    sh """
    curl -s -H \"Authorization: token ${PAT}\" -X POST -d '{\"body\": \"${PR_COMMENT}\"}' \"${PR}\"
    """
}