void call() {
  stage("Container Image Scan") {
    String GRYPE_CONFIG = config.configuration_file ?: ".grype.yaml"
    String RAW_RESULTS_FILE = "-grype-results.json"
    String TRANSFORMED_RESULSTS_FILE = "-grype-results.txt"
    List<String> tar_files = []
    List<Exception> exceptions = []

    
    if (!fileExists("./${GRYPE_CONFIG}")) { error "no grype config found" }

    def images = get_images_to_build()

    //Save images to workspace and add to list of tar'ed images     
    images.each { img ->
      String tar = "${img.tag}.tar"
      sh "docker save ${img.registry}/${img.repo}:${img.tag} > ${img.tag}.tar"
      tar_files.push(tar)
    }
      
      //replace below with inside_sdp_image "grype:latest"
      docker.withRegistry("https://registry.uip.sh/", "registry-creds") {
        docker.image("registry.uip.sh/toolkit/grype:latest").inside() {
          tar_files.each{ file ->
          
          // perform the grype scan
          try {
            sh "grype ${file} -o json >> ${file}+${RAW_RESULTS_FILE}"
          
            echo "No CVE's at or above set threshold!"
          }
          
          // Catch the error on quality gate failure
          catch(Exception err) {
            exceptions.push(err)
            
            echo "Failed: ${err}"
          
            echo "Grype Quality Gate Failed. There are one or more CVE's that exceed the maximum allowed severity rating!"
          }

          // display the results in a human-readable format
          finally {
            def transform_script = resource("transform-grype-scan-results.sh")
            writeFile file: "transform-results.sh", text: transform_script

            tar_files.each{ file ->
              //need to remove .tar from file name
              String modifiedFileName = file.lastIndexOf('.')
              def transformed_results = sh script: "/bin/bash ./transform-results.sh ${modifiedFileName}+${RAW_RESULTS_FILE} ${GRYPE_CONFIG}", returnStdout: true
              writeFile file: TRANSFORMED_RESULSTS_FILE, text: transformed_results.trim()

              // archive the results
              archiveArtifacts artifacts: "${modifiedFileName}+${RAW_RESULTS_FILE}, ${modifiedFileName}+${TRANSFORMED_RESULSTS_FILE}, allowEmptyArchive: true"
            }

            //Throw exceptions from gathered list
            //should this be outside the "tar_files.each{ file ->" loop???
            exceptions.each{ error ->
              throw error
            }
          }
        }
      }
    }
  }
}
