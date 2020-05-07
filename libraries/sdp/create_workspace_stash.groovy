/*
  Copyright © 2018 Booz Allen Hamilton. All Rights Reserved.
  This software package is licensed under the Booz Allen Public License. The license can be found in the License file or at http://boozallen.github.io/licenses/bapl
*/

import hudson.AbortException

@Validate // validate so this runs prior to other @Init steps
void call(context){
    node{
        cleanWs()
        try{
            checkout scm
        }catch(AbortException ex) {
            println "scm var not present, skipping source code checkout" 
        }
        stash name: 'workspace', allowEmpty: true, useDefaultExcludes: false
    }
}