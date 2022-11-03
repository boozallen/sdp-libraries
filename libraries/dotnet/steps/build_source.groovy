/*
  Copyright © 2018 Booz Allen Hamilton. All Rights Reserved.
  This software package is licensed under the Booz Allen Public License. The license can be found in the License file or at http://boozallen.github.io/licenses/bapl
*/

package libraries.dotnet.steps
import jenkins.model.Jenkins

void call() {

  // default values for config options
  LinkedHashMap defaults = [
    unity_app: false
  ]

  // whether or not this is a unity build
  Boolean unity_app = defaults.unity_app
  if(config.containsKey("unity_app")){
    unity_app = config.unity_app
  }

  try {
    // if sonarqube library is loaded then skip, else run appropriate builds
    if (jte.libraries.sonarqube) {
      println "Skipping this step, build occurs during static code analysis."
    }
  }
  catch (any) {
    // if static code analysis is not configured in this, run build commands
    if (unity_app == true) {
      build_unity()
      build_dotnet()
    }
    else
      build_dotnet()
  }
}