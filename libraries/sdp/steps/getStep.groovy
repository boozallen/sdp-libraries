/*
  Copyright © 2018 Booz Allen Hamilton. All Rights Reserved.
  This software package is licensed under the Booz Allen Public License. 
  The license can be found in the License file or at http://boozallen.github.io/licenses/bapl
*/
package libraries.sdp.steps

def getStep(String stepName){
  if(jteVersion.lessThanOrEqualTo("2.0.4")){
    return getBinding().getStep(stepName)
  } else { 
    return this.getStepFromCollector(stepName)
  }
}

@NonCPS
def getStepFromCollector(String stepName){
  try{
    Class collector = Class.forName("org.boozallen.plugins.jte.init.primitives.TemplatePrimitiveCollector")
    List steps = collector.current().getStep(stepName)
    if(steps.size() == 0){
      error "Step '${stepName}' not found."
    } else if (steps.size() > 1){
      error "Ambiguous step name '${stepName}'. Found multiple steps by that name."
    } else {
      return steps.first()
    }
  }catch(ClassNotFoundException ex){
    error "can't find the TemplatePrimitiveCollector class. That's odd. current JTE version is '${jteVersion.get()}'. You should submit an issue at https://github.com/jenkinsci/templating-engine-plugin/issues/new/choose"
  }
}