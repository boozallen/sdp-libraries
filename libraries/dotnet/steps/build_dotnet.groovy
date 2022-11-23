/*
  Copyright © 2018 Booz Allen Hamilton. All Rights Reserved.
  This software package is licensed under the Booz Allen Public License. The license can be found in the License file or at http://boozallen.github.io/licenses/bapl
*/

package libraries.dotnet.steps
import jenkins.model.Jenkins

void call() {

  // default values for config options
  LinkedHashMap defaults = [
    image: "dotnet-sonar-scanner:5.2.2-1.1",
    stage_display_name: "Dotnet Build",
    cli_parameters: [] // does it makes sense to allow people to pass additional params?
  ]

  // sets image to use
  String image = config.image ?: defaults.image

  // purely aesthetic.  the name of the "Stage" for this task. 
  String stage_display_name = config.stage_display_name ?: defaults.stage_display_name

  stage(stage_display_name) {
  // Need to move container to SDP.
  // using same container so this is no longer needed??
    inside_sdp_image image, {

      // fetch the source code 
      unstash "workspace"

      // build the build command
      ArrayList dotnet_build_command = [ "dotnet build" ]
      dotnet_build_command << (config.cli_parameters ?: defaults.cli_parameters)
      // run dotnet build on sln
      sh dotnet_build_command.flatten().join(" ")

      
   
      // stash build results
      stash "workspace" 
    }
  }
}
