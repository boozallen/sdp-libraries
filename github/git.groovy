/*
  Copyright © 2018 Booz Allen Hamilton. All Rights Reserved.
  This software package is licensed under the Booz Allen Public License. The license can be found in the License file or at http://boozallen.github.io/licenses/bapl
*/

import org.codehaus.groovy.runtime.GStringImpl

void call(Map args){

  // supported git actions 
  actions = [
    "add": { files ->
      if (files instanceof String) sh "git add ${files}"
      else if (files instanceof GStringImpl) sh "git add ${files}"
      else sh "git add ${files.join(" ")}"
    }, 
    "commit": { message ->
      sh "git config user.email 'jenkins@nothing.com' && git config user.name 'Jenkins'"
      sh script: "git commit -m \"${message}\"", returnStatus: true
    },
    "push": { flags ->
      sh "git push ${flags ?: ""} ${env.git_url_with_creds}"
    }
  ]

  // validate that there are no invalid inputs
  invalid_args = args.findAll{ !(it.getKey() in actions.keySet()) }
  if (invalid_args) error "Unknown git actions: ${invalid_args.collect{ it.getKey() }.join(", ")}"
  
  // validate the user is doing an action
  if (!(args.subMap(actions.keySet()))) error "git: You must use an action: ${actions.keySet().join(", ")}"
  
    
  // do the things.
  args.each{ action, value ->
    def c = actions.get(action)
    c.resolveStrategy = Closure.DELEGATE_FIRST
    c.delegate = this        
    c.call(value)
  }
}

void call(String action){
  String a = action.toString()
  this.call([
    (a): null
  ])
}
