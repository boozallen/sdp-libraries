/*
  Copyright © 2018 Booz Allen Hamilton. All Rights Reserved.
  This software package is licensed under the Booz Allen Public License. The license can be found in the License file or at http://boozallen.github.io/licenses/bapl
*/

package libraries.grype

import JTEPipelineSpecification


public class ContainerImageScanTestSpec extends JTEPipelineSpecification {

    def ContainerImageScan = null

    def setup() {
        ContainerImageScan = loadPipelineScriptForStep("grype", "container_image_scan")
        explicitlyMockPipelineStep("inside_sdp_image")
        explicitlyMockPipelineStep("login_to_registry")
        explicitlyMockPipelineStep("get_images_to_build")
        ContainerImageScan.getBinding().setVariable("config", [:])

        getPipelineMock("get_images_to_build")() >> {
            def images = []
            images << [registry: "test_registry", repo: "test_repo", context: "image1", tag: "4321dcba"]
            images << [registry: "test_registry", repo: "test_repo", context: "image2", tag: "4321dcba"]
            return images
        }
    
    }

    def "Unstash workspace Before Scanning Images" () {
        when:
            ContainerImageScan()
        then:
            1 * getPipelineMock("unstash")("workspace")
    }

     }


