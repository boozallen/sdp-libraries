/*
  Copyright © 2022 Booz Allen Hamilton. All Rights Reserved.
  This software package is licensed under the Booz Allen Public License.
  The license can be found in the License file or at http://boozallen.github.io/licenses/bapl
*/

package libraries.syft

public class GenerateSBOMSpec extends JTEPipelineSpecification {
  def GenerateSBOM = null

  def setup() {
    GenerateSBOM = loadPipelineScriptForStep("syft", "generate_sbom")

    GenerateSBOM.getBinding().setVariable("config", [:])

    explicitlyMockPipelineStep("login_to_registry")
    explicitlyMockPipelineStep("inside_sdp_image")
    explicitlyMockPipelineVariable("get_images_to_build")

    getPipelineMock("get_images_to_build.call")() >> {
      def images = []
      images << [registry: "ghcr.io/boozallen/sdp-images", repo: "syft", context: "syft", tag: "latest"]
      images << [registry: "ghcr.io/boozallen/sdp-images", repo: "grype", context: "grype", tag: "latest"]
      return images
    }
  }

  def "Generates Software Bill of Materials file" () {
    given:
      GenerateSBOM.getBinding().setVariable("config", [sbom_format: ["json"]])
    when:
      GenerateSBOM()
    then:
      1 * getPipelineMock('sh').call('syft ghcr.io/boozallen/sdp-images/syft:latest -q -o json=syft-latest-syft-sbom-results-json.json ')
      1 * getPipelineMock('sh').call('syft ghcr.io/boozallen/sdp-images/grype:latest -q -o json=grype-latest-syft-sbom-results-json.json ')
  }

  def "Archives SBOM file as expected" () {
    when:
      GenerateSBOM()
    then:
      2 * getPipelineMock('archiveArtifacts.call')(_ as Map)
  }
}
