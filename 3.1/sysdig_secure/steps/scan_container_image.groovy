/*
  Copyright © 2018 Booz Allen Hamilton. All Rights Reserved.
  This software package is licensed under the Booz Allen Public License. The license can be found in the License file or at http://boozallen.github.io/licenses/bapl
*/

package libraries.sysdig_secure.steps

void call(){
  stage("Scanning Container Image: Sysdig Secure"){
    node{
        String inlineScriptLocation = config.scan_script_url ?: "https://download.sysdig.com/stable/inline_scan.sh"      
        String sysdig_secure_url = config.sysdig_secure_url ?: null
        boolean enforce_success = config.containsKey("enforce_success") ? config.enforce_success : true
        String sArg = sysdig_secure_url ? "-s ${sysdig_secure_url}" : "-s https://secure.sysdig.com"
        withCredentials([string(credentialsId: config.cred, variable: 'TOKEN')]) {
            catchError(message: 'Failed to fetch inline_scan.sh from GitHub', stageResult: 'FAILURE') {
              sh "curl -o inline_scan.sh ${inlineScriptLocation}"
            }
            String resultsDir = "sysdig-secure"
            sh "mkdir -p ${resultsDir}"
            def imageThreads = [:]
            get_images_to_build().each{ img ->
              String image = "${img.registry}/${img.repo}:${img.tag}"
              imageThreads[image] = {
                login_to_registry{
                  sh "docker pull ${image}"
                  sh "sh inline_scan.sh analyze -R ${resultsDir} ${sArg} -k $TOKEN ${image}"
                }
              }
            }
            try{
              parallel imageThreads          
            }catch(any){
              String msg = "Sysdig Secure: Failed to scan images"
              enforce_success ? error(msg) : unstable(msg)
            }finally{
              archiveArtifacts allowEmptyArchive: true, artifacts: "${resultsDir}/"
            }
        }
    }
  }
}