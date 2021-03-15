/*
  Copyright © 2020 Booz Allen Hamilton. All Rights Reserved.
  This software package is licensed under the Booz Allen Public License. The license can be found in the License file or at http://boozallen.github.io/licenses/bapl
*/
package libraries.git.steps

/*
  glConnection = Name assigned to the GitLab connection set when configuring Jenkins.
  jobName = Name of the pipeline job triggered in Gitlab. Typically "<jenkins-service-account>/branch"
  jobStatus = Status of job being performed. Accepts the following: "pending", "running", "canceled", "failed", "success"
*/
def call(String gl_connection = null, String gl_job_name = null, String gl_job_status = null){

  def gitlab = config.gitlab ?: [:]

  def job_connection = gl_connection ?: gitlab.connection ?: 
    { error "gitlab connection must be a valid string" }()

  def job_name = gl_job_name ?: gitlab.job_name ?: 
    { error "gitlab job name must be a valid string" }()

  def job_status = gl_job_status ?: gitlab.job_status ?: 
    { error "gitlab job status must be a valid string" }()   

  properties([gitLabConnection(job_connection)])
  updateGitlabCommitStatus name: job_name , state: job_status

}
