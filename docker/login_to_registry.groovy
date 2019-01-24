/*
  Copyright © 2018 Booz Allen Hamilton. All Rights Reserved.
  This software package is licensed under the Booz Allen Public License. The license can be found in the License file or at http://boozallen.github.io/licenses/bapl
*/

void call(){
  def cfg = get_repo_info()
  def image_repo = cfg.repo
  def image_repo_cred = cfg.cred

  withCredentials([usernamePassword(credentialsId: image_repo_cred, passwordVariable: 'pass', usernameVariable: 'user')]) {
    sh "echo ${pass} | docker login -u ${user} --password-stdin ${image_repo}"
  }
}
