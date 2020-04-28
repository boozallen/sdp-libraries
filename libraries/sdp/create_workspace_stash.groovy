/*
  Copyright © 2018 Booz Allen Hamilton. All Rights Reserved.
  This software package is licensed under the Booz Allen Public License. The license can be found in the License file or at http://boozallen.github.io/licenses/bapl
*/
import org.jenkinsci.plugins.workflow.cps.GlobalVariable
import org.jenkinsci.plugins.workflow.multibranch.SCMVar

@Validate // validate so this runs prior to other @Init steps
void call(context){
    GlobalVariable scm = GlobalVariable.byName("scm", currentBuild.rawBuild)
    node{
        if(scm instanceof SCMVar){
            cleanWs()
            checkout scm
        } else {
            println "scm var not present, skipping source code checkout" 
        }
        stash name: 'workspace', allowEmpty: true, useDefaultExcludes: false
    }
}