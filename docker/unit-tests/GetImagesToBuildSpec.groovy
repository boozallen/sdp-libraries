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
    explicitlyMockPipelineStep("get_registry_info")

    getPipelineMock("get_registry_info")() >> ["test_registry", "test_cred"]
  }

  def "Get_registry_info method is called" () {
    setup:
      GetImagesToBuild.getBinding().setVariable("config", [:])
    when:
      GetImagesToBuild()
    then:
      1 * getPipelineMock("get_registry_info")() >> ["test_registry", "test_cred"]
  }

  def "path_prefix Is An Empty String If No repo_path_prefix Is Set In The Config" () {
    setup:
      GetImagesToBuild.getBinding().setVariable("config", [repo_path_prefix: null, build_strategy: build_strategy])
      GetImagesToBuild.getBinding().setVariable("env", [REPO_NAME: "git_repo", GIT_SHA: "8675309"])
      getPipelineMock("findFiles")([glob: "*/Dockerfile"]) >> [[path: "service/Dockerfile"]]
    when:
      def imageList = GetImagesToBuild()
    then:
      imageList == [[registry: "test_registry", repo:repo , context: build_context, tag: "8675309"]]
    where:
      build_strategy | build_context | repo
      "dockerfile"   | "."           | "git_repo"
      "modules"      | "service"     | "git_repo_service"
  }

  def "path_prefix Is Properly Prepended To Repo Value" () {
    setup:
      GetImagesToBuild.getBinding().setVariable("config", [repo_path_prefix: "test_prefix", build_strategy: build_strategy])
      GetImagesToBuild.getBinding().setVariable("env", [REPO_NAME: "git_repo", GIT_SHA: "8675309"])
      getPipelineMock("findFiles")([glob: "*/Dockerfile"]) >> [[path: "service/Dockerfile"]]
    when:
      def imageList = GetImagesToBuild()
    then:
      imageList == [[registry: "test_registry", repo: repo, context: build_context, tag: "8675309"]]
    where:
      build_strategy | build_context | repo
      "dockerfile"   | "."           | "test_prefix/git_repo"
      "modules"      | "service"     | "test_prefix/git_repo_service"
  }

  def "Invalid build_strategy Throws Error" () {
    setup:
      GetImagesToBuild.getBinding().setVariable("config", [build_strategy: x])
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
          registry: "test_registry",
          repo: "Vulcan_planet",
          context: "planet",
          tag: "1234abcd"
        ], [
          registry: "test_registry",
          repo: "Vulcan_planet2",
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
      imageList == [[registry: "test_registry", repo: "Vulcan", context: ".", tag: "5678efgh"]]
  }

}
