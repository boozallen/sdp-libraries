void call(){
  stage("NPM Unit test"){
    if( config.test_disable ){
      unstable("unit tests are disabled")
      return;
    }

    def nodeImage = config.node_image ?: "node:latest"
    def testScript = config.test_cmd ?: "run test"
    def installScript = config.test_install ?: "install"
    def failOnException = config.test_fail_on_exception ?: true

    inside_sdp_image(nodeImage){
      def test_results_file = config.test_report_path ?: "junit.xml"

      unstash "workspace"
      echo "running npm unit tests"

      timeout(time: 10, unit: 'MINUTES') {
        try {
          boolean stashAlways = config.test_stash_always ?: false

          def ex = null
          try {

            if( installScript ){
              sh "npm ${installScript}"
            }

            sh "npm run ${testScript}"
          }catch(any){
            ex = any
          }

          if( !ex || stashAlways ) {
            def stash_name = config.test_stash ?: ( config.stash?.name ?: "workspace")
            stash name: stash_name,
                    includes: config.stash?.includes ?: "**",
                    excludes: config.stash?.excludes ?: "node_modules/**",
                    useDefaultExcludes: false,
                    allowEmpty: true
          }

          if( ex ){
            throw ex
          }

          echo "archiving ${test_results_file}"

          archiveArtifacts allowEmptyArchive: true, artifacts: "${test_results_file}"
        } catch (any) {
          println "issue with npm unit tests"
          failOnException ? { error("issue with npm unit tests") }() : {
            unstable("issue with npm unit tests")
          }()
        }
      }
    }
  }
}