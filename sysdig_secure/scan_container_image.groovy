/*
  Copyright © 2018 Booz Allen Hamilton. All Rights Reserved.
  This software package is licensed under the Booz Allen Public License. The license can be found in the License file or at http://boozallen.github.io/licenses/bapl
*/


void call(){
  stage("Scanning Container Image: Sysdig Secure"){
    node{
        String sysdig_secure_url = config.sysdig_secure_url ?: { error "must define url for sysdig secure" }()
        withCredentials([string(credentialsId: 'sysdig-secure-api', variable: 'TOKEN')]) {
            catchError(message: 'Failed to fetch inline_scan.sh from GitHub', stageResult: 'FAILURE') {
              sh 'curl -o inline_scan.sh https://raw.githubusercontent.com/sysdiglabs/secure-inline-scan/master/inline_scan.sh'
            }
            try{
              String resultsDir = "sysdig-secure"
              sh "mkdir -p ${resultsDir}"
              def imageThreads = [:]
              get_images_to_build().each{ img ->
                String image = "${img.registry}/${img.repo}:${img.tag}"
                imageThreads[image] = {
                  sh "docker pull ${image}"
                  sh "sh inline_scan.sh analyze -R ${resultsDir} -s ${sysdig_secure_url} -k $TOKEN ${image}"
                }
              }
              parallel imageThreads          
            }catch(any){
              error "Sysdig Secure: Failed to scan images" 
            }finally{
              archiveArtifacts allowEmptyArchive: true, artifacts: "${resultsDir}/"
            }
        }
    }
  }
}