/*
  Copyright © 2018 Booz Allen Hamilton. All Rights Reserved.
  This software package is licensed under the Booz Allen Public License. The license can be found in the License file or at http://boozallen.github.io/licenses/bapl
*/
import org.jenkinsci.plugins.workflow.cps.GlobalVariable
import org.jenkinsci.plugins.workflow.multibranch.SCMVar
import hudson.scm.SCM
import org.boozallen.plugins.jte.console.TemplateLogger


@Validate // validate so this runs prior to other @Init steps
void call(context){
    println "inside" 
    node{
        println "fetching scm" 
        println "scm = ${getSCM()}"
        if(getSCM()){
            cleanWs()
            checkout getSCM()
        } else {
            println "scm var not present, skipping source code checkout" 
        }
        stash name: 'workspace', allowEmpty: true, useDefaultExcludes: false
    }
}

@NonCPS
SCM getSCM(){
    GlobalVariable scmVar = GlobalVariable.byName("scm", currentBuild.rawBuild)
    SCM scm = scmVar.getValue(this)
    TemplateLogger.printWarning "scm = ${scm}"
    return scm
}