/*
  Copyright © 2018 Booz Allen Hamilton. All Rights Reserved.
  This software package is licensed under the Booz Allen Public License. The license can be found in the License file or at http://boozallen.github.io/licenses/bapl
*/

package libraries.dotnet.steps
import jenkins.model.Jenkins
import com.cloudbees.plugins.credentials.Credentials
import com.cloudbees.plugins.credentials.CredentialsProvider
import com.cloudbees.plugins.credentials.impl.UsernamePasswordCredentialsImpl
import org.jenkinsci.plugins.plaincredentials.impl.StringCredentialsImpl

void call() {

  // default values for config options
  // TODO: make the untiy creds required params and remove these defualt values
  // print error if null, there are examples of doing this in other libs
  LinkedHashMap defaults = [
    image: "unity:ubuntu-2020.3.30f1-base-1.0.1-1.1",
    stage_display_name: "Unity Build",
    unity_credential_id: "unitycreds",
    unity_serial_id: "unityserial",
    activate_license_parameters:  [ "-nographics", "-logFile=/dev/stdout"],
    build_unity_parameters: [ "-nographics", "-logFile=/dev/stdout" ]
  ]

  // credential ID for Unity license
  String  unity_credential_id = config.unity_credential_id ?: defaults.unity_credential_id

  // credential ID for Unity serial
  String unity_serial_id = config.unity_serial_id ?: defaults.unity_serial_id

  // sets image to use
  String image = config.image ?: defaults.image

  // purely aesthetic.  the name of the "Stage" for this task. 
  String stage_display_name = config.stage_display_name ?: defaults.stage_display_name

  stage(stage_display_name) {
    inside_sdp_image image, {
      withCredentials([ usernamePassword(credentialsId: unity_credential_id, usernameVariable: 'USERNAME', passwordVariable: 'PASSWORD'),
      string(credentialsId: unity_serial_id, variable: 'SERIAL')]) { // can this be a secret credential type? Should variable name be a configurable via config file?
        // base activate license command to execute
        unstash "workspace"

        ArrayList activate_license_command = [ "unity-editor -username '${USERNAME}' -password '${PASSWORD}' -serial '${SERIAL}' -projectPath=${workspace}  -quit" ]
        // join user provided params
        activate_license_command << (config.activate_license_parameters ?: defaults.activate_license_parameters)
        // Activate Unity License
        sh activate_license_command.flatten().join(" ")

        // base build unity command to execute
        // TODO: -projectPath=${workspace}  *** seems the solution inherits this name, is this configurable via parameters? **
        ArrayList build_unity_command = [ "unity-editor -projectPath=${workspace} -executeMethod UnityEditor.SyncVS.SyncSolution -quit" ]
        // join user provided unity build params
        build_unity_command << (config.build_unity_parameters ?: defaults.build_unity_parameters)
        // build the Unity solution
        sh build_unity_command.flatten().join(" ")

        // stash build results
        stash "workspace" 

      } 
    }
  }
}
