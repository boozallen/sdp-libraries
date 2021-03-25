void call(String stashName){
  cleanWs()
  steps.unstash(stashName)
}
