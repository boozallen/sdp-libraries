node{
  checkout scm 
  stage("Unit Test"){
    sh "make test docker" 
    archive "target/reports/tests/test/**" 
  }
}
