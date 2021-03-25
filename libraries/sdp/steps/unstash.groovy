import org.boozallen.plugins.jte.util.TemplateLogger

void call(String stashName){
  try{
    if( new File(".git").exists() ){
      sh "rm -rf .git"
    }
  }catch(any){
    TemplateLogger.createDuringRun().printWarning(any.getMessage())
  }
  
  steps.unstash(stashName)
}
