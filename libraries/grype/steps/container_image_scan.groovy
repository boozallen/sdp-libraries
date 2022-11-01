package libraries.grype.steps

void call() {
    stage("Grype Image Scan") {
        String grypeContainer = config?.grype_container ?: "grype:0.38.0"
        String outputFormat = config?.report_format ?: 'json'
        String severityThreshold = config?.fail_on_severity ?: 'high'
        String grypeConfig = config?.grype_config
        Boolean scanSbom = config?.scan_sbom ?: false
        String baseDir = "./"
        String resultsFileFormat = ".txt"
        String ARGS = ""
        // is flipped to True if an image scan fails
        Boolean shouldFail = false 
        //test
        baseDir.eachFileMatch FileType.any, ~/\*json.json/, { names << it.name } 
        names.each { name ->
        println(name)}
        //end
        if (outputFormat != null) {
            ARGS += "-o ${outputFormat} "
            if (outputFormat == 'json') {
                resultsFileFormat = '.json'
            }
            else if (outputFormat == 'cyclonedx') {
                resultsFileFormat = '.xml'
            }
            else if (outputFormat == 'template') {
                //placeholder for custom template format
                resultsFileFormat = '.template'
            }
        }

        if (severityThreshold != "none") {
            ARGS += "--fail-on ${severityThreshold} "
        }

        inside_sdp_image(grypeContainer){
            login_to_registry{
                unstash "workspace"

                // Gets environment variable and sets it to a groovy var
                String HOME = sh (script: 'echo $HOME', returnStdout: true).trim()

                // Gets environment variable and sets it to a groovy var
                String XDG = sh (script: 'echo $XDG_CONFIG_HOME', returnStdout: true).trim()

                if (grypeConfig != null) {
                    ARGS += "--config ${grypeConfig}"
                    echo "Grype file explicitly specified in pipeline_config.groovy"
                }
                else if (fileExists('.grype.yaml')) {
                    grypeConfig = '.grype.yaml'
                    ARGS += "--config ${grypeConfig}"
                    echo "Found .grype.yaml"
                }
                else if (fileExists('.grype/config.yaml')) {
                    grypeConfig = '.grype/config.yaml'
                    ARGS += "--config ${grypeConfig}"
                    echo "Found .grype/config.yaml"
                }
                else if (fileExists("${HOME}/.grype.yaml")) {
                    grypeConfig = "${HOME}/.grype.yaml"
                    ARGS += "--config ${grypeConfig}"
                    echo "Found ~/.grype.yaml"
                }
                else if (fileExists("${XDG}/grype/config.yaml")) {
                    grypeConfig = "${XDG}/grype/config.yaml"
                    ARGS += "--config ${grypeConfig}"
                    echo "Found <XDG_CONFIG_HOME>/grype/config.yaml"
                }

                def images = get_images_to_build()
                images.each { img ->
                    // Use $img.repo to help name our results uniquely. Checks to see if a forward slash exists and splits the string at that location.
                    String rawResultsFile, transformedResultsFile
                    if (img.repo.contains("/")) {
                        String[] repoImageName = img.repo.split('/')
                        rawResultsFile = repoImageName[1] + '-grype-scan-results' + resultsFileFormat
                        transformedResultsFile = repoImageName[1] + '-grype-scan-results.txt'
                    }
                    else {
                        rawResultsFile = "${img.repo}-grype-scan-results" + resultsFileFormat
                        transformedResultsFile = "${img.repo}-grype-scan-results.txt"
                    }

                    // perform the grype scan
                    try {
                        sh "grype ${img.registry}/${img.repo}:${img.tag} ${ARGS} >> ${rawResultsFile}"
                    }
                    // Catch the error on quality gate failure
                    catch(Exception err) {
                        shouldFail = true
                        echo "Failed: ${err}"
                        echo "Grype Quality Gate Failed. There are one or more CVE's that exceed the maximum allowed severity rating!"
                    }
                    // display the results in a human-readable format
                    finally {
                        //Specific to BASS team. Allows Backstage to ingest JSON but also creates a human readable artifact.
                        if (outputFormat == "json" && grypeConfig != null) {
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

            if(shouldFail){
                error "One or more image scans with Grype failed"
            }
        }
    }
}

void findSbom() {
    def sbomPattern = ~'json|cyclonedx|json'

}
