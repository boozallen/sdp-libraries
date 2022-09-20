/*
  Copyright © 2022 Booz Allen Hamilton. All Rights Reserved.
  This software package is licensed under the Booz Allen Public License.
  The license can be found in the License file or at http://boozallen.github.io/licenses/bapl
*/

package libraries.cookiecutter.steps

void call() {
  stage("Cookiecutter") {
    String cookiecutterImage = config?.cookiecutter_image ?: "cookiecutter:2.1.1"
    String templatePath = config?.template_path ?: null //TEMPLATE
    String scmPull = config?.scm_url ?: null //TEMPLATE
    String scmCred = config?.scm_cred ?: null //creds to pull cc template from scm
    String checkout = config?.checkout ?: null //OPTION
    String cookiecutterDir = config?.cookiecutterDirectory ?: null //OPTION
    String outDir = config?.output_directory ?: null //OPTION
    String cookieCutterJson = config?.cookie_cutter_json ?: null //Overwrite cookiecutter.json with this file
    String projectFolder = config?.project_folder ?: null
    String ARGS = "" //Final Command
    Boolean noInput = config?.no_input ?: true //OPTION
    Boolean debugOn = config?.verbose ?: false //OPTION
    Boolean overwriteWorkspace = config?.overwrite_workspace ?: false
    Boolean shouldFail = false
        
    if (checkout) {
        ARGS += "--checkout ${checkout} "
    }

    if (cookiecutterDir) {
      ARGS += "--directory ${cookiecutterDir}"
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

        
    //cookiecutter [OPTIONS] [TEMPLATE] [EXTRA_CONTEXT]...
    inside_sdp_image(cookiecutterImage) {
      if (templatePath) {
        unstash 'workspace'
            
        ARGS += templatePath

        if (cookieCutterJson) {
          sh "cp -f ${cookieCutterJson} ./cookiecutter.json"
        }
      }
      else if (scmPull) {
        if (scmCred) {
          withCredentials([string(credentialsId: "${scmCred}", variable: 'PAT')]) {
            scmPull.replaceFirst("://","://${PAT}@")
            ARGS += scmPull
          }
        }  
        else {
          ARGS += scmPull
        }
      }
      
      try {
        sh "cookiecutter ${ARGS}"
      }
      catch (Exception err) {
        shouldFail = true
        echo "Failed: {$err}"
      }
      finally {
        if (overwriteWorkspace) {
          if (outDir) {
            dir("${outDir}" + "${projectFolder}")
          }
          else {
            dir("${projectFolder}") {
              stash name: 'workspace', allowEmpty: true, useDefaultExcludes: false
            }
          }
        }
        sh 'ls -alh'
      } 
    }
  }
}
