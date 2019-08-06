/*
  Copyright © 2018 Booz Allen Hamilton. All Rights Reserved.
  This software package is licensed under the Booz Allen Public License. The license can be found in the License file or at http://boozallen.github.io/licenses/bapl
*/

import org.boozallen.plugins.jte.hooks.*

//Alert when job is finished
@CleanUp
def slack_cleanup(Map context){
    switch(context.status){
        case null: // no result set yet means success
        case "SUCCESS":
          slackSend color: "good", message: "Build Successful: ${env.JOB_URL}"
          break;
        case "FAILURE":
          slackSend color: '#ff0000', message: "Build Failure: ${env.JOB_URL}"
          break;
        default:
          echo "Slack Notifier doing nothing: ${context.status}"
    }
}

//Alert on successful deployment
@AfterStep
def slack_report_deployment(Map context){
  if (context.step == "deploy_to" && context.status == "SUCCESS") {
    slackSend color: '#0000ff', message: "Deployment Successful: ${env.JOB_URL}"
  }
}
