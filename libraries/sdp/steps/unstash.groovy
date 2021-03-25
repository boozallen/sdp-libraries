import org.boozallen.plugins.jte.util.TemplateLogger

void call(String stashName){
  try{
    sh " [ -d '.git' ] && rm -rf .git "
  }catch(any){
    TemplateLogger.createDuringRun().printWarning(any.getMessage())
  }
  
  steps.unstash(stashName)
}
