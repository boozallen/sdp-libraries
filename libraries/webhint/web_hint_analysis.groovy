/*
  Copyright © 2018 Booz Allen Hamilton. All Rights Reserved.
  This software package is licensed under the Booz Allen Public License. The license can be found in the License file or at http://boozallen.github.io/licenses/bapl
*/
void call(){
    stage("Webhint: Lint") {
      
      String resultsDir = "hint-report"
      
      def url = env.FRONTEND_URL ?: config.url ?: {
        error """
        Webhint.io Library needs the target url.
        libraries{
          webhint{
            url = "https://example.com"
          }
        }
        """
      } ()
      
      this.createHintrcFile()
      
      inside_sdp_image "webhint:1.8", {
        this.makeArchiveStagingDirectory(resultDir)
        this.createHintrcFile(resultDir)

        String resultsText = "hint.results.txt"
        String resultsJson = "hint.results.json"
        
        sh script: """
            cp ${resultsDir}/.hintrc .;
            cat .hintrc;
            hint ${url} > ${resultsDir}/${resultsText};
           """, returnStatus: true
        
        // hint ${url} always exits non 0 so run cleanup work with separate sh
        // Our goal here to to remove the first two lines which are not valid json
        // Those lines were included as part of the redirection
        // This should be a feature request of Webhint.io to create a json file for us like html does
        sh """
            tail -n+3 ${resultsDir}/${resultsText} > ${resultsDir}/${resultsJson};
            rm -rf ${resultsDir}/${resultsText};
           """
        
        archiveArtifacts allowEmptyArchive: true, artifacts: "${resultsDir}/"
        //this.validateResults("${resultsDir}/${resultsFile}")
      }
    }
}

void makeArchiveStagingDirectory(String path) {
     sh "mkdir -p ${path}"
}

void createHintrcFile(String path) {
  def hintrc = [
    extends: [ "accessibility" ],
    formatters: [ "html", "json" ]
  ]

  writeJSON file: "${path}/.hintrc", json: hintrc
  sh "cat ${path}/.hintrc"
}

void validateResults(String resultsFile) {
    if(!fileExists(resultsFile)) {
        return
    }
}
