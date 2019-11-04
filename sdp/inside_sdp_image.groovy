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

  Map libraryConfig = body.getOwner().getConfig()
  Map imageConfig = libraryConfig?.images?.getAt(img)

  // + is generally left-associative : w + x + y + z -> ((w + x) + y) + z
  // groovy Map.plus(rightMap) gives precedence to right's values

  def callConfig = config.images ?: [:]

  if( libraryConfig?.images ){
    callConfig = callConfig + libraryConfig.images
  }

  if( imageConfig ){
    callConfig = callConfig + imageConfig
  }

  if( !callConfig.images ){
    error getMissingConfigMsg()
  }

  def errors = []

  def sdp_img_reg = callConfig.registry ?:
                    { errors << getMissingRegistryMsg() } ()
  
  def sdp_img_repo = callConfig.repository ?: "sdp"
                     
  def sdp_img_repo_cred = callConfig.cred ?:
                          { errors << getMissingCredentialMsg() }()
  
  def docker_args = params.args ?: ( callConfig.docker_args ?: "" )

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

String getMissingConfigMsg(){
  return "SDP Image Config is empty in Pipeline Config, Caller Library/Image Config"
}

String getMissingRegistryMsg(){
  return "SDP Image Registry not defined in Pipeline Config, Caller Library/Image Config"
}

String getMissingCredentialMsg(){
  return "SDP Image Repository Credential not defined in Pipeline Config, Caller Library/Image Config"
}