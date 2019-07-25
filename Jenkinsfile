
stage("Unit Test"){
  node{
    unstash "workspace"
    sh "make test docker && touch \$(ls target/test-results/test/*.xml)"
    archiveArtifacts "target/reports/tests/test/**"
    junit "target/test-results/test/*.xml"
    stash "workspace"
  }
}

parallel "Compile Docs": {
  stage("Compile Docs"){
    node{
      unstash "workspace"
      sh "make docs"
      archiveArtifacts "_build/html/**"
    }
  }
}, "Static Code Analysis": {
  static_code_analysis()
}
