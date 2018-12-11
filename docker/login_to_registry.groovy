/*
  Copyright © 2018 Booz Allen Hamilton. All Rights Reserved.
  This software package is licensed under the Booz Allen Public License. The license can be found in the License file or at http://boozallen.github.io/licenses/bapl
*/

void call(){
  def image_repo = pipelineConfig?.application_image_repository ?:
                   {error "application_image_repository not defined in pipeline config"}()

  def image_repo_cred = pipelineConfig?.application_image_repository_credential ?:
                        {error "application_image_repository_credential not defined in pipeline config"}()

  withCredentials([usernamePassword(credentialsId: image_repo_cred, passwordVariable: 'pass', usernameVariable: 'user')]) {
    sh "echo ${pass} | docker login -u ${user} --password-stdin ${image_repo}"
  }
}
