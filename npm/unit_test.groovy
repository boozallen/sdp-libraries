void call(){
  stage("NPM Unit test"){
    def nodeImage = config.node_image ?: "node:latest"
    def testScript = config.test_cmd ?: "test"
    def installScript = config.test_install ?: "install"

    docker.image(nodeImage).inside{
      def test_results_file = config.test_report_path ?: "junit.xml"

      unstash "workspace"
      echo "running npm unit tests"

      if( installScript ){
        sh "npm ${installScript}"
      }

      sh "npm run ${testScript}"

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