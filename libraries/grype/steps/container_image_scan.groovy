package libraries.grype.steps

void call() {
    stage("Grype Image Scan") {
        String grypeContainer = config?.grype_container ?: "grype:0.38.0"
        String outputFormat = config?.report_format
        String severityThreshold = config?.fail_on_severity
        String grypeConfig = config?.grype_config
        String rawResultsFile = ""
        String transformedResultsFile = ""
        String ARGS = ""
        List<Exception> errors = []
        if (outputFormat != null) {
            ARGS += "-o ${outputFormat} "
        }
        if (severityThreshold != null) {
            ARGS += "--fail-on ${severityThreshold} "
        }

        inside_sdp_image "${grypeContainer}", {
            login_to_registry{
                unstash "workspace"
                if (grypeConfig != null) {
                    ARGS += "--config ${grypeConfig}"
                }
                else if (fileExists('.grype.yaml')) {
                    grypeConfig = '.grype.yaml'
                    ARGS += "--config ${grypeConfig}"
                }
                else if (fileExists('.grype/config.yaml')) {
                    grypeConfig = '.grype/config.yaml'
                    ARGS += "--config ${grypeConfig}"
                }
                else if (fileExists('~/.grype.yaml')) {
                    grypeConfig = '~/grype.yaml'
                    ARGS += "--config ${grypeConfig}"
                }
                else if (fileExists('<XDG_CONFIG_HOME>/grype/config.yaml')) {
                    grypeConfig = '<XDG_CONFIG_HOME>/grype/config.yaml'
                    ARGS += "--config ${grypeConfig}"
                }
                else {
                    //do nothing
                }
                echo grypeConfig
                def images = get_images_to_build()
                images.each { img ->
                    // Use $img.repo to help name our results uniquely. Checks to see if a forward slash exists and splits the string at that location.
                    if (img.repo.contains("/")) {
                        String[] repoImageName = img.repo.split('/')
                        rawResultsFile = repoImageName[1] + '-grype-scan-results'
                        transformedResultsFile = repoImageName[1] + '-grype-scan-results.txt'
                    }
                    else {
                        rawResultsFile = "${img.repo}-grype-scan-results"
                        transformedResultsFile = "${img.repo}-grype-scan-results.txt"
                    }
                    // perform the grype scan
                    try {
                        sh "grype ${img.registry}/${img.repo}:${img.tag} ${ARGS} >> ${rawResultsFile}"
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
