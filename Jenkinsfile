node{
  stage("Unit Test"){
    checkout scm
    sh "docker build -f unit_test.Dockerfile -t pipeline-unit-testing ."
    sh "docker run --rm -t -v \$(pwd):/app -w /app pipeline-unit-testing gradle --no-daemon test"
    archiveArtifacts artifacts: 'target/reports/tests/test/**'
  }
}
