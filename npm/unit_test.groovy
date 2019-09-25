void call(){
  stage("NPM Unit test"){
    def nodeImage = config.nodeImage ?: "node:10.16.0-stretch-slim"
    docker.image(nodeImage).inside{
      def test_results_file = config.test_report_path ?: "junit.xml"

      unstash "workspace"
      echo "running npm unit tests"

      String testCmd = config.test_cmd ?: "npm run test"
      sh "npm install && ${testCmd}"
      stash name: "workspace",
              includes: config.stash?.includes ?: "**",
              excludes: config.stash?.excludes ?: "node_modules/**",
              useDefaultExcludes: false,
              allowEmpty: true

      echo "archiving ${test_results_file}"

      archiveArtifacts allowEmptyArchive: true, artifacts: "${test_results_file}"
    }
  }
}