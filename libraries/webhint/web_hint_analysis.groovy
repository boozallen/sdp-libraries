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
      
      inside_sdp_image "webhint:1.0", {
        //sh "echo \"{\"extends\":[\"accessibility\"]\",\"formatters\":[\"json\"]} > .hintrc"
        sh "cp /hint/.hintrc ."
        sh "ls -al"
        //sh "ls -al /hint"
        sh "cat .hintrc"
        sh "hint ${url}"
      }
    }
}
