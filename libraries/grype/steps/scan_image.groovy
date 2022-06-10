package libraries.grype.steps

void call() {
  stage("Grype Image Scan") {
    def String grypeConfig = ".grype.yaml"
    def String rawResultsFile = "grype-scan-results.json"
    def String transformedResultsFile = "grype-scan-results.txt"
    def String outputFormat = config?.report_format ?: "json"
    def String severityThreshold = config?.fail_on_severity ?: "high"
    def images = get_images_to_build()
        
    images.each { img ->
      docker.withRegistry("https://registry.uip.sh/", "registry-creds") {
        docker.image("registry.uip.sh/toolkit/grype:0.38.0").inside() {
          unstash "workspace"
          //check for grype config file in workspace
          if (!fileExists("./${grypeConfig}")) { error "no grype config found" }

          // perform the grype scan
          try {
            if (severityThreshold == "none") {
              sh "grype ${img.registry}/${img.repo}:${img.tag} -o ${outputFormat} >> ${rawResultsFile}"
            }
            else {
            sh "grype ${img.registry}/${img.repo}:${img.tag} -o ${outputFormat} --fail-on ${severityThreshold} >> ${rawResultsFile}"
            
            echo "No CVE's at or above set threshold!"
            }
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
          
            def transformed_results = sh script: "/bin/bash ./transform-results.sh ${rawResultsFile} ${grypeConfig}", returnStdout: true
            writeFile file: transformedResultsFile, text: transformed_results.trim()

           // archive the results
            archiveArtifacts artifacts: "${rawResultsFile}, ${transformedResultsFile}"
            stash "workspace"
          }
        }
      }
    }
  }
}
