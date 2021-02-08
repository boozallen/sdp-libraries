/*
  Copyright © 2018 Booz Allen Hamilton. All Rights Reserved.
  This software package is licensed under the Booz Allen Public License. The license can be found in the License file or at http://boozallen.github.io/licenses/bapl
*/

package libraries.docker

public class LoginToRegistrySpec extends JTEPipelineSpecification {

  def login_to_registry = null

  def setup() {
    login_to_registry = loadPipelineScriptForStep("docker", "login_to_registry")
    explicitlyMockPipelineStep("get_registry_info")
  }

  def "login to registry passes values from get_registry_info to docker.withRegistry"(){
    when: 
    1 * getPipelineMock("get_registry_info")() >> { return ["test_registry", "test_cred_id"] }
    login_to_registry{
      echo "hi"
    }
    then:
    // validate we called docker.withRegistry()
    1 * getPipelineMock("docker.withRegistry")("test_registry", "test_cred_id", _)
    // validate the closure got executed
    1 * getPipelineMock("echo")("hi")
  }

  def "login to registry passes method parameters to docker.withRegistry if provided"(){
    when: 
    1 * getPipelineMock("get_registry_info")() >> { return ["test_registry", "test_cred_id"] }
    login_to_registry("parameter_registry_url", "parameter_credential_id"){
      echo "hi"
    }
    then:
    // validate we called docker.withRegistry()
    1 * getPipelineMock("docker.withRegistry")("parameter_registry_url", "parameter_credential_id", _)
    // validate the closure got executed
    1 * getPipelineMock("echo")("hi")
  }

}
