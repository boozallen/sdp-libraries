/*
  Copyright © 2018 Booz Allen Hamilton. All Rights Reserved.
  This software package is licensed under the Booz Allen Public License. The license can be found in the License file or at http://boozallen.github.io/licenses/bapl
*/
void call(){
    stage("Webhint: Lint") {
      def url = config.url ?: {
        error """
        Webhint.io Library needs the target url.
        libraries{
          webhint{
            url = "https://example.com"
          }
        }
        """
      } ()
      
      inside_sdp_image "webhint:1.9", {
        String resultsDir = "hint-report"
        String resultsText = "hint.results.log"
        String resultsJson = "hint.results.json"
        def hintrc = [
          extends: config.extender ?: [ "accessibility" ],
          formatters: [ "html", "json", "summary" ]
        ]
        
        sh "mkdir -p ${resultsDir}"
        writeJSON file: "${resultsDir}/.hintrc", json: hintrc
        sh "cp ${resultsDir}/.hintrc .; cat .hintrc;"
        //sh script: "hint ${url} > ${resultsDir}/${resultsText}", returnStatus: true
        sh script: "hint ${url} -f html json -o ${resultsDir}/${resultsJson}", returnStatus: true
        
        // hint ${url} always exits non 0 so run cleanup work with separate sh
        // Our goal here to to remove the first two lines which are not valid json
        // Those lines were included as part of the redirection
        // This should be a feature request of Webhint.io to create a json file for us like html does
        /*
        // Webhint JSON formattor doesn't export a file for us. It sends this content to standard out.
        // This is hugely promblimatic since the json is intermixed with echos.
        // Webhint also will output multiple JSON objects depending on its findings
        // For now, just provide a summary and html. Revisit later when I got time to hand parse the redirection content.
        sh """
            tail -n+3 ${resultsDir}/${resultsText} > ${resultsDir}/${resultsJson};
            if [ ! -s ${resultsDir}/${resultsJson} ] ; then
              rm ${resultsDir}/${resultsJson}
            fi
           """
        */
        archiveArtifacts allowEmptyArchive: true, artifacts: "${resultsDir}/"
        //this.validateResults("${resultsDir}/${resultsJson}")
      }
    }
}

void validateResults(String resultsFile) {
    if(!fileExists(resultsFile)) {
        return
    }
  
    def results = readJSON file: "${resultsFile}"

    boolean shouldFail = results.size() >= config.failThreshold
    boolean shouldWarn = results.size() < config.failThreshold
    
    if(shouldFail) error("Webhint.io suggestions exceeded the fail threshold")
    if(shouldWarn) unstable("Webhint.io suggested some changes")
}
