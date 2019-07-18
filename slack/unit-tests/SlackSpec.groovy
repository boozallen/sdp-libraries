/*
  Copyright © 2018 Booz Allen Hamilton. All Rights Reserved.
  This software package is licensed under the Booz Allen Public License. The license can be found in the License file or at http://boozallen.github.io/licenses/bapl
*/

package slack

import com.homeaway.devtools.jenkins.testing.JenkinsPipelineSpecification

public class SlackSpec extends JenkinsPipelineSpecification {

  def SlackTest = null
  def context = null

  def setup() {
    SlackTest = loadPipelineScriptForTest("slack/slack.groovy")
    explicitlyMockPipelineStep("echo")
    explicitlyMockPipelineVariable("CleanUp")
  }

  def "Successful Build Sends Success Result" () {
    setup:
      context = [status: "SUCCESS"]
    when:
      SlackTest.slack_cleanup(context)
    then:
      1 * getPipelineMock("slackSend")(_ as Map) >> { _arguments ->
        assert _arguments[0]["message"] =~ /Build Successful:.*/
      }
  }

  def "Failed Build Sends Fail Result" () {
    setup:
      context = [status: "FAILURE"]
    when:
      SlackTest.slack_cleanup(context)
    then:
      1 * getPipelineMock("slackSend")(_ as Map) >> { _arguments ->
        assert _arguments[0]["message"] =~ /Build Failure:.*/
      }
    }

  def "Other Builds Send No Result" () {
    setup:
      context = [status: "ILLOGICAL"]
      //explicitlyMockPipelineVariable("out") //not sure why, but this tests fails w/o this mock
    when:
      SlackTest.slack_cleanup(context)
    then:
      0 * getPipelineMock("slackSend")(_)
  }

}
