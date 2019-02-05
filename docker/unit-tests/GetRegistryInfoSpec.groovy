/*
  Copyright © 2018 Booz Allen Hamilton. All Rights Reserved.
  This software package is licensed under the Booz Allen Public License. The license can be found in the License file or at http://boozallen.github.io/licenses/bapl
*/

import com.homeaway.devtools.jenkins.testing.JenkinsPipelineSpecification

public class GetRegistryInfoSpec extends JenkinsPipelineSpecification {

  def GetRegistryInfo = null

  def setup() {
    GetRegistryInfo = loadPipelineScriptForTest("docker/get_registry_info.groovy")
  }

  def "Missing Registry Throws Error" () {
    setup:
      GetRegistryInfo.getBinding().setVariable("config", [registry: null, cred: "credential"])
      def missing_reg_message = "Application Docker Image Registry, libraries.docker.registry, not defined in pipeline config"
    when:
      GetRegistryInfo()
    then:
      1 * getPipelineMock("error")(missing_reg_message)
  }

  def "Missing Credential Throws Error" () {
    setup:
      GetRegistryInfo.getBinding().setVariable("config", [registry: "registry", cred: null])
      def missing_cred_message = "Application Docker Image Registry Credential, libraries.docker.cred, not defined in pipeline config"
    when:
      GetRegistryInfo()
    then:
      1 * getPipelineMock("error")(missing_cred_message)
  }

  def "Missing Registry and Credential Outputs Combined Error Message" () {
    setup:
      GetRegistryInfo.getBinding().setVariable("config", [:])
      def missing_reg_message = "Application Docker Image Registry, libraries.docker.registry, not defined in pipeline config"
      def missing_cred_message = "Application Docker Image Registry Credential, libraries.docker.cred, not defined in pipeline config"
    when:
      GetRegistryInfo()
    then:
      1 * getPipelineMock("error")("${missing_reg_message}; ${missing_cred_message}")
  }

  def "Return Value is a Two-Element Array of [Registry, Credential]" () {
    setup:
      GetRegistryInfo.getBinding().setVariable("config", [registry: "reg", cred: "cred"])
    when:
      def regInfo = GetRegistryInfo()
    then:
      0 * getPipelineMock("error")(_)
      regInfo == ["reg", "cred"]
  }


}
