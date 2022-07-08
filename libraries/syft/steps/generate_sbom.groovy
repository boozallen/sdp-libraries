/*
  Copyright © 2022 Booz Allen Hamilton. All Rights Reserved.
  This software package is licensed under the Booz Allen Public License.
  The license can be found in the License file or at http://boozallen.github.io/licenses/bapl
*/
package libraries.syft.steps

void call() {
    node {
        //Import settings from config
        String raw_results_file = config?.raw_results_file ?: 'syft-sbom-results.json'
        String sbom_container = config?.sbom_container ?: 'syft:latest'

        //Get list of images to scan (assuming same set built by Docker)
        def images = get_images_to_build()

        stage('Generate SBOM using Syft') {
            inside_sdp_image "${sbom_container}", {
                unstash "workspace"
                images.each { img ->
                    // perform the syft scan
                    sh "syft ${img.registry}/${img.repo}:${img.tag} -o json=${img.repo}-${img.tag}-${raw_results_file}"

                    // archive the results
                    archiveArtifacts artifacts: "${img.repo}-${img.tag}-${raw_results_file}"
                }
                stash "workspace"
            }
        }
    }
}
