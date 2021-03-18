/*
  Copyright © 2018 Booz Allen Hamilton. All Rights Reserved.
  This software package is licensed under the Booz Allen Public License. The license can be found in the License file or at http://boozallen.github.io/licenses/bapl
*/

package libraries.owasp_dep_check.steps

void call() {
  stage('Application Dependency Scan: OWASP Dep Checker'){
    String resultsDir = "owasp-dependency-check"
    String args = "--out ${resultsDir} --enableExperimental --format ALL" 

    ArrayList scan = config.scan ?: [ '.' ] 
    scan.each{ s -> args += " -s ${s}" }

    ArrayList exclude = config.exclude ?: []
    exclude.each{ e -> args += " --exclude ${e}" }

    // vulnerabilities greater than this will fail the build 
    // max value 10 
    if(config.containsKey("cvss_threshold")){
      Double threshold = config.cvss_threshold 
      if(threshold <= 10.0){
        args += " --failOnCVSS ${threshold} --junitFailOnCVSS ${threshold}"
      }
    }

    String image_tag = config.image_tag ?: "latest"
    inside_sdp_image "owasp-dep-check:$image_tag", {
      unstash "workspace"
      try {
        sh "mkdir -p ${resultsDir} && mkdir -p owasp-data && /usr/share/dependency-check/bin/dependency-check.sh ${args} -d owasp-data"
      }catch (ex) {
        error "Error occured when running OWASP Dependency Check: ${ex.getMessage()}"
      }finally {
        archiveArtifacts allowEmptyArchive: true, artifacts: "${resultsDir}/"
        junit allowEmptyResults: true, healthScaleFactor: 0.0, testResults: "${resultsDir}/dependency-check-junit.xml"
      }
    }
  }
}
