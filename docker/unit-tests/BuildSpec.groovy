/*
  Copyright © 2018 Booz Allen Hamilton. All Rights Reserved.
  This software package is licensed under the Booz Allen Public License. The license can be found in the License file or at http://boozallen.github.io/licenses/bapl
*/

import com.homeaway.devtools.jenkins.testing.JenkinsPipelineSpecification

public class BuildSpec extends JenkinsPipelineSpecification {

  def Build = null

  def setup() {
    Build = loadPipelineScriptForTest("./docker/build.groovy")
    explicitlyMockPipelineVariable("get_images_to_build")
    explicitlyMockPipelineVariable("login_to_registry")

    getPipelineMock("get_images_to_build.call")() >> {
      def images = []
      images << [repo: "repo1", path: "path1", context: "context1", tag: "tag1"]
      images << [repo: "repo2", path: "path2", context: "context2", tag: "tag2"]
      return images
    }
  }

  def "Docker Build & Push Called For Each Image" () {
    when:
      Build()
    then:
      1 * getPipelineMock("sh")("docker build context1 -t repo1/path1:tag1")
      1 * getPipelineMock("sh")("docker push repo1/path1:tag1")
      1 * getPipelineMock("sh")("docker build context2 -t repo2/path2:tag2")
      1 * getPipelineMock("sh")("docker push repo2/path2:tag2")
  }

}
