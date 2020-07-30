/*
  Copyright © 2018 Booz Allen Hamilton. All Rights Reserved.
  This software package is licensed under the Booz Allen Public License. The license can be found in the License file or at http://boozallen.github.io/licenses/bapl
*/
void call(){
    stage("Webhint: Lint"){
      
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
      
      inside_sdp_image "webhint:1.8", {
        String resultsText = "hint.results.txt"
        String resultsJson = "hint.results.json"
        String resultsDir = "hint-report"
        
        sh script: """
            mkdir -p ${resultsDir};
            cp /.hintrc ./${resultsDir};
            cd ${resultsDir};
            cat /.hintrc;
            hint ${url} > ${resultsText};
           """, returnStatus: true
        
        // hint ${url} always exits non 0 so run cleanup work with separate sh
        // Our goal here to to remove the first two lines which are not valid json
        // Those lines were included as part of the redirection
        // This should be a feature request of Webhint.io to create a json file for us like html does
        sh """
            ls -al ${resultsDir};
            tail -n+3 ${resultsDir}/${resultsText} > ${resultsDir}/${resultsJson};
           """
        
        archiveArtifacts allowEmptyArchive: true, artifacts: "${resultsDir}/"
        //this.validateResults("${resultsDir}/${resultsFile}")
      }
    }
}

void validateResults(String resultsFile){
    if(!fileExists(resultsFile)){
        return
    }
}
