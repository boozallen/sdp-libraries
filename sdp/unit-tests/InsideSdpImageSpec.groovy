/*
  Copyright © 2018 Booz Allen Hamilton. All Rights Reserved.
  This software package is licensed under the Booz Allen Public License. The license can be found in the License file or at http://boozallen.github.io/licenses/bapl
*/

import com.homeaway.devtools.jenkins.testing.JenkinsPipelineSpecification

public class InsideSdpImageSpec extends JenkinsPipelineSpecification {

  def InsideSdpImage = null

  public static class DummyException extends RuntimeException {
		public DummyException(String _message) { super( _message ); }
	}

  def setup() {
    InsideSdpImage = loadPipelineScriptForTest("./sdp/inside_sdp_image.groovy")
  }

  def "If no value for config.images, throw error" () {
    setup:
      InsideSdpImage.getBinding().setVariable("config", [images: null])
    when:
      try {
        InsideSdpImage("test-image", {echo 'testing 123'})
      } catch(DummyException e) {}
    then:
      1 * getPipelineMock("error")("SDP Image Config not defined in Pipeline Config") >> {throw new DummyException("images error")}
  }

  def "If no value for config.images.registry, throw error" () {
    setup:
      InsideSdpImage.getBinding().setVariable("config", [images: [repository: "repotest", cred: "testcred", docker_args: "testargs"]])
    when:
      try {
        InsideSdpImage("test-image", {echo 'testing 123'})
      } catch(DummyException e) {}
    then:
      1 * getPipelineMock("error")("SDP Image Registry not defined in Pipeline Config") >> {throw new DummyException("images.registry error")}
  }

  def "If no value for config.images.repository, default to \"sdp\"" () {
    setup:
      InsideSdpImage.getBinding().setVariable("config", [images: [registry: "testregistry", cred: "testcred", docker_args: "testargs"]])
    when:
      try {
        InsideSdpImage("test-image", {echo 'testing 123'})
      } catch(DummyException e) {}
    then:
      1 * getPipelineMock("docker.image")("sdp/test-image") >> explicitlyMockPipelineVariable("Image")

  }

  def "If no value for config.images.cred, throw error" () {
    setup:
      InsideSdpImage.getBinding().setVariable("config", [images: [registry: "testregistry", repository: "testrepo", docker_args: "testargs"]])
    when:
      try {
        InsideSdpImage("test-image", {echo 'testing 123'})
      } catch(DummyException e) {}
    then:
      1 * getPipelineMock("error")("SDP Image Repository Credential not defined in Pipeline Config") >> {throw new DummyException("images.cred error")}
  }

  def "If no value for config.images.docker_args, default to empty string" () {
    setup:
      InsideSdpImage.getBinding().setVariable("config", [images: [registry: "testregistry", repository: "restrepo", cred: "testcred"]])
    when:
      try {
        InsideSdpImage("test-image", {echo 'testing 123'})
      } catch(DummyException e) {}
    then:
      _ * getPipelineMock("docker.image")(_) >> explicitlyMockPipelineVariable("Image")
      1 * getPipelineMock("Image.inside")("", _ as Closure)
  }

  def "Login to the Docker registry specified in the pipeline config" () {
    setup:
      InsideSdpImage.getBinding().setVariable("config", [images: [registry: "testregistry", repository: "restrepo", cred: "testcred", docker_args: "testargs"]])
      getPipelineMock("docker.image")(_) >> explicitlyMockPipelineVariable("Image")
    when:
      InsideSdpImage("test-image", {echo 'testing 123'})
    then:
      1 * getPipelineMock("docker.withRegistry")("testregistry", "testcred", _ as Closure)
  }

  def "Ensure the image is run w/ the given docker args" () {
    setup:
      InsideSdpImage.getBinding().setVariable("config", [images: [registry: "testregistry", repository: "restrepo", cred: "testcred", docker_args: "testargs"]])
      getPipelineMock("docker.image")(_) >> explicitlyMockPipelineVariable("Image")
    when:
      InsideSdpImage("test-image", {echo 'testing 123'})
    then:
      1 * getPipelineMock("Image.inside")("testargs", _ as Closure)
  }

  def "Ensure the closure's resolveStrategy is set to DELEGATE_FIRST" () {
    setup:
      InsideSdpImage.getBinding().setVariable("config", [images: [registry: "testregistry", repository: "restrepo", cred: "testcred", docker_args: "testargs"]])
      getPipelineMock("docker.image")(_) >> explicitlyMockPipelineVariable("Image")
      def body = {echo 'testing 123'}
    when:
      InsideSdpImage("test-image", body)
    then:
      body.resolveStrategy == Closure.DELEGATE_FIRST
  }

  def "Ensure that the closure's delegate is set to the current script" () {
    setup:
      InsideSdpImage.getBinding().setVariable("config", [images: [registry: "testregistry", repository: "restrepo", cred: "testcred", docker_args: "testargs"]])
      getPipelineMock("docker.image")(_) >> explicitlyMockPipelineVariable("Image")
      def body = {echo 'testing 123'}
    when:
      InsideSdpImage("test-image", body)
    then:
      body.delegate == InsideSdpImage
  }

  def "Execute the given closure within the given image" () {
    setup:
      InsideSdpImage.getBinding().setVariable("config", [images: [registry: "testregistry", repository: "restrepo", cred: "testcred", docker_args: "testargs"]])
      getPipelineMock("docker.image")(_) >> explicitlyMockPipelineVariable("Image")
      def body = {echo 'testing 123'}
    when:
      InsideSdpImage("test-image", body)
    then:
      1 * getPipelineMock('echo')('testing 123')
  }
}
