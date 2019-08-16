package docker

import com.homeaway.devtools.jenkins.testing.JenkinsPipelineSpecification

public class DockerComposeBuildSpec extends JenkinsPipelineSpecification {

  def DockerComposeBuild = null

  public static class DummyException extends RuntimeException {
    public DummyException(String _message) { super( _message ); }
  }

  def setup() {
    DockerComposeBuild = loadPipelineScriptForTest("docker/docker_compose_build.groovy")
  }

// TODO: Add Unit Tests
  def "docker-compose command is called" () {
    setup:
      DockerComposeBuild.getBinding().setVariable("config", [registry: "reg"])
    when:
     DockerComposeBuild
    then:
      1 * getPipelineMock("sh")([script: "docker-compose -f docker-compose.yml build", returnStdout: true])
  }
}
