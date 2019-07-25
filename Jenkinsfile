parallel "Unit Test": {
  node{
    stage("Unit Test"){
      unstash "workspace"
      sh "make test docker && touch \$(ls target/test-results/test/*.xml)" 
      archiveArtifacts "target/reports/tests/test/**" 
      junit "target/test-results/test/*.xml" 
    }
  }
}, "Compile Docs": {
  node{
    stage("Compile Docs"){
      unstash "workspace"
      sh "make docs" 
      archiveArtifacts "_build/html/**"
    }
  }
}, "Static Code Analysis": {
  static_code_analysis()
}
