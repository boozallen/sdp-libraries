/*
  Copyright © 2018 Booz Allen Hamilton. All Rights Reserved.
  This software package is licensed under the Booz Allen Public License. The license can be found in the License file or at http://boozallen.github.io/licenses/bapl
*/

/*
  glConnection = Name assigned to the GitLab connection set when configuring Jenkins.
  jobName = Name of the pipeline job triggered in Gitlab. Typically "<jenkins-service-account>/branch"
  jobStatus = Status of job being performed. Accepts the following: "pending", "running", "canceled", "failed", "success"
*/

def call(String glConnection, String jobName, String jobStatus){

  if (glConnection == "" && config.glConnection != ""){
    glConnection = config.glConnection
  }
  else if (glConnection instanceof String){

  }
  else{
    error "glConnection must be a string and not empty"
  }

  if (jobName == "" && config.jobName != ""){
    jobName = config.jobName
  }
  else if (jobName instanceof String){

  }
  else{
    error "jobName must be a string and not empty"
  }

  if (jobStatus == "" && config.jobStatus != ""){
    jobStatus = config.jobStatus
  }
  else if (jobStatus instanceof String){

  }
  else{
    error "jobStatus must be a string and not empty"
  }

  properties([gitLabConnection(glConnection)])
  updateGitlabCommitStatus name: jobName , state: jobStatus

}
