/*
  Copyright © 2018 Booz Allen Hamilton. All Rights Reserved.
  This software package is licensed under the Booz Allen Public License. The license can be found in the License file or at http://boozallen.github.io/licenses/bapl
*/

import org.kohsuke.github.GHCommitState

void call(Map args = [:], body){
  
  // do nothing if not pr
  if (!env.GIT_BUILD_CAUSE.equals("pr")) 
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
  
  println "running because of a PR from ${source_branch} to ${target_branch}"
  body()
  
}

def get_source_branch(){
  def ghUrl = "${env.GIT_URL.split("/")[0..-3].join("/")}/api/v3"
  def repo 
  def org 
  withCredentials([usernamePassword(credentialsId: 'github', passwordVariable: 'PAT', usernameVariable: 'USER')]) {
    org = org.kohsuke.github.GitHub.connectToEnterprise(ghUrl, PAT)
    repo = org.getRepository("${env.ORG_NAME}/${env.REPO_NAME}")
  }
  def pr = repo.getPullRequest(env.CHANGE_ID.toInteger())
  return pr.getHead().getRef() 
}
