/*
  Copyright © 2020 Booz Allen Hamilton. All Rights Reserved.
  This software package is licensed under the Booz Allen Public License. The license can be found in the License file or at http://boozallen.github.io/licenses/bapl
*/

//report build result
@CleanUp
def slack_cleanup(context){
  switch(currentBuild.result){
      case null: // no result set yet means success
      case "SUCCESS":
        conditional_slack_send("good", "Build Successful: ${env.JOB_URL}", "success")
        break;
      case "FAILURE":
        conditional_slack_send("danger", "Build Failure: ${env.JOB_URL}", "failure")
        break;
      case "UNSTABLE":
        conditional_slack_send("warning", "Build Unstable: ${env.JOB_URL}", "failure")
      default:
        echo "Slack Notifier doing nothing: ${currentBuild.result}"
  }
}

//alert on successful deployment
@AfterStep({ context.step.equals("deploy_to") && currentBuild.result in [null, "SUCCESS"] })
def slack_report_deployment(context){
  conditional_slack_send('#0000ff', "Deployment Successful: ${env.JOB_URL}", "status")
}

/*
  Lets you control which messages to send to slack using the pipeline config
  color: The color of the message sent to slack. either a hex color code ("#000000") or one of "good", "warning", or "danger"
  message: The slack message content
  message_type: if this string is in the config's "notify_on" list, send the message
*/
def conditional_slack_send(String color, String message, String message_type){
  def notify_on = config.notify_on instanceof List ? config.notify_on : { println "no valid notify_on config option found; using the default"; return ["success", "failure"] }()
  notify_on = notify_on.collect{ it instanceof String ? it.toLowerCase() : it }
  
  if ( message_type in notify_on ) {
    // ref: https://www.jenkins.io/doc/pipeline/steps/slack/
    slackSend color: color, message: message
  }
}