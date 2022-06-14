/*
  Copyright © 2018 Booz Allen Hamilton. All Rights Reserved.
  This software package is licensed under the Booz Allen Public License. The license can be found in the License file or at http://boozallen.github.io/licenses/bapl
*/

package libraries.owasp_dep_check

public class ApplicationDependencyScanSpec extends JTEPipelineSpecification {
  def ApplicationDependencyScan = null

  String fileDoesNotExistWarning = "\"dependency-check-suppression.xml\" does not exist. Skipping suppression."

  def setup() {
    ApplicationDependencyScan = loadPipelineScriptForStep("owasp_dep_check", "application_dependency_scan")

    ApplicationDependencyScan.getBinding().setVariable("config", [:])
    
    explicitlyMockPipelineStep("inside_sdp_image")
    explicitlyMockPipelineVariable("out")
  }

  def "Prints warning message if the suppression file is not found" () {
    setup:
      getPipelineMock("fileExists")() >> false
    when:
      ApplicationDependencyScan()
    then:
      1 * getPipelineMock("echo")(fileDoesNotExistWarning)
  }

  def "Does not print warning message if the suppression file is found" () {
    setup:
      getPipelineMock("fileExists")() >> true
    when:
      ApplicationDependencyScan()
    then:
      0 * getPipelineMock("echo")(fileDoesNotExistWarning)
  }
}
