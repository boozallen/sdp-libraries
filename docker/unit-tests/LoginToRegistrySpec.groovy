/*
  Copyright © 2018 Booz Allen Hamilton. All Rights Reserved.
  This software package is licensed under the Booz Allen Public License. The license can be found in the License file or at http://boozallen.github.io/licenses/bapl
*/

import com.homeaway.devtools.jenkins.testing.JenkinsPipelineSpecification

public class LoginToRegistrySpec extends JenkinsPipelineSpecification {

  def LoginToRegistry = null

  def setup() {
    LoginToRegistry = loadPipelineScriptForTest("docker/login_to_registry.groovy")

    getPipelineMock("usernamePassword.call")(_ as Map) >> { _arguments ->
      LoginToRegistry.getBinding().setVariable("user", "user")
      LoginToRegistry.getBinding().setVariable("pass", "pass")
    }


  }

  def "Missing application_image_repository Throws Error" () {
    setup:
      LoginToRegistry.getBinding().setVariable("pipelineConfig", [application_image_repository: null])
    when:
      LoginToRegistry()
    then:
      1 * getPipelineMock("error")("application_image_repository not defined in pipeline config")
  }

  def "Missing application_image_repository_credential Throws Error" () {
    setup:
      LoginToRegistry.getBinding().setVariable("pipelineConfig", [application_image_repository: "Sulu", application_image_repository_credential: null])
    when:
      LoginToRegistry()
    then:
      1 * getPipelineMock("error")("application_image_repository_credential not defined in pipeline config")
  }

  def "Docker Login Command Is Run" () {
    setup:
      LoginToRegistry.getBinding().setVariable("pipelineConfig", [application_image_repository: "Sulu", application_image_repository_credential: "Scotty"])
    when:
      LoginToRegistry()
    then:
      1 * getPipelineMock("usernamePassword.call")([credentialsId: "Scotty", passwordVariable: 'pass', usernameVariable: 'user']) >> {
        LoginToRegistry.getBinding().setVariable("user", "user")
        LoginToRegistry.getBinding().setVariable("pass", "pass")
      }
      1 * getPipelineMock("sh")("echo pass | docker login -u user --password-stdin Sulu")

  }

}
