/*
  Copyright © 2018 Booz Allen Hamilton. All Rights Reserved.
  This software package is licensed under the Booz Allen Public License. The license can be found in the License file or at http://boozallen.github.io/licenses/bapl
*/

package sdp

import com.homeaway.devtools.jenkins.testing.JenkinsPipelineSpecification

public class NodeSpec extends JenkinsPipelineSpecification {

  def Node = null

  def testConfig = [:]

  public static class DummyException extends RuntimeException {
		public DummyException(String _message) { super( _message ); }
	}

  def setup() {
    Node = loadPipelineScriptForTest("./sdp/node.groovy")
  }

/*****************************************************************************************************/

  def "If no value for config.images, throw error" () {
    setup:
      Node.getBinding().setVariable("config", [images: null])
    when:
      try {
        Node("test-image", {echo 'testing 123'})
      } catch(DummyException e) {}
    then:
      1 * getPipelineMock("error")("SDP Image Config not defined in Pipeline Config") >> {throw new DummyException("images error")}
  }


/*****************************************************************************************************/

  def "If no value for config.images.registry, throw error" () {
    setup:
      Node.getBinding().setVariable("config", [images: [repository: "repotest", cred: "testcred", docker_args: "testargs"]])
    when:
      try {
        Node("test-image", {echo 'testing 123'})
      } catch(DummyException e) {}
    then:
      1 * getPipelineMock("error")("SDP Image Registry not defined in Pipeline Config") >> {throw new DummyException("images.registry error")}
  }

/*****************************************************************************************************/

  def "If no value for config.images.repository, default to \"sdp\"" () {
    setup:
      Node.getBinding().setVariable("config", [images: [registry: "testregistry", cred: "testcred", docker_args: "testargs"]])
    when:
      try {
        Node("test-image", {echo 'testing 123'})
      } catch(DummyException e) {}
    then:
      1 * getPipelineMock("docker.image")("sdp/test-image") >> explicitlyMockPipelineVariable("Image")

  }

/*****************************************************************************************************/

  def "If no value for config.images.cred, throw error" () {
    setup:
      Node.getBinding().setVariable("config", [images: [registry: "testregistry", repository: "testrepo", docker_args: "testargs"]])
    when:
      try {
        Node("test-image", {echo 'testing 123'})
      } catch(DummyException e) {}
    then:
      1 * getPipelineMock("error")("SDP Image Repository Credential not defined in Pipeline Config") >> {throw new DummyException("images.cred error")}
  }

/*****************************************************************************************************/

  def "If no value for config.images.docker_args, default to empty string" () {
    setup:
      Node.getBinding().setVariable("config", [agentType: "docker",images: [registry: "testregistry", repository: "testrepo", cred: "testcred"]])
    when:
      try {
        Node("test-image", {echo 'testing 123'})
      } catch(DummyException e) {}
    then:
      _ * getPipelineMock("docker.image")(_) >> explicitlyMockPipelineVariable("Image")
      1 * getPipelineMock("Image.inside")("", _ as Closure)
  }

/*****************************************************************************************************/

  def "Login to the Docker registry specified in the pipeline config" () {
    setup:
      Node.getBinding().setVariable("config", [images: [registry: "testregistry", repository: "restrepo", cred: "testcred", docker_args: "testargs"]])
      getPipelineMock("docker.image")(_) >> explicitlyMockPipelineVariable("Image")
    when:
      Node("test-image", {echo 'testing 123'})
    then:
      1 * getPipelineMock("docker.withRegistry")("testregistry", "testcred", _ as Closure)
  }

/*****************************************************************************************************/

  def "Ensure the image is run w/ the given docker args" () {
    setup:
      Node.getBinding().setVariable("config", [images: [registry: "testregistry", repository: "restrepo", cred: "testcred", docker_args: "testargs"]])
      getPipelineMock("docker.image")(_) >> explicitlyMockPipelineVariable("Image")
    when:
      Node("test-image", {echo 'testing 123'})
    then:
      1 * getPipelineMock("Image.inside")("testargs", _ as Closure)
  }

/*****************************************************************************************************/

  def "Ensure the closure's resolveStrategy is set to OWNER_FIRST, the default" () {
    setup:
      Node.getBinding().setVariable("config", [images: [registry: "testregistry", repository: "restrepo", cred: "testcred", docker_args: "testargs"]])
      getPipelineMock("docker.image")(_) >> explicitlyMockPipelineVariable("Image")
      def body = {echo 'testing 123'}
    when:
      Node("test-image", body)
    then:
      body.resolveStrategy == Closure.OWNER_FIRST
  }

/*****************************************************************************************************/

  def "Execute the given closure within the given image" () {
    setup:
      Node.getBinding().setVariable("config", [images: [registry: "testregistry", repository: "restrepo", cred: "testcred", docker_args: "testargs"]])
      getPipelineMock("docker.image")(_) >> explicitlyMockPipelineVariable("Image")
      def body = {echo 'testing 123'}
    when:
      Node("test-image", body)
    then:
      1 * getPipelineMock('echo')('testing 123')
  }

/*****************************************************************************************************/

  def "Ensure outer call config is used instead of sdp config" () {
    setup:
    def sdpConfig = [images: [registry: "testregistry", repository: "restrepo", cred: "testcred", docker_args: "testargs"]]
    Node.getBinding().setVariable("config", sdpConfig )
    getPipelineMock("docker.image")(_) >> explicitlyMockPipelineVariable("Image")
    def contextConfig = null
    def body = { echo 'testing 123'; contextConfig = config}
    def outer = { Node.call("test-image", body) }


    when:
    outer()
    then:
    1 * getPipelineMock('echo')('testing 123')
    contextConfig == testConfig
    contextConfig != sdpConfig
  }

/*****************************************************************************************************/

  def getConfig(){
    return testConfig
  }
}
