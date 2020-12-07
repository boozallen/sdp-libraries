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

  def gitlab = [:] + (config.gitlab)

  glConnection = glConnection ?: gitlab.connection ?: 
    { error "gitlab connection must be a valid string" } 

  jobName = jobName ?: gitlab.job_name ?: 
    { error "gitlab job name must be a valid string" }

  jobStatus = jobStatus ?: gitlab.job_status ?: 
    { error "gitlab job status must be a valid string" }   

  properties([gitLabConnection(glConnection)])
  updateGitlabCommitStatus name: jobName , state: jobStatus

}
