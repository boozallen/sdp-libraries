/*
  Copyright © 2018 Booz Allen Hamilton. All Rights Reserved.
  This software package is licensed under the Booz Allen Public License. The license can be found in the License file or at http://boozallen.github.io/licenses/bapl
*/

import com.homeaway.devtools.jenkins.testing.JenkinsPipelineSpecification

public class GetImagesToBuildSpec extends JenkinsPipelineSpecification {

  def GetImagesToBuild = null

  public static class DummyException extends RuntimeException {
    public DummyException(String _message) { super( _message ); }
  }

  def setup() {
    GetImagesToBuild = loadPipelineScriptForTest("docker/get_images_to_build.groovy")
  }

  def "Missing application_image_repository Throws Error" () {
    setup:
      GetImagesToBuild.getBinding().setVariable("config", [:])
      GetImagesToBuild.getBinding().setVariable("pipelineConfig", [application_image_repository: null])
    when:
      GetImagesToBuild()
    then:
      1 * getPipelineMock("error")("application_image_repository not defined in pipeline config.")
  }

  def "Invalid build_strategy Throws Error" () {
    setup:
      GetImagesToBuild.getBinding().setVariable("config", [build_strategy: x])
      GetImagesToBuild.getBinding().setVariable("pipelineConfig", [application_image_repository: "Enterprise"])
    when:
      GetImagesToBuild()
    then:
      y * getPipelineMock("error")("build strategy: ${x} not one of [docker-compose, modules, dockerfile]")
    where:
      x                | y
      "docker-compose" | 0
      "Kobayashi Maru" | 1
      "modules"        | 0
      "dockerfile"     | 0
      "Starfleet"      | 1
  }

  def "docker-compose build_strategy Throws Error" () {
    setup:
      GetImagesToBuild.getBinding().setVariable("config", [build_strategy: "docker-compose"])
      GetImagesToBuild.getBinding().setVariable("pipelineConfig", [application_image_repository: "Enterprise"])
    when:
      GetImagesToBuild()
    then:
      1 * getPipelineMock("error")("docker-compose build strategy not implemented yet")
  }

  def "modules build_strategy Builds Correct Image List" () {
    setup:
      GetImagesToBuild.getBinding().setVariable("config", [build_strategy: "modules"])
      GetImagesToBuild.getBinding().setVariable("env", [REPO_NAME: "Vulcan", GIT_SHA: "1234abcd"])
      getPipelineMock("findFiles")([glob: "*/Dockerfile"]) >> [[path: "planet/Romulus"], [path: "planet2/Earth"]]
      GetImagesToBuild.getBinding().setVariable("pipelineConfig", [application_image_repository: "Enterprise"])
    when:
      def imageList = GetImagesToBuild()
    then:
      imageList == [
        [
          repo: "Enterprise",
          path: "Vulcan_planet",
          context: "planet",
          tag: "1234abcd"
        ], [
          repo: "Enterprise",
          path: "Vulcan_planet2",
          context: "planet2",
          tag: "1234abcd"
        ]
      ]

  }

  def "dockerfile build_strategy Builds Correct Image List" () {
    setup:
      GetImagesToBuild.getBinding().setVariable("config", [build_strategy: "dockerfile"])
      GetImagesToBuild.getBinding().setVariable("env", [REPO_NAME: "Vulcan", GIT_SHA: "5678efgh"])
      getPipelineMock("findFiles")([glob: "*/Dockerfile"]) >> [[path: "planet/Romulus"]]
      GetImagesToBuild.getBinding().setVariable("pipelineConfig", [application_image_repository: "Enterprise"])
    when:
      def imageList = GetImagesToBuild()
    then:
      imageList == [[repo: "Enterprise", path: "Vulcan", context: ".", tag: "5678efgh"]]
  }

}
