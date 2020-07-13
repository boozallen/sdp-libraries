/*
  Copyright © 2018 Booz Allen Hamilton. All Rights Reserved.
  This software package is licensed under the Booz Allen Public License. The license can be found in the License file or at http://boozallen.github.io/licenses/bapl
*/

/*
  glConnection = Name assigned to the GitLab connection set when configuring Jenkins.
  jobName = Name of the pipeline job triggered in Gitlab. Typically "<jenkins-service-account>/branch"
  jobStatus = Status of job being performed. Accepts the following: "pending", "running", "canceled", "failed", "success"
*/

def call(){
    glConnection = config.glConnection
    jobName = config.jobName
    jobStatus = config.jobStatus
    call(glConnection, jobName, jobStatus)
}

def call(String jobStatus){
    glConnection = config.glConnection
    jobName = config.jobName
    call(glConnection, jobName, jobStatus)
}

def call(String glConnection, String jobStatus){
    jobName = config.jobName
    call(glConnection, jobName, jobStatus)
}

def call(String glConnection, String jobName, String jobStatus){
    properties([gitLabConnection(glConnection)])
    updateGitlabCommitStatus name: jobName , state: jobStatus
}
