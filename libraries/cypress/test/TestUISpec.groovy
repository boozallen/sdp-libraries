/*
  Copyright © 2022 Booz Allen Hamilton. All Rights Reserved.
  This software package is licensed under the Booz Allen Public License.
  The license can be found in the License file or at http://boozallen.github.io/licenses/bapl
*/

package libraries.cypress

public class TestUISpec extends JTEPipelineSpecification {
  def TestUI = null

  static class DummyException extends RuntimeException {
    DummyException(String _message) { super( _message ) }
  }

  def setup() {
    LinkedHashMap env = [:]
    LinkedHashMap config = [:]

    TestUI = loadPipelineScriptForStep("cypress", "test_ui")

    TestUI.getBinding().setVariable("env", env)
    TestUI.getBinding().setProperty("config", config)
  }

  def "Fails when npm_script is missing" () {
    setup:
      TestUI.getBinding().setVariable("config", [report_path: "cypress/report/path"])
      getPipelineMock("docker.image")(_) >> explicitlyMockPipelineVariable("Image")
    when:
      try {
        TestUI()
      } catch(DummyException e) {}
    then:
      1 * getPipelineMock("error")("Missing required parameter(s) (npm_script, report_path)")
  }

  def "Fails when report_path is missing" () {
    setup:
      TestUI.getBinding().setVariable("config", [npm_script: "npm run something"])
      getPipelineMock("docker.image")(_) >> explicitlyMockPipelineVariable("Image")
    when:
      try {
        TestUI()
      } catch(DummyException e) {}
    then:
      1 * getPipelineMock("error")("Missing required parameter(s) (npm_script, report_path)")
  }

  def "External test repository steps work" () {
    setup:
      TestUI.getBinding().setVariable("config", [npm_script: "npm run something", report_path: "cypress/report/path", test_repo: "https://github.com/boozallen/sdp-libraries", branch: "develop"])
      getPipelineMock("docker.image")(_) >> explicitlyMockPipelineVariable("Image")
    when:
      try {
        TestUI()
      } catch(DummyException e) {}
    then:
      1 * getPipelineMock("sh")({it =~ / git clone https:\/\/github.com\/boozallen\/sdp-libraries /})
  }

  def "Cypress test step runs" () {
    setup:
      TestUI.getBinding().setVariable("config", [npm_script: "npm run something", report_path: "cypress/report/path"])
    when:
      TestUI()
    then:
      1 * getPipelineMock("docker.image")(_) >> explicitlyMockPipelineVariable("Image")
      1 * getPipelineMock("Image.inside")(_ as Closure)
      1 * getPipelineMock("sh")({it =~ /npm run something/})
  }

  def "Test artifacts are archived in Jenkins" () {
    setup:
      TestUI.getBinding().setVariable("config", [npm_script: "npm run something", report_path: "cypress/report/path"])
      getPipelineMock("docker.image")(_) >> explicitlyMockPipelineVariable("Image")
    when:
      try {
        TestUI()
      } catch(DummyException e) {}
    then:
      1 * getPipelineMock("archiveArtifacts.call")([artifacts: "cypress/report/path", allowEmptyArchive: true ])
  }
}