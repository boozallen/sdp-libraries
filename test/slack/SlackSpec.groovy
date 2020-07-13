/*
  Copyright © 2018 Booz Allen Hamilton. All Rights Reserved.
  This software package is licensed under the Booz Allen Public License. The license can be found in the License file or at http://boozallen.github.io/licenses/bapl
*/

public class SlackSpec extends JTEPipelineSpecification {

  def Slack = null

  def setup() {
    Slack = loadPipelineScriptForTest("slack/slack.groovy")
    explicitlyMockPipelineStep("echo")
  }
  
  /************************
  ***** slack_cleanup *****
  ************************/

  def "Successful Build Sends Success Result" () {
    setup:
      def context = [:]
      Slack.getBinding().setVariable("currentBuild", [result: "SUCCESS"])
      Slack.getBinding().setVariable("config", [:])
    when:
      Slack.slack_cleanup(context)
    then:
      1 * getPipelineMock("slackSend")(_ as Map) >> { _arguments ->
        assert _arguments[0]["message"] =~ /Build Successful:.*/
      }
  }

  def "Failed Build Sends Fail Result" () {
    setup:
      def context = [:]
      Slack.getBinding().setVariable("currentBuild", [result: "FAILURE"])
      Slack.getBinding().setVariable("config", [:])
    when:
      Slack.slack_cleanup(context)
    then:
      1 * getPipelineMock("slackSend")(_ as Map) >> { _arguments ->
        assert _arguments[0]["message"] =~ /Build Failure:.*/
      }
    }
    
  def "Unstable Build Sends Warning Result" () {
    setup:
      def context = [:]
      Slack.getBinding().setVariable("currentBuild", [result: "UNSTABLE"])
      Slack.getBinding().setVariable("config", [:])
    when:
      Slack.slack_cleanup(context)
    then:
      1 * getPipelineMock("slackSend")(_ as Map) >> { _arguments ->
        assert _arguments[0]["message"] =~ /Build Unstable:.*/
      }
  }
  
  def "Null Build Result Sends Success" () {
    setup:
      def context = [:]
      Slack.getBinding().setVariable("currentBuild", [result: null])
      Slack.getBinding().setVariable("config", [:])
    when:
      Slack.slack_cleanup(context)
    then:
      1 * getPipelineMock("slackSend")(_ as Map) >> { _arguments ->
        assert _arguments[0]["message"] =~ /Build Successful:.*/
      }
  }

  def "Other Builds Send No Result" () {
    setup:
      def context = [:]
      Slack.getBinding().setVariable("currentBuild", [result: "ILLOGICAL"])
      Slack.getBinding().setVariable("config", [:])
      explicitlyMockPipelineVariable("out") //not sure why, but this tests fails w/o this mock
    when:
      Slack.slack_cleanup(context)
    then:
      0 * getPipelineMock("slackSend")(_)
  }
  
  /**********************************
  ***** slack_report_deployment *****
  **********************************/
  
  // Since the step annotations don't appear to check the context (or, at least, their closures always return true),
  // it is assumed here that they and their closures work as expected
  
  def "Successful deploy_to step Sends message to Slack" () {
    setup:
      def context = [:]
    //  Slack.getBinding().setVariable("currentBuild", [result: null])
      Slack.getBinding().setVariable("config", [notify_on: ["success", "failure", "status"]])
    when:
      Slack.slack_report_deployment(context)
    then:
      1 * getPipelineMock("slackSend")(_ as Map) >> { _arguments ->
        assert _arguments[0]["message"] =~ /Deployment Successful:.*/
      }
  }
  
  def "Don't send message after deploy_to if notify_on doesn't contain \"status\"" () {
    setup:
      def context = [:]
    //  Slack.getBinding().setVariable("currentBuild", [result: null])
      Slack.getBinding().setVariable("config", [notify_on: ["success", "failure"]])
    when:
      Slack.slack_report_deployment(context)
    then:
      0 * getPipelineMock("slackSend")(_)
  }
  
  
  /*********************************
  ***** conditional_slack_send *****
  *********************************/
  
  def "Only send a slack message if the message_type is in notify_on" () {
    setup:
      Slack.getBinding().setVariable("config", [notify_on: ["my_message_type"]])
    when:
      Slack.conditional_slack_send("#000000", "test_message", message_type)
    then:
      i * getPipelineMock("slackSend")([color: "#000000", message: "test_message"])
    where:
      i | message_type
      0 | "success"
      1 | "my_message_type"
  }
  
  def "Send a message to slack with the appropriate message & color" () {
    setup:
      Slack.getBinding().setVariable("config", [notify_on: ["my_message_type"]])
    when:
      Slack.conditional_slack_send(color, message, "my_message_type")
    then:
      1 * getPipelineMock("slackSend")([color: color, message: message])
    where:
       color     | message
       "#000000" | "test_message"
       "#ABCDEF" | "TEST_MESSAGE" 
       "danger"  | "danger"
  }
  
  def "Use config.notify_on for notify_on variable when available" () {
    setup:
      Slack.getBinding().setVariable("config", [notify_on: ["my_message_type"]])
    when:
      Slack.conditional_slack_send("#000000", "test_message", "my_message_type")
    then:
      1 * getPipelineMock("slackSend")([color: "#000000", message: "test_message"])
  }
  
  def "Default to notifying on \"success\" and \"failure\" when no config.notify_on is available" () {
    setup:
      Slack.getBinding().setVariable("config", [notify_on: null])
    when:
      Slack.conditional_slack_send("#000000", "test_message", message_type)
    then:
      i * getPipelineMock("slackSend")([color: "#000000", message: "test_message"])
    where:
      i | message_type
      1 | "success"
      1 | "failure"
      0 | "notify"
      0 | "my_message_type"
  }
  
  def "Only use config.notify_on if it's a list" () {
    setup:
      Slack.getBinding().setVariable("config", [notify_on: "notify"])
    when:
      Slack.conditional_slack_send("#000000", "test_message", message_type)
    then:
      i * getPipelineMock("slackSend")([color: "#000000", message: "test_message"])
    where:
      i | message_type
      1 | "success"
      1 | "failure"
      0 | "notify"
      0 | "my_message_type"
  }
  
  def "Make all elements in config.notify_on lowercase" () {
    setup:
      Slack.getBinding().setVariable("config", [notify_on: ["SUCCESS", "FaIlUrE", "Notify", "MY_message_TYPE"]])
    when:
      Slack.conditional_slack_send("#000000", "test_message", message_type)
    then:
      i * getPipelineMock("slackSend")([color: "#000000", message: "test_message"])
    where:
      i | message_type
      1 | "success"
      1 | "failure"
      1 | "notify"
      1 | "my_message_type"
      0 | "unused_message_type"
  }
  
}
