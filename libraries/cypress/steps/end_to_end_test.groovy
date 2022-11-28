/*
  Copyright © 2022 Booz Allen Hamilton. All Rights Reserved.
  This software package is licensed under the Booz Allen Public License.
  The license can be found in the License file or at http://boozallen.github.io/licenses/bapl
*/

package libraries.cypress.steps

void call() {

  stage("End-to-End Testing (Cypress)") {
    // Required parameters
    String npmScript = config?.npm_script ?: null
    String reportPath = config?.report_path ?: null

    if (null in [npmScript, reportPath]) {
      error "Missing required parameter(s) (npm_script, report_path)"
    }
    
    // Optional parameters
    String testRepo = config?.test_repo ?: '.'
    String testRepoCreds = config?.test_repo_creds ?: ''
    String branch = config?.branch ?: 'main'
    String containerImage = config?.container_image ?: 'cypress/browsers:node14.17.0-chrome91-ff89'
    String containerRegistry = config?.container_registry ?: 'https://index.docker.io/v1/'
    String containerRegistryCreds = config?.container_registry_creds ?: ''
    
    unstash "workspace"

    // if test_repo isn't default ('.')
    if (testRepo != '.') {
      // make temp test directory
      // cd into new directory
      // pull down test repository
      String prepTestRepo = """
        mkdir test_repo_dir
        cd test_repo_dir
        git clone ${testRepo} .
        git checkout ${branch}
      """
      if (testRepoCreds != '') {
        withCredentials([usernamePassword(credentialsId: testRepoCreds, passwordVariable: 'PASS', usernameVariable: 'USER')]) {
          sh prepTestRepo.replaceFirst("://","://${USER}:${PASS}@")
        }
      }
      else {
        sh prepTestRepo
      }
      //update reportPath
      reportPath = "test_repo_dir/" + reportPath
    }

    // run tests inside container
    docker.withRegistry("${containerRegistry}", "${containerRegistryCreds}") {
      docker.image("${containerImage}").inside {
        String runTests = """
          npm ci
          \$(npm bin)/cypress verify
          ${npmScript}
        """
        if (testRepoCreds != '') {
          dir('test_repo_dir') {
            sh runTests
          }
        }
        else {
          sh runTests
        }
      }
    }

    // archive report(s)
    archiveArtifacts artifacts: "${reportPath}", allowEmptyArchive: true
  }
}
