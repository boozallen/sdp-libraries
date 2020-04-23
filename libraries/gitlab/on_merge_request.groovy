/*
  Copyright © 2018 Booz Allen Hamilton. All Rights Reserved.
  This software package is licensed under the Booz Allen Public License. The license can be found in the License file or at http://boozallen.github.io/licenses/bapl
*/
import org.gitlab4j.api.GitLabApi
import org.gitlab4j.api.MergeRequestApi
import org.gitlab4j.api.models.MergeRequest

void call(Map args = [:], body){
  
  // do nothing if not mr
  if (!env.GIT_BUILD_CAUSE.equals("mr")) 
    return
  
  def source_branch = get_source_branch()
  def target_branch = env.CHANGE_TARGET
    
  // do nothing in source branch doesn't match
  if (args.from)
  if (!(source_branch ==~ args.from))
    return

  // do nothing if target branch doesnt match
  if (args.to)
  if (!(target_branch ==~ args.to))
    return
  
  println "running because of a MR from ${source_branch} to ${target_branch}"
  body()  

}

def get_source_branch(){
  node{
    def ghUrl = "${env.GIT_URL.split("/")[0..-3].join("/")}"
    def cred_id = env.GIT_CREDENTIAL_ID
    withCredentials([usernamePassword(credentialsId: cred_id, passwordVariable: 'PAT', usernameVariable: 'USER')]) {
      if ((env.ORG_NAME).isEmpty())
      {
        projectName = "${env.REPO_NAME}"
      }
      else
      {
        projectName = "${env.ORG_NAME}/${env.REPO_NAME}"
      }
      GitLabApi gl = new GitLabApi(ghUrl, PAT)
      sourceBranch =  gl.getMergeRequestApi().getMergeRequest(projectName.toString(), env.CHANGE_ID.toInteger()).getSourceBranch() 
      println "RETURNING SOURCE BRANCH NAME: ${sourceBranch}"
      return sourceBranch
    }
  }
}
