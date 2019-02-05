/*
  Copyright © 2018 Booz Allen Hamilton. All Rights Reserved.
  This software package is licensed under the Booz Allen Public License. The license can be found in the License file or at http://boozallen.github.io/licenses/bapl
*/

import com.homeaway.devtools.jenkins.testing.JenkinsPipelineSpecification

public class BuildSpec extends JenkinsPipelineSpecification {

  def Build = null

  def setup() {
    Build = loadPipelineScriptForTest("./docker/build.groovy")
    explicitlyMockPipelineStep("get_images_to_build")
    explicitlyMockPipelineStep("login_to_registry")

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
      1 * getPipelineMock("login_to_registry")()
    then:
      (1.._) * getPipelineMock("sh")({it =~ /^docker push .*/})
  }

  def "Each Image is Properly Built" () {
    when:
      Build()
    then:
      1 * getPipelineMock("sh")("docker build context1 -t reg1/repo1:tag1")
      1 * getPipelineMock("sh")("docker build context2 -t reg2/repo2:tag2")
  }

  def "Each Image is Properly Pushed" () {
    when:
      Build()
    then:
      1 * getPipelineMock("sh")("docker push reg1/repo1:tag1")
      1 * getPipelineMock("sh")("docker push reg2/repo2:tag2")
  }

}
