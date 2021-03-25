void call(String stashName){
  try{
    if( new File(".git").exists() ){
      sh "rm -rf .git"
    }
  }catch(any){}
  
  steps.unstash(stashName)
}
