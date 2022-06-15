package libraries.grype.steps

void call() {
    stage("Grype Image Scan") {
        String grypeConfig = ".grype.yaml"
        String outputFormat = config?.report_format ?: "json"
        String severityThreshold = config?.fail_on_severity ?: "high"
        List<Exception> errors = []

        inside_sdp_image "grype:0.38.0", {
            login_to_registry{
                unstash "workspace"
                def images = get_images_to_build()
                images.each { img ->
                    String rawResultsFile = "${img.context}-grype-scan-results.json"
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
                        errors.push(err)
                        echo "Failed: ${err}"
                        echo "Grype Quality Gate Failed. There are one or more CVE's that exceed the maximum allowed severity rating!"
                    }
                    // display the results in a human-readable format
                    finally {
                        if (outputFormat == "json") {
                            String transformedResultsFile = "${img.context}-grype-scan-results.txt"
                            def transform_script = resource("transform-grype-scan-results.sh")
                            writeFile file: "transform-results.sh", text: transform_script
                            def transformed_results = sh script: "/bin/bash ./transform-results.sh ${rawResultsFile} ${grypeConfig}", returnStdout: true
                            writeFile file: transformedResultsFile, text: transformed_results.trim()
                            // archive the results
                            archiveArtifacts artifacts: "${rawResultsFile}, ${transformedResultsFile}", allowEmptyArchive: true
                        }
                        else {
                            archiveArtifacts artifacts: "${rawResultsFile}", allowEmptyArchive: true
                        }
                    }
                }  
            }      
            stash "workspace"
            if (!(errors?.empty)) {
                errors.each { errs -> 
                    throw errs
                }
            }
        }
    }
}
