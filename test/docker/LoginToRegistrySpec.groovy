/*
  Copyright © 2018 Booz Allen Hamilton. All Rights Reserved.
  This software package is licensed under the Booz Allen Public License. The license can be found in the License file or at http://boozallen.github.io/licenses/bapl
*/

package libraries.docker

public class LoginToRegistrySpec extends JTEPipelineSpecification {

  def login_to_registry = null

  def setup() {
    login_to_registry = loadPipelineScriptForStep("docker", "login_to_registry")
    login_to_registry.getBinding().setVariable("config", [:])
    explicitlyMockPipelineStep("get_registry_info")
  }

  def "login to registry executes closure"(){
    when: 
    1 * getPipelineMock("get_registry_info")() >> { return ["test_registry", "test_cred_id"] }
    login_to_registry{
      echo "hi"
    }
    then:
    1 * getPipelineMock("echo")("hi")
  }


  def "login to registry passes method parameters to docker.withRegistry if provided"(){
    when: 
    1 * getPipelineMock("get_registry_info")() >> { return ["test_registry", "test_cred_id"] }
    login_to_registry("parameter_registry_url", "parameter_credential_id"){
      echo "hi"
    }
    then:
    1 * getPipelineMock("docker.withRegistry")("parameter_registry_url", "parameter_credential_id", _)
  }

  def "default protocol for user-provided registry is https://"(){
    when: 
    1 * getPipelineMock("get_registry_info")() >> { return ["test_registry", "test_cred_id"] }
    login_to_registry{
      echo "hi"
    }
    then:
    1 * getPipelineMock("docker.withRegistry")("https://test_registry", _, _)
  }

  def "setting the config.registry_protocol changes the protocol"(){
    when:
    1 * getPipelineMock("get_registry_info")() >> { return ["test_registry", "test_cred_id"] }
    login_to_registry.getBinding().setVariable("config", [ registry_protocol: "http://" ])
    login_to_registry{
      echo "hi"
    }
    then: 
    1 * getPipelineMock("docker.withRegistry")("http://test_registry", _, _)
  }



}
