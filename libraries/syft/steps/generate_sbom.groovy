/*
  Copyright © 2022 Booz Allen Hamilton. All Rights Reserved.
  This software package is licensed under the Booz Allen Public License.
  The license can be found in the License file or at http://boozallen.github.io/licenses/bapl
*/
package libraries.syft.steps

void call() {
    stage('Generate SBOM using Syft') {
        //Import settings from config
        String raw_results_file = config?.raw_results_file ?: 'syft-sbom-results' // leave off file extension so that it can be added based off off selected formats
        String sbom_container = config?.sbom_container ?: 'syft:0.47.0'
        ArrayList sbom_format = config?.sbom_format ?: ["json"]
        String ARGS = "-q"
        String artifacts = ""
        String formatter

        //Get list of images to scan (assuming same set built by Docker)
        def images = get_images_to_build()
        inside_sdp_image "${sbom_container}", {
            login_to_registry {
                unstash "workspace"
                images.each { img ->
                    // perform the syft scan
                    String results_name = "${img.repo}-${img.tag}-${raw_results_file}".replaceAll("/","-")
                    //for(int i = 0;i < sbom_format.size();i++) {
                      sbom_format.each { format ->
                        formatter += " "
                        if(format == "json" || "cyclonedx-json" || "spdx-json" || "github") {
                          formatter += "${results_name}-${format}.json"
                        }
                        else if(format == "text" || "sdpx-tag-value" || "table") {
                          formatter += "${results_name}-${format}.txt"
                        }
                        else if (format == "cyclonedx-xml") {
                          formatter += "${results_name}-${format}.xml"
                        }
                        else {
                          //throw exception not a supported format
                          echo " Bad Format"
                        }
                        ARGS += "-o ${format}=${formatter}"
                        artifacts += "${formatter}"
                    }
                    
                    //println(ARGS)
                    sh "syft ${img.registry}/${img.repo}:${img.tag} ${ARGS}"
                    sh "ls -alh"

                    archiveArtifacts artifacts: "${artifacts}"
                }
                stash "workspace"
            }
        }
    }
}
