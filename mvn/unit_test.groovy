void call(){
  stage("Maven Unit test"){
    if( config.test_disable ){
      unstable("unit tests are disabled")
      return;
    }

    def image = config.image ?: "maven:3.6-jdk-8"
    def ls_output = config.ls_output ?: false
    def failOnException = config.test_fail_on_exception ?: true

    inside_sdp_image(image){
      def test_results_file = config.test_report_path ?: "junit.xml"

      unstash "workspace"
      echo "running maven unit tests"

      timeout(time: 10, unit: 'MINUTES') {
        try {
          String testCmd = config.test_cmd ?: "mvn clean verify jacoco:report"
          sh "${testCmd}"

          if( ls_output ){
            sh "ls -R | grep jacoco"
          }

          def stash_name = config.test_stash ?: ( config.stash?.name ?: "workspace")
          stash name: stash_name,
                  includes: config.stash?.includes ?: "**",
                  excludes: config.stash?.excludes ?: "**/*Test.java",
                  useDefaultExcludes: false,
                  allowEmpty: true

          echo "archiving ${test_results_file}"

          archiveArtifacts allowEmptyArchive: true, artifacts: "${test_results_file}"
        } catch (any) {
          String ex_message = "issue with maven unit tests: ${any}"
          println ex_message
          failOnException ? { error(ex_message) }() : {
            unstable(ex_message)
          }()
        }
      }
    }
  }
}