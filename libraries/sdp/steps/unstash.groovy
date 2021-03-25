import org.boozallen.plugins.jte.util.TemplateLogger

void call(String stashName){
  try{
    sh script: " [ -d '.git' ] && rm -rf .git ", returnStatus: true
  }catch(any){
    TemplateLogger.createDuringRun().printWarning(any.getMessage())
  }
  
  steps.unstash(stashName)
}
