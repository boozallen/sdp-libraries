/*
  Copyright © 2018 Booz Allen Hamilton. All Rights Reserved.
  This software package is licensed under the Booz Allen Public License. The license can be found in the License file or at http://boozallen.github.io/licenses/bapl
*/

import com.homeaway.devtools.jenkins.testing.JenkinsPipelineSpecification

public class LoginToRegistrySpec extends JenkinsPipelineSpecification {

  def LoginToRegistry = null

  def setup() {
    LoginToRegistry = loadPipelineScriptForTest("docker/login_to_registry.groovy")

    explicitlyMockPipelineStep("get_registry_info")
  }

  def "Get_repo_info method's Values Are Passed to withCredentials" () {
    when:
      LoginToRegistry()
    then:
      1 * getPipelineMock("get_registry_info")() >> ["test_registry", "test_cred_id"]
    then:
      1 * getPipelineMock("usernamePassword.call")([credentialsId: "test_cred_id", passwordVariable: 'pass', usernameVariable: 'user']) >> {
        LoginToRegistry.getBinding().setVariable("user", "user")
        LoginToRegistry.getBinding().setVariable("pass", "pass")
      }
  }

  def "Docker Login Command Is Run" () {
    setup:
      getPipelineMock("get_registry_info")() >> ["test_registry", "test_cred_id"]
    when:
      LoginToRegistry()
    then:
      1 * getPipelineMock("usernamePassword.call")([credentialsId: "test_cred_id", passwordVariable: 'pass', usernameVariable: 'user']) >> {
        LoginToRegistry.getBinding().setVariable("user", "user")
        LoginToRegistry.getBinding().setVariable("pass", "pass")
      }
      1 * getPipelineMock("sh")("echo pass | docker login -u user --password-stdin test_registry")

  }

}
