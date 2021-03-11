/*
  Copyright © 2018 Booz Allen Hamilton. All Rights Reserved.
  This software package is licensed under the Booz Allen Public License. The license can be found in the License file or at http://boozallen.github.io/licenses/bapl
*/

package libraries.github

import org.kohsuke.github.GitHub

void call(Map args = [:], body){
  
  // do nothing if not pr
  if (!env.GIT_BUILD_CAUSE.equals("pr")){}
    return
  }
  

  // do nothing in source branch doesn't match
  if (args.from){
    def source_branch = impls().get_source_branch()
    if (!(source_branch ==~ (~args.from))){
        return 
    }
  }

  // do nothing if target branch doesnt match
  if (args.to){
    def target_branch = env.CHANGE_TARGET
    if (!(target_branch ==~ (~args.to) )){
        return 
    }
  }

  if (args.with_label){
    def labels = impls().get_labels()
    if(!(args.with_label in labels)){
        return
    }
  }
  
  
  println "running because of a PR from ${source_branch} to ${target_branch}"
  body()  

}