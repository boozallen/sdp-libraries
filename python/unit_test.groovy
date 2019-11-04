void call(){
  stage(config.stage_name ?: "Python Unit test"){
    def image = config.docker_image ?: "python:3.7.4"
    inside_sdp_image(image){
      def test_results_file = config.test_report_path ?: "junit.xml"

      unstash "workspace"
      echo "running python unit tests"

      try {
        String testInstalls = config.test_installs ?: "pip install beautifulsoup4 lxml dateutils nltk pytest"
        String testCmd = config.test_cmd ?: "python -m pytest --junitxml=junit.xml"
        sh "${testInstalls} && ${testCmd}"
        stash name: "workspace",
                includes: config.stash?.includes ?: "**",
                excludes: config.stash?.excludes ?: "node_modules/**",
                useDefaultExcludes: false,
                allowEmpty: true

        echo "archiving ${test_results_file}"

        archiveArtifacts allowEmptyArchive: true, artifacts: "${test_results_file}"
      }catch(any){
        println "issue with python unit tests"
        unstable("issue with python unit tests")
      }
    }
  }
}