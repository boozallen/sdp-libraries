void call() {
  stage("UIP - Grype Image Scan") {
    def GRYPE_CONFIG = ".grype.yaml"
    def RAW_RESULTS_FILE = "grype-scan-results.json"
    def TRANSFORMED_RESULSTS_FILE = "grype-scan-results.txt"

    if (!fileExists("./${GRYPE_CONFIG}")) { error "no grype config found" }

    def images = get_images_to_build()
        
    images.each { img ->
      sh "docker save ${img.registry}/${img.repo}:${img.tag} > ${img.tag}.tar"

      docker.withRegistry("https://registry.uip.sh/", "registry-creds") {
        docker.image("registry.uip.sh/toolkit/grype:latest").inside() {
          
          // perform the grype scan
          try {
            sh "grype ${img.tag}.tar -o json >> ${RAW_RESULTS_FILE}"
          
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
          }
        }
      }
    }
  }
}
