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
      1 * getPipelineMock("error")("SDP Image Config not defined in Pipeline Config") >> {throw new DummyException("Bad Token")}
  }

  def "If no value for config.images.registry, throw error" () {
    setup:
      InsideSdpImage.getBinding().setVariable("config", [images: [repository: "repotest", cred: "credtest", docker_args: "argstest"]])
    when:
      try {
        InsideSdpImage("test-image", {echo 'testing 123'})
      } catch(DummyException e) {}
    then:
      1 * getPipelineMock("error")("SDP Image Registry not defined in Pipeline Config") >> {throw new DummyException("Bad Token")}
  }

  def "If no value for config.images.repository, default to \"sdp\"" () {
    setup:
      InsideSdpImage.getBinding().setVariable("config", [images: [registry: "testregistry", cred: "credtest", docker_args: "argstest"]])
    when:
      try {
        InsideSdpImage("test-image", {echo 'testing 123'})
      } catch(DummyException e) {}
    then:
      1 * getPipelineMock("docker.image")("sdp/test-image")

  }

  def "If no value for config.images.cred, throw error" () {

  }

  def "If no value for config.images.docker_args, default to empty string" () {

  }

  def "Login to Docker registry specified in the pipeline config" () {

  }

  def "Ensure the image is run w/ the given docker args" () {

  }

  def "Ensure the closure's resolveStrategy is set to DELEGATE_FIRST" () {

  }

  def "Ensure that the closure's delegate is set to the current script" () {

  }

  def "Execute the given closure within the given image" () {

  }
}
