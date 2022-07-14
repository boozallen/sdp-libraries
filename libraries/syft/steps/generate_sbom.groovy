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

        images.each { img ->
            // pull and save images as tarballs
            String archive_name = "${img.registry}-${img.repo}-${img.tag}.tar".replaceAll("/","-")
            sh "docker save ${img.registry}/${img.repo}:${img.tag} > ${archive_name}"
        }

        stage('Generate SBOM using Syft') {
            inside_sdp_image "${sbom_container}", {
                unstash "workspace"
                images.each { img ->
                    // perform the syft scan
                    String archive_name = "${img.registry}-${img.repo}-${img.tag}.tar".replaceAll("/","-")
                    String results_name = "${img.repo}-${img.tag}-${raw_results_file}".replaceAll("/","-")
                    sh "syft ${archive_name} -o json=${results_name}"

                    // archive the results
                    archiveArtifacts artifacts: "${results_name}"
                }
                stash "workspace"
            }
        }
    }
}
