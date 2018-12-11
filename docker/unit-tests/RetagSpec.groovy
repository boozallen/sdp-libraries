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
    explicitlyMockPipelineVariable("get_images_to_build")


  }

  def "A Single Image is Properly Read & Retagged" () {

    when:
      Retag("tag_viejo", "tag_nuevo")
    then:
      1 * getPipelineMock("get_images_to_build.call")() >> [[repo: "Repo", path: "Path"]]
      1 * getPipelineMock("sh")("docker pull Repo/Path:tag_viejo" )
      1 * getPipelineMock("sh")("docker tag Repo/Path:tag_viejo Repo/Path:tag_nuevo")
      1 * getPipelineMock("sh")("docker push Repo/Path:tag_nuevo")
  }

  def "Multiple Images are Properly Read & Retagged" () {
    def images = []
    images << [repo: "Repo1", path: "Path1"]
    images << [repo: "Repo2", path: "Path2"]
    images << [repo: "Repo3", path: "Path3"]
    when:
      Retag("tag_viejo", "tag_nuevo")
    then:
      1 * getPipelineMock("get_images_to_build.call")() >> images

      images.each{ img ->
        1 * getPipelineMock("sh")("docker pull ${img.repo}/${img.path}:tag_viejo" )
        1 * getPipelineMock("sh")("docker tag ${img.repo}/${img.path}:tag_viejo ${img.repo}/${img.path}:tag_nuevo")
        1 * getPipelineMock("sh")("docker push ${img.repo}/${img.path}:tag_nuevo")
      }
  }

}
