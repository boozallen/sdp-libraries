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
      
      inside_sdp_image "webhint", {
        //sh "cat /hint/.hintrc"
        //sh "cp /hint/.hintrc ."
        //sh "hint ${url}"
        
        String resultsFile = "hint-results.json"
        String resultsDir = "webhint"
        
        sh """
            cat /.hintrc;
            cp /.hintrc .;
            mkdir -p ${resultsDir};
            touch /${resultsDir}/${resultsFile}
           """
        // hint ${url} > ${resultsDir}/${resultsFile};
        
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
