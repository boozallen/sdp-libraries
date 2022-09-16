/*
  Copyright © 2022 Booz Allen Hamilton. All Rights Reserved.
  This software package is licensed under the Booz Allen Public License.
  The license can be found in the License file or at http://boozallen.github.io/licenses/bapl
*/

package libraries.cookiecutter.steps

void call() {
    stage("Utilities") {
        inside_sdp_image('cookiecutter:2.1.1') {
            sh "ls -alh"
            unstash 'workspace'
            sh "ls -alh"
        }