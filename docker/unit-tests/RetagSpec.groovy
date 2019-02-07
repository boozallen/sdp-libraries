/*
  Copyright © 2018 Booz Allen Hamilton. All Rights Reserved.
  This software package is licensed under the Booz Allen Public License. The license can be found in the License file or at http://boozallen.github.io/licenses/bapl
*/

import com.homeaway.devtools.jenkins.testing.JenkinsPipelineSpecification

public class RetagSpec extends JenkinsPipelineSpecification {

  def Retag = null

  def setup() {
    Retag = loadPipelineScriptForTest("docker/retag.groovy")
    explicitlyMockPipelineStep("login_to_registry")
    explicitlyMockPipelineStep("get_images_to_build")
  }

  def "Log Into Registry Before Pushing or Pulling Images" () {
    when:
      Retag("tag_viejo", "tag_nuevo")
    then:
      1 * getPipelineMock("login_to_registry")()
    then:
      1 * getPipelineMock("get_images_to_build")() >> [[registry: "Reg", repo: "Repo"]]
      (1.._) * getPipelineMock("sh")({it =~ /^docker .*/})

  }

  def "A Single Image is Properly Read & Retagged" () {

    when:
      Retag("tag_viejo", "tag_nuevo")
    then:
      1 * getPipelineMock("get_images_to_build")() >> [[registry: "Reg", repo: "Repo"]]
      1 * getPipelineMock("sh")("docker pull Reg/Repo:tag_viejo" )
      1 * getPipelineMock("sh")("docker tag Reg/Repo:tag_viejo Reg/Repo:tag_nuevo")
      1 * getPipelineMock("sh")("docker push Reg/Repo:tag_nuevo")
  }

  def "Multiple Images are Properly Read & Retagged" () {
    def images = []
    images << [registry: "Reg1", repo: "Repo1"]
    images << [registry: "Reg2", repo: "Repo2"]
    images << [registry: "Reg3", repo: "Repo3"]
    when:
      Retag("tag_viejo", "tag_nuevo")
    then:
      1 * getPipelineMock("get_images_to_build")() >> images

      images.each{ img ->
        1 * getPipelineMock("sh")("docker pull ${img.registry}/${img.repo}:tag_viejo" )
        1 * getPipelineMock("sh")("docker tag ${img.registry}/${img.repo}:tag_viejo ${img.registry}/${img.repo}:tag_nuevo")
        1 * getPipelineMock("sh")("docker push ${img.registry}/${img.repo}:tag_nuevo")
      }
  }

}
