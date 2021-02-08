/*
  Copyright © 2018 Booz Allen Hamilton. All Rights Reserved.
  This software package is licensed under the Booz Allen Public License. The license can be found in the License file or at http://boozallen.github.io/licenses/bapl
*/

package libraries.docker

public class BuildSpec extends JTEPipelineSpecification {

  def Build = null

  def setup() {
    Build = loadPipelineScriptForStep("docker", "build")
    explicitlyMockPipelineStep("get_images_to_build")
    explicitlyMockPipelineStep("login_to_registry")
    Build.getBinding().setVariable("config", [:])

    getPipelineMock("get_images_to_build")() >> {
      def images = []
      images << [registry: "reg1", repo: "repo1", context: "context1", tag: "tag1"]
      images << [registry: "reg2", repo: "repo2", context: "context2", tag: "tag2"]
      return images
    }
  }

  def "Unstash workspace Before Building Images" () {
    when:
      Build()
    then:
      1 * getPipelineMock("unstash")("workspace")
    then:
      (1.._) * getPipelineMock("sh")({it =~ /^docker build .*/})
  }

  def "Log Into Registry Before Pushing Images" () {
    when:
      Build()
    then:
      1 * getPipelineMock("login_to_registry")(_)
    then:
      (1.._) * getPipelineMock("sh")({it =~ /^docker push .*/})
  }

  def "Each Image is Properly Built" () {
    when:
      Build()
    then:
      1 * getPipelineMock("sh")("docker build context1 -t reg1/repo1:tag1 ")
      1 * getPipelineMock("sh")("docker build context2 -t reg2/repo2:tag2 ")
  }

  def "Each Image is Properly Pushed" () {
    when:
      Build()
    then:
      1 * getPipelineMock("sh")("docker push reg1/repo1:tag1")
      1 * getPipelineMock("sh")("docker push reg2/repo2:tag2")
  }

  def "If value is true for config.remove_local_image,remove local image" () {
    setup:
      Build.getBinding().setVariable("config", [remove_local_image: true])
    when:
      Build()
    then:
      1 * getPipelineMock("sh")("docker rmi -f reg1/repo1:tag1 2> /dev/null")
      1 * getPipelineMock("sh")("docker rmi -f reg2/repo2:tag2 2> /dev/null")
  }

  def "If value is false for config.remove_local_image,do not remove local image" () {
    setup:
      Build.getBinding().setVariable("config", [remove_local_image: false])
    when:
      Build()
    then:
      0 * getPipelineMock("sh")("docker rmi -f reg1/repo1:tag1 2> /dev/null")
      0 * getPipelineMock("sh")("docker rmi -f reg2/repo2:tag2 2> /dev/null")
  }

  def "If value is null for config.remove_local_image,do not remove local image" () {
    setup:
      Build.getBinding().setVariable("config", [remove_local_image: null])
    when:
      Build()
    then:
      0 * getPipelineMock("sh")("docker rmi -f reg1/repo1:tag1 2> /dev/null")
      0 * getPipelineMock("sh")("docker rmi -f reg2/repo2:tag2 2> /dev/null")
  }

  def "If value is not a Boolean for config.remove_local_image,throw error" () {
    setup:
      Build.getBinding().setVariable("config", [remove_local_image: "true"])
    when:
      Build()
    then:
      1 * getPipelineMock("error")(_)
  }

}
