package libraries.grype.steps

void call() {
    stage("Grype Image Scan") {
        String grypeContainer = config?.grype_container ?: "grype:0.38.0"
        String outputFormat = config?.report_format ?: "json"
        String severityThreshold = config?.fail_on_severity ?: "high"
        List<Exception> errors = []

        inside_sdp_image "${grypeContainer}", {
            login_to_registry{
                unstash "workspace"
                def images = get_images_to_build()
                images.each { img ->
                    // Use $img.repo to help name our results uniquely. Checks to see if a forward slash exists in the string and remove everything to the left if it does.
                    println(img.repo.contains(/))
                    if (img.repo.contains(/))
                    String rawResultsFile = "${img.repo}-grype-scan-results.json"
//check for grype config file in workspace
//remove if (!fileExists("./${grypeConfig}")) { error "no grype config found" }
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
                        //Specific to BASS team. Allows Backstage to ingest JSON but also creates a human readable artifact.
                        if (outputFormat == "json") {
                            if (fileExists())
                            String transformedResultsFile = "${img.repo}-grype-scan-results.txt"
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
