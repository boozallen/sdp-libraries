/*
  Copyright © 2018 Booz Allen Hamilton. All Rights Reserved.
  This software package is licensed under the Booz Allen Public License. The license can be found in the License file or at http://boozallen.github.io/licenses/bapl
*/

package libraries.sdp.steps

import hudson.AbortException

@Validate // validate so this runs prior to other @Init steps
void call(){
    node{
        cleanWs()
        try{
            checkout scm
        }catch(AbortException ex) {
            println "scm var not present, skipping source code checkout" 
        }catch(err){
          println "exception ${err}" 
        } 
      
        stash name: 'workspace', allowEmpty: true, useDefaultExcludes: false
    }
}
