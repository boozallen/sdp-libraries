void call() {
  stage("Grype Image Scan") {
    def String GRYPE_CONFIG = ".grype.yaml"
    def String RAW_RESULTS_FILE = "grype-scan-results.json"
    def String TRANSFORMED_RESULSTS_FILE = "grype-scan-results.txt"
    def images = get_images_to_build()
        
    images.each { img ->
      docker.withRegistry("https://registry.uip.sh/", "registry-creds") {
        docker.image("registry.uip.sh/toolkit/grype:0.38.0").inside() {
          unstash "workspace"
          //check for grype config file in workspace
          if (!fileExists("./${GRYPE_CONFIG}")) { error "no grype config found" }

          // perform the grype scan
          try {
            sh "grype ${img.registry}/${img.repo}:${img.tag} -o json >> ${RAW_RESULTS_FILE}"
          
            echo "No CVE's at or above set threshold!"
          }
          
          // Catch the error on quality gate failure
          catch(Exception err) {
            echo "Failed: ${err}"
          
            echo "Grype Quality Gate Failed. There are one or more CVE's that exceed the maximum allowed severity rating!"
          
            throw err
          }

          // display the results in a human-readable format
          finally {
            def transform_script = resource("transform-grype-scan-results.sh")
            writeFile file: "transform-results.sh", text: transform_script
          
            def transformed_results = sh script: "/bin/bash ./transform-results.sh ${RAW_RESULTS_FILE} ${GRYPE_CONFIG}", returnStdout: true
            writeFile file: TRANSFORMED_RESULSTS_FILE, text: transformed_results.trim()

           // archive the results
            archiveArtifacts artifacts: "${RAW_RESULTS_FILE}, ${TRANSFORMED_RESULSTS_FILE}"
            stash "workspace"
          }
        }
      }
    }
  }
}
