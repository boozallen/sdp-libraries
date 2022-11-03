/*
  Copyright © 2018 Booz Allen Hamilton. All Rights Reserved.
  This software package is licensed under the Booz Allen Public License. The license can be found in the License file or at http://boozallen.github.io/licenses/bapl
*/

package libraries.sonarqube.steps
import jenkins.model.Jenkins
import com.cloudbees.plugins.credentials.Credentials
import com.cloudbees.plugins.credentials.CredentialsProvider
import com.cloudbees.plugins.credentials.impl.UsernamePasswordCredentialsImpl
import org.jenkinsci.plugins.plaincredentials.impl.StringCredentialsImpl
import hudson.plugins.sonar.SonarGlobalConfiguration

void call() {

  // default values for config options
  LinkedHashMap defaults = [
    credential_id: "sonarqube-token",
    unity_app: false,
    image: "dotnet-sonar-scanner:5.2.2-1.1",
    installation_name: "SonarQube",
    stage_display_name: "SonarQube Dotnet Analysis",
    unstash: [ "workspace" ],
    scanner_begin_parameters: [],
    dotnet_build_parameters: [],
    scanner_end_parameters: []
  ]

  // name of installation to use, as configured in Manage Jenkins > Configure System > SonarQube Installations
  String installation_name = config.installation_name ?: defaults.installation_name

  // whether or not this is a unity build
  Boolean unity_app = defaults.unity_app
  if(config.containsKey("unity_app")){
    unity_app = config.unity_app
  }

  // credential ID for SonarQube Auth
  String cred_id = config.credential_id ?: defaults.credential_id

  //sonar project key
  String sonar_project_key = config.sonar_project_key ?: '';
  // dotnet sonarscanner does not use properties file. Try to get project key from env
  if(sonar_project_key.isEmpty()){
      if ((env.ORG_NAME ?: '').isEmpty()){
          sonar_project_key = "${env.REPO_NAME}"
      } else {
          sonar_project_key = "${env.ORG_NAME}:${env.REPO_NAME}"
      }
  }

  // purely aesthetic.  the name of the "Stage" for this task. 
  String stage_display_name = config.stage_display_name ?: defaults.stage_display_name

  // sets image to use
  String image = config.image ?: defaults.image

  ArrayList unstashList = config.unstash ?: defaults.unstash

  // if a unity project, build the unity solution
  if (unity_app)
    build_unity()

  stage(stage_display_name) {
    inside_sdp_image image, {
      withCredentials([string(credentialsId: cred_id, variable: 'sq_token')]) {
        withSonarQubeEnv(installation_name){ 

          // fetch the source code 
          unstash "workspace"

          // build commands to execute
          // start with base command...
          ArrayList scanner_begin_command = [ "dotnet-sonarscanner begin" ]
          ArrayList dotnet_build_command = [ "dotnet build" ]
          ArrayList scanner_end_command = [ "dotnet-sonarscanner end" ]

          scanner_begin_command << "/k:'${sonar_project_key}' /d:sonar.login='${env.sq_token}' /d:sonar.host.url='${SONAR_HOST_URL}'"
          scanner_end_command << "/d:sonar.login='${env.sq_token}'"

          // then join user provided params
          scanner_begin_command << (config.scanner_begin_parameters ?: defaults.scanner_begin_parameters)
          dotnet_build_command << (config.dotnet_build_parameters ?: defaults.dotnet_build_parameters)
          scanner_end_command << (config.scanner_end_parameters ?: defaults.scanner_end_parameters)

          // begin dotnet sonar scan
          sh scanner_begin_command.flatten().join(" ")

          // run dotnet build on sln
          sh dotnet_build_command.flatten().join(" ")
          // end dotnet sonar scan, send results to sonar server
          sh scanner_end_command.flatten().join(" ")
        }        
      }
    } 
  }
}
