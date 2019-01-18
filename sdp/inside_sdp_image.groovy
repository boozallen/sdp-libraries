/*
  Copyright © 2018 Booz Allen Hamilton. All Rights Reserved.
  This software package is licensed under the Booz Allen Public License. The license can be found in the License file or at http://boozallen.github.io/licenses/bapl
*/

/*
  A helper function for running pipeline code inside
  an SDP pipeline image. 
  
  ex: 
  inside_sdp_image "openshift_helm", {
    sh "helm version"
  }
*/
void call(String img, Closure body){
  
  def sdp_img_reg = config.registry ?:
                    { error "SDP Image Registry not defined in Pipeline Config" } ()
  
  def sdp_img_repo = config.repository ?:
                     { echo "SDP Image Repository not defined in Pipeline Config. Defaulting to \"sdp\""; return "sdp" }()
                     
  def sdp_img_repo_cred = config.cred ?:
                          { error "SDP Image Repository Credential not defined in Pipeline Config" }()
  
  docker.withRegistry(sdp_img_repo, sdp_img_repo_cred){
    docker.image("${sdp_image_repo}/${img}").inside{
      body.resolveStrategy = Closure.DELEGATE_FIRST
      body.delegate = this
      body()
    }
  }
}
