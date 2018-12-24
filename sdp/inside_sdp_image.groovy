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
  def sdp_img_repo = pipeline_config().sdp_image_repository ?:
                     { error "SDP Image Repository not defined in Pipeline Config" }()
                     
  def sdp_img_repo_cred = pipeline_config().sdp_image_repository_credential ?:
                          { error "SDP Image Repository Credential not defined in Pipeline Config" }()
  
  docker.withRegistry(sdp_img_repo, sdp_img_repo_cred){
    docker.image("sdp/${img}").inside{
      body.resolveStrategy = Closure.DELEGATE_FIRST
      body.delegate = this
      body()
    }
  }
}