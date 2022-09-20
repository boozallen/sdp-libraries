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
        //String checkout = config?.checkout ?: null
        String scmURL = config?.scm_pull ?: null //TEMPLATE
        String outDir = config?.output_directory ?: null //OPTION
        String cookieCutterJson = config?.cookie_cutter_json ?: null //Overwrite cookiecutter.json with this file
        String cookieCutterFolder = config?.cookie_cutter_folder ?: null
        //String configFile = config?.config_file ?: null
        //String defaultConf = config?.default_config ?: null
        //String debugFile = config?.debug_file ?: null
        String ARGS = "" //Final Command
        Boolean noInput = config?.no_input = false //OPTION
        Boolean debugOn = config?.verbose = false //OPTION
        Boolean overwriteWorkspace = config?.overwrite_workspace = false
        Boolean shouldFail = false
        
        if (!outDir) {
          ARGS += " --output-dir ${outDir}"
        }

        if (noInput) {
          ARGS += " --no-input"
        }
        
        if (debugOn) {
          ARGS += " --verbose " //last option will need a space before and after input
        }

        
        //cookiecutter [OPTIONS] [TEMPLATE] [EXTRA_CONTEXT]...
        inside_sdp_image(cookiecutterImage) {
            if (templatePath) {
                unstash 'workspace'
            
                ARGS += templatePath

                if (cookieCutterJson) {
                    sh 'cp -f ./${cookieCutterJson} ./cookiecutter.json'
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
                        dir("app-name-here") {
                        stash name: 'workspace', allowEmpty: true, useDefaultExcludes: false
                        }
                    }
                }
            }
        }
    }
}