/*
  Copyright © 2018 Booz Allen Hamilton. All Rights Reserved.
  This software package is licensed under the Booz Allen Public License. The license can be found in the License file or at http://boozallen.github.io/licenses/bapl
*/

package libraries.slack 

public class SlackSpec extends JTEPipelineSpecification {

  def SlackTest = null

  def setup() {
    SlackTest = loadPipelineScriptForStep("slack","slack")
    explicitlyMockPipelineStep("echo")
  }

  def "Successful Build Sends Success Result" () {
    setup:
      SlackTest.getBinding().setVariable("currentBuild", [ result: "SUCCESS" ])
    when:
      SlackTest()
    then:
      1 * getPipelineMock("slackSend")(_ as Map) >> { _arguments ->
        assert _arguments[0]["message"] =~ /Build Successful:.*/
      }
  }

  def "Failed Build Sends Fail Result" () {
    setup:
      SlackTest.getBinding().setVariable("currentBuild", [ result: "FAILURE" ])
    when:
      SlackTest()
    then:
      1 * getPipelineMock("slackSend")(_ as Map) >> { _arguments ->
        assert _arguments[0]["message"] =~ /Build Failure:.*/
      }
    }

  def "Other Builds Send No Result" () {
    setup:
      SlackTest.getBinding().setVariable("currentBuild", [ result: "ILLOGICAL" ])
      explicitlyMockPipelineVariable("out") //not sure why, but this tests fails w/o this mock
    when:
      SlackTest()
    then:
      0 * getPipelineMock("slackSend")(_)
  }

}
