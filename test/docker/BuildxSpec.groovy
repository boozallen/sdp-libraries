/*
  Copyright © 2018 Booz Allen Hamilton. All Rights Reserved.
  This software package is licensed under the Booz Allen Public License. The license can be found in the License file or at http://boozallen.github.io/licenses/bapl
*/

package libraries.docker

public class BuildxSpec extends JTEPipelineSpecification {

  def Buildx = null

  def setup() {
    Buildx = loadPipelineScriptForStep("docker", "buildx")
    explicitlyMockPipelineStep("get_images_to_build")
    explicitlyMockPipelineStep("login_to_registry")
    Buildx.getBinding().setVariable("config", [build_strategy: 'buildx'])

    getPipelineMock("get_images_to_build")() >> {
      def images = []
      images << [registry: "reg1", repo: "repo1", context: "context1", tag: "tag1", build_args: null, platforms: ["arm", "amd64"], useLatestTag: true]
      images << [registry: "reg2", repo: "repo2", context: "context2", tag: "tag2", build_args: [BASE_IMAGE:"image"], platforms: ["arm", "amd64"], useLatestTag: false]
      return images
    }
  }

  def "Unstash workspace Before Building Images" () {
    when:
      Buildx()
    then:
      1 * getPipelineMock("unstash")("workspace")
    then:
      (1.._) * getPipelineMock("sh")({it =~ /^docker buildx */})
  }

  def "Log Into Registry Before Pushing Images" () {
    when:
      Buildx()
    then:
      1 * getPipelineMock("login_to_registry")(_)
    then:
      (1.._) * getPipelineMock("sh")({it =~ /^docker buildx */})
  }

  def "Each Image is Properly Built and pushed" () {
    when:
      Buildx()
    then:
      1 * getPipelineMock("sh")("docker buildx build context1 -t reg1/repo1:tag1 -t reg1/repo1:latest  --platform arm,amd64 --push")
      1 * getPipelineMock("sh")("docker buildx build context2 -t reg2/repo2:tag2 --build-arg BASE_IMAGE='image' --platform arm,amd64 --push")
  }

  def "If value is true for config.setExperimentalFlag,set the experimental flag" () {
    setup:
      Buildx.getBinding().setVariable("config", [setExperimentalFlag: true, build_strategy: 'buildx'])
    when:
      Buildx()
    then:
      1 * getPipelineMock("sh")("DOCKER_CLI_EXPERIMENTAL=enabled docker buildx create --name smartbuilder --driver docker-container --use")
      1 * getPipelineMock("sh")("DOCKER_CLI_EXPERIMENTAL=enabled docker buildx build context1 -t reg1/repo1:tag1 -t reg1/repo1:latest  --platform arm,amd64 --push")
      1 * getPipelineMock("sh")("DOCKER_CLI_EXPERIMENTAL=enabled docker buildx build context2 -t reg2/repo2:tag2 --build-arg BASE_IMAGE='image' --platform arm,amd64 --push")
  }
  def "If value is false for config.setExperimentalFlag,set the experimental flag to false" () {
    setup:
      Buildx.getBinding().setVariable("config", [setExperimentalFlag: false, build_strategy: 'buildx'])
    when:
      Buildx()
    then:
      0 * getPipelineMock("sh")("DOCKER_CLI_EXPERIMENTAL=enabled docker buildx create --name smartbuilder --driver docker-container --use")
      0 * getPipelineMock("sh")("DOCKER_CLI_EXPERIMENTAL=enabled docker buildx build context1 -t reg1/repo1:tag1 -t reg1/repo1:latest  --platform arm,amd64 --push")
      0 * getPipelineMock("sh")("DOCKER_CLI_EXPERIMENTAL=enabled docker buildx build context2 -t reg2/repo2:tag2 --build-arg BASE_IMAGE='image' --platform arm,amd64 --push")
  }

}
