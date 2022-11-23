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

 if (jte.libraries.sonarqube) {
  println "Skipping this step, build occurs during static code analysis."
}
else {
  if (unity_app) {
      build_unity()
  }

  build_dotnet()
}



}