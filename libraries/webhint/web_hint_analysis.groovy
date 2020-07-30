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
      
      inside_sdp_image "webhint:latest", {
        //sh "cat /hint/.hintrc"
        //sh "cp /hint/.hintrc ."
        //sh "hint ${url}"
        
        sh """
            cat /hint/.hintrc;
            cp /hint/.hintrc .;
           """
        
        //hint ${url} > /hint/hint-results.json
        archiveArtifacts allowEmptyArchive: true, artifacts: "/hint/"
        this.validateResults()
      }
    }
}

void validateResults(){
    if(!fileExists("/hint/hint-results.json")){
        return
    }
}
