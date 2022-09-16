/*
  Copyright © 2022 Booz Allen Hamilton. All Rights Reserved.
  This software package is licensed under the Booz Allen Public License.
  The license can be found in the License file or at http://boozallen.github.io/licenses/bapl
*/

package libraries.cookiecutter.steps

void call() {
    stage("Cookiecutter") {
        String cookiecutterImage = config?.cookiecutter_image ?: "cookiecutter:2.1.1"
        String templatePath = config?.template_path ?: "./"
        String checkout = config?.checkout ?: null
        String directory = config?.cookiecutter_json_dir ?: null
        String outDir = config?.output_directory ?: null
        String configFile = config?.config_file ?: null
        String defaultConf = config?.default_config ?: null
        String debugFile = config?.debug_file ?: null
        String ARGS = ""
        Boolean replay = config?.replay ?: false
        Boolean noInput = config?.no_input ?: false
        Boolean overwrite = config?.overwrite_if_exists ?: false
        Boolean skip = config?.skip_if_file_exists ?: false
        Boolean showVer = config?.show_version ?: false
        Boolean debugOn = config?.verbose ?: false

        inside_sdp_image(cookiecutterImage) {
          unstash 'workspace'
          sh '''
            cp -f cookiecutter/docker_cookiecutter.json ./cookiecutter.json
            cookiecutter --no-input -o test ./
            ls -alh
            '''

          
        }

    }
}