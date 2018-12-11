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
  try{
    update_pr status: "Jenkins Pull Request",
              state: "pending"
    body()
    update_pr status: "Jenkins Pull Request",
              state: "success"
  }catch(any){
    update_pr status: "Jenkins Pull Request",
              state: "failure"
    throw any
  }
  
}

void update_pr(args){
  /*try{
    ghe.getRepo().createCommitStatus(
      env.GIT_SHA, 
      args.state.toUpperCase() as GHCommitState,
      env.JOB_URL, 
      "Jenkins Pull Request Job", 
      args.status
    )
  } catch(ex){
    println "creating github status failed.." 
    throw ex
  }
  */
}

def get_source_branch(){
  return ghe.pr().getHead().getRef() 
}
