/*
  Copyright © 2018 Booz Allen Hamilton. All Rights Reserved.
  This software package is licensed under the Booz Allen Public License. The license can be found in the License file or at http://boozallen.github.io/licenses/bapl
*/

package libraries.owasp_dep_check

public class ApplicationDependencyScanSpec extends JTEPipelineSpecification {
  def ApplicationDependencyScan = null

  String fileDoesNotExistWarning = "\"dependency-check-suppression.xml\" does not exist. Skipping suppression."

  String commandBeginning = "mkdir -p owasp-dependency-check && mkdir -p owasp-data && /usr/share/dependency-check/bin/dependency-check.sh"
  String defaultArgs = "--out owasp-dependency-check --enableExperimental --format ALL -s ."
  String expectedAdditionalArgs = ""
  String commandEnd = "-d owasp-data"

  def setup() {
    ApplicationDependencyScan = loadPipelineScriptForStep("owasp_dep_check", "application_dependency_scan")

    ApplicationDependencyScan.getBinding().setVariable("config", [:])
    
    explicitlyMockPipelineStep("inside_sdp_image")
  }

  def "Does not print warning message if the suppression file is found" () {
    setup:
      getPipelineMock("fileExists")(_) >> { return true }
    when:
      ApplicationDependencyScan()
    then:
      0 * getPipelineMock("echo")(fileDoesNotExistWarning)
  }

  def "Prints warning message if the suppression file is not found" () {
    setup:
      getPipelineMock("fileExists")(_) >> { return false }
    when:
      ApplicationDependencyScan()
    then:
      1 * getPipelineMock("echo")(fileDoesNotExistWarning)
  }

  def "Uses --suppression flag when using suppression file" () {
    setup:
      getPipelineMock("fileExists")(_) >> { return true }
      expectedAdditionalArgs = " --suppression dependency-check-suppression.xml"
    when:
      ApplicationDependencyScan()
    then:
      1 * getPipelineMock("sh")("${commandBeginning} ${defaultArgs}${expectedAdditionalArgs} ${commandEnd}")
  }

  def "Does not use --supppression flag when not using suppression file" () {
    setup:
      getPipelineMock("fileExists")(_) >> { return false }
      expectedAdditionalArgs = ""
    when:
      ApplicationDependencyScan()
    then:
      1 * getPipelineMock("sh")("${commandBeginning} ${defaultArgs}${expectedAdditionalArgs} ${commandEnd}")
  }
}
