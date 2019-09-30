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
void call(String img, Map params = [:], Closure body){

  def errors = []

  config.images ?: { error "SDP Image Config not defined in Pipeline Config" } ()
  
  def sdp_img_reg = config.images.registry ?:
                    { errors << "SDP Image Registry not defined in Pipeline Config" } ()
  
  def sdp_img_repo = config.images.repository ?: "sdp"
                     
  def sdp_img_repo_cred = config.images.cred ?:
                          { errors << "SDP Image Repository Credential not defined in Pipeline Config" }()
  
  def docker_args = params.args ?: ( config.images.docker_args ?: "" )

  def docker_command = params.command ?: ""

  if(!errors.empty) {
    error errors.join("; ")
  }
  
  docker.withRegistry(sdp_img_reg, sdp_img_repo_cred){
    docker.image("${sdp_img_repo}/${img}").inside("${docker_args}", "${docker_command}"){
      body()
    }
  }
}
