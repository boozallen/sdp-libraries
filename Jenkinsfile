parallel "Unit Test": {
  node{
    checkout scm 
    stage("Unit Test"){
      sh "make test docker && touch \$(ls target/test-results/test/*.xml)" 
      archiveArtifacts "target/reports/tests/test/**" 
      junit "target/test-results/test/*.xml" 
    }
  }
}, "Compile Docs": {
  node{
    stage("Compile Docs"){
      checkout scm 
      sh "make docs" 
      archiveArtifacts "_build/html/**"
    }
  }
}, "Static Code Analysis": {
  static_code_analysis()
}
