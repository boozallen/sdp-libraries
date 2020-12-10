/*
  Copyright © 2018 Booz Allen Hamilton. All Rights Reserved.
  This software package is licensed under the Booz Allen Public License. The license can be found in the License file or at http://boozallen.github.io/licenses/bapl
*/

package libraries.sdp

import hudson.AbortException

@Validate // validate so this runs prior to other @Init steps
void call(){
    node{
        cleanWs()
        try{
            println "start checkout"
            checkout scm
            println "end checkout"
        }catch(AbortException ex) {
            println "scm var not present, skipping source code checkout" 
        }catch(err){
          getBinding().getVariables().each{ k, v ->
            println "${k}: ${v}"
          }
          println "oops ${err}" 
        } finally {
          println "print finally"  
        }
      
        stash name: 'workspace', allowEmpty: true, useDefaultExcludes: false
    }
}
