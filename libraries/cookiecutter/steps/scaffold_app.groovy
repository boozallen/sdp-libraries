/*
  Copyright © 2022 Booz Allen Hamilton. All Rights Reserved.
  This software package is licensed under the Booz Allen Public License.
  The license can be found in the License file or at http://boozallen.github.io/licenses/bapl
*/

package libraries.cookiecutter.steps

void call() {
  stage("Scaffold App (cookiecutter)") {
    String cookiecutterImage = config?.cookiecutter_image ?: "cookiecutter:2.1.1"
    String templatePath = config?.template_path ?: null //TEMPLATE
    String scmPull = config?.scm_url ?: null //TEMPLATE
    String scmCred = config?.scm_cred ?: null //creds to pull cc template from scm
    String checkout = config?.checkout ?: null //OPTION
    String cookiecutterDir = config?.cookiecutter_directory ?: null //OPTION
    String outDir = config?.output_directory ?: null //OPTION
    String cookieCutterJson = config?.cookie_cutter_json ?: null //Overwrite cookiecutter.json with this file
    String projectFolder = config?.project_folder ?: null
    String ARGS = "" //Final Command
    String extraContextARGS = ""
    Boolean noInput = config?.no_input ?: true //OPTION
    Boolean debugOn = config?.verbose ?: false //OPTION
    Boolean overwriteWorkspace = config?.overwrite_workspace ?: false
    Boolean shouldFail = false
    ArrayList extraContext = config?.extra_context ?: [] //EXTRA_CONTEXT

    if (checkout) {
      ARGS += "--checkout ${checkout} "
    }

    if (cookiecutterDir) {
      ARGS += "--directory ${cookiecutterDir} "
    }

    if (outDir) {
      ARGS += "--output-dir ${outDir} "
    }

    if (noInput) {
      ARGS += "--no-input "
    }

    if (debugOn) {
      ARGS += "--verbose "
    }

    if (!extraContext.isEmpty()) {
      extraContext.each {
        val -> extraContextARGS += "${val} "
      }
    }


    //cookiecutter [OPTIONS] [TEMPLATE] [EXTRA_CONTEXT]...
    inside_sdp_image(cookiecutterImage) {
      try {
        if (templatePath) {
          unstash 'workspace'
          cleanWs()
          ARGS += templatePath

          if (cookieCutterJson) {
            sh "cp -f ${cookieCutterJson} ./cookiecutter.json"
          }

          sh "cookiecutter ${ARGS} ${extraContextARGS}"
        }
        else if (scmPull) {
          if (scmCred) {
            withCredentials([string(credentialsId: "${scmCred}", variable: 'PAT')]) {
              scmPull = scmPull.replaceFirst("://","://${PAT}@")
              echo "${scmPull}"
              ARGS += scmPull

              sh "cookiecutter ${ARGS} ${extraContextARGS}"
            }
          }
          else {
            ARGS += scmPull

            sh "cookiecutter ${ARGS} ${extraContextARGS}"
          }
        }
        else {
          sh "cookiecutter ${ARGS} ${extraContextARGS}"
        }
      }
      catch (Exception err) {
        shouldFail = true
        echo "Scaffold App Stage Failed: {$err}"
      }
      finally {
        echo "executing finally block"
        sh 'ls -alh'
        if (overwriteWorkspace) {
          echo " overwriting WS"
          if (outDir) {
            dir("${outDir}" + "${projectFolder}") {
              echo "in if"
              stash name: 'workspace', allowEmpty: true, useDefaultExcludes: false
            }
          }
          else {
            echo "in else"
            dir("${projectFolder}") {
              sh 'ls -alh'
              stash name: 'workspace', allowEmpty: true, useDefaultExcludes: false
            }
          }
        }
      }
      sh 'ls -alh'
    }
  }
}
