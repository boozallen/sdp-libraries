/*
  Copyright © 2018 Booz Allen Hamilton. All Rights Reserved.
  This software package is licensed under the Booz Allen Public License. The license can be found in the License file or at http://boozallen.github.io/licenses/bapl
*/

package libraries.docker

public class RetagSpec extends JTEPipelineSpecification {

  def Retag = null

  def setup() {
    Retag = loadPipelineScriptForStep("docker","retag")
    explicitlyMockPipelineStep("login_to_registry")
    explicitlyMockPipelineStep("get_images_to_build")
    Retag.getBinding().setVariable("config", [:])
  }

  def "Log Into Registry Before Pushing or Pulling Images" () {
    when:
      Retag("tag_viejo", "tag_nuevo")
    then:
      1 * getPipelineMock("login_to_registry")(_)
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

  def "If value is true for config.remove_local_image,remove local image" () {
    setup:
      Retag.getBinding().setVariable("config", [remove_local_image: true])
    when:
      Retag("tag_viejo", "tag_nuevo")
    then:
      1 * getPipelineMock("get_images_to_build")() >> [[registry: "Reg", repo: "Repo"]]
      1 * getPipelineMock("sh")("docker rmi -f Reg/Repo:tag_nuevo 2> /dev/null")
  }

  def "If value is false for config.remove_local_image,do not remove local image" () {
    setup:
      Retag.getBinding().setVariable("config", [remove_local_image: false])
    when:
      Retag("tag_viejo", "tag_nuevo")
    then:
      1 * getPipelineMock("get_images_to_build")() >> [[registry: "Reg", repo: "Repo"]]
      0 * getPipelineMock("sh")("docker rmi -f Reg/Repo:tag_nuevo 2> /dev/null")
  }

  def "If value is null for config.remove_local_image,do not remove local image" () {
    setup:
      Retag.getBinding().setVariable("config", [remove_local_image: null])
    when:
      Retag("tag_viejo", "tag_nuevo")
    then:
      1 * getPipelineMock("get_images_to_build")() >> [[registry: "Reg", repo: "Repo"]]
      0 * getPipelineMock("sh")("docker rmi -f Reg/Repo:tag_nuevo 2> /dev/null")
  }

  def "If value is not a Boolean for config.remove_local_image,throw error" () {
    setup:
      Retag.getBinding().setVariable("config", [remove_local_image: "true"])
    when:
      Retag("tag_viejo", "tag_nuevo")
    then:
      1 * getPipelineMock("error")(_)
  }

}
