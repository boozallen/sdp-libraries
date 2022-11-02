/*
  Copyright © 2022 Booz Allen Hamilton. All Rights Reserved.
  This software package is licensed under the Booz Allen Public License.
  The license can be found in the License file or at http://boozallen.github.io/licenses/bapl
*/

package libraries.cypress.steps

void call() {

  stage("Frontend Testing (Cypress)") {
    // Required parameters
    ArrayList targetApps = config?.target_apps ?: null
    String npmScript = config?.npm_script ?: null
    String reportPath = config?.report_path ?: null

    if (null in [targetApps, npmScript, reportPath]) {
      error "Missing required parameter(s) (target_apps, npm_script, report_path)"
    }
    
    // Optional parameters
    String testRepo = config?.test_repo ?: '.'
    String branch = config?.branch ?: 'main'
    String containerImage = config?.container_image ?: 'cypress/browsers:node14.17.0-chrome91-ff89'
    
    // if test_repo isn't default ('.')
    if (testRepo != '.') {
      // make temp test directory
      // cd into new directory
      // pull down test repository
      sh """
        mkdir test_repo_dir
        cd test_repo_dir
        git clone ${testRepo} .
        git checkout ${branch}
      """
    }

    // run tests inside container
    docker.image("${containerImage}").inside {
      sh """
        npm ci
        $(npm bin)/cypress verify
        ${npmScript}
      """
    }

    // archive report(s)
    archiveArtifacts artifacts: "${reportPath}", allowEmptyArchive: true
  }
}