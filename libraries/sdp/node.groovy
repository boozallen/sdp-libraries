/*
  Copyright © 2018 Booz Allen Hamilton. All Rights Reserved.
  This software package is licensed under the Booz Allen Public License. The license can be found in the License file or at http://boozallen.github.io/licenses/bapl
*/

/***************************************************************************************************
  An implementtaion of custom node step to be used by SDP library  steps to run a step on a 
  particular node. 
  
***************************************************************************************************/
void call(String label = null, Closure body){
    println "Inside nodeStep"
    def bodyConfig = [:]
    try{
        bodyConfig = body.config
    }catch(MissingPropertyException ex){
       /*
          config not defined
          this probably means the node block called is not called from within a library step 
          - should be processed in the traditional manner
       */

    /* If being called with a specified node label no need to process further */
    processNodeCall(label,body,true)
    return
}
    procesNodeCall(label,body,false)
    return
}

void processNodeCall(String label, Closure body, Boolean forceUseDefault){

    if (forceUseDefault && label){
      steps.node(label){
        body()
      }
      return
    }

    /* Determine agent Type from library configuration and default configuration 
       Assume generic if none specified */
    if(forceUseDefault)
       agentType = config.agentType ?: { return "generic" }()
    else
       agentType = body.config.agentType ?: config.agentType ?: { return "generic" }()
                                

    switch(agentType){
      case "kubernetes":
        handleKubernetesNode(label,body,forceUseDefault)
        break
      case "docker":
        handleDockerNode(label,body,forceuseDefault)
        break
      case "generic":
        handleGenericNode(label,body,forceUseDefault)
        break
      default:
        error "SDP Agent Type not derivable" 
        break
    }
}


/***************************************************************************************************

handleKubernetesNode() implements the node step when the agentType is kubernetes

***************************************************************************************************/

void handleKubernetesNode( String label, Closure body, Boolean forceUseDefault)
{
    println "Inside handleKubernetesNode"
    if (forceUseDefault && !(config.podSpec &&  config.podSpec.img)){
      steps.node(){
        body()
      }
    }
    else{
      podTemplate(yaml: "${getPodTemplate(label,body,forceUseDefault)}",workingDir: "/home/jenkins/agent", cloud: "${getPodCloudName(body,forceUseDefault)}", namespace: "${getPodNamespace(body,forceUseDefault)}"){
        steps.node(POD_LABEL){
          container('sdp-container') {
            body()
          }
        }
      }
   }
}

/***************************************************************************************************

handleDockerNode() implements the node step when the agentType is docker

***************************************************************************************************/

void handleDockerNode(String label, Closure body, Boolean forceUseDefault)
{

   println "Inside handleDockerNode"
   if (forceUseDefault && !(config.images &&  config.images.img)){
     error "Default SDP Image not defined  for agent type "docker"
     }
   else{
     def nodeLabel = getNodeLabel(body,forceUseDefault)
     if (nodeLabel != "")
     {
        steps.node(nodeLabel){
          def sdp_img_reg = getRegistry(body,"docker",forceUseDefault)
          if (sdp_img_reg != ""){
            docker.withRegistry(sdp_img_reg, "${getRegistryCred(body,"docker",forceUseDefault)}"){
              docker.image("${getImage(label,body,"docker",forceUseDefault)}").inside("${getDockerArgs(body,forceUseDefault)}"){
                body()
              } 
            }
          }
          else{
            /* For public docker registry, there is no need to login */
            docker.image("${getImage(label,body,"docker",forceUseDefault)}").inside("${getDockerArgs(body,forceUseDefault)}"){
              body()
            }
          }
        }
     }
     else
     {
        println "docker agentType launching on any available node"
       /* Execute on any available node */
       steps.node(){
         def sdp_img_reg = getRegistry(body,"docker",forceUseDefault)
         if (sdp_img_reg != ""){
            docker.withRegistry(sdp_img_reg, "${getRegistryCred(body,"docker",forceUseDefault)}"){
              docker.image("${getImage(label,body,"docker",forceUseDefault)}").inside("${getDockerArgs(body,forceUseDefault)}"){
                body()
             }
           }
        }
        else{
          /* For public docker registry, there is no need to login */
          docker.image("${getImage(label,body,"docker",forceUseDefault)}").inside("${getDockerArgs(body,forceUseDefault)}"){
            body()
          }  
        }
      }
    }
  }
}

/***************************************************************************************************

handleGenericNode() implements the node step when the agentType is generic 

***************************************************************************************************/

void handleGenericNode(String label, Closure body, Boolean forceUseDefault)
{
   println " Inside handleGenericNode"
   def nodeLabel = getNodeLabel(body,forceUseDefault)
   if (nodeLabel != "")
   {
      /* Execute on a particular node */
      steps.node(nodeLabel){
        body()
      } 
   }
   else
   {
     /* Execute on any available node */
     steps.node(){
       body()
     }
   }
}


/***************************************************************************************************

getImage()  is a helper method which formulates the complete image defenition by checking the 
library's configuration and then the default configuration for image name and repo name. If no
image name is specified in either of these locations then the "label" passed by the calling library 
function is used as a default. 

***************************************************************************************************/ 

String getImage(String label, Closure body, String agentType, Boolean forceUseDefault)
{

   println " Inside getImage"
   if (agentType == "docker"){
    if(forceUseDefault)
        bodyConfig = config.images
    else
   	bodyConfig = body.config.images
      libConfig = config.images
   }
   else{
    if(forceUseDefault)
        bodyConfig = config.podSpec
    else
   	bodyConfig = body.config.podSpec
      libConfig = config.podSpec
   }
      
   def sdp_img = bodyConfig ? bodyConfig.img ?: libConfig ? libConfig.img ?: label ?: { error "SDP Image  not defined in Pipeline Config" } ()
                                                          :  label ?: { error "SDP Image  not defined in Pipeline Config" } ()
                            :  libConfig ? libConfig.img ?: label ?: { error "SDP Image  not defined in Pipeline Config" } ()
                                         : label ?: { error "SDP Image  not defined in Pipeline Config" } ()


   def sdp_img_repo = bodyConfig ? bodyConfig.repository ?: libConfig ? libConfig.repository ?: { return ""}()
                                                                      : { return ""}()
                                 : libConfig ? libConfig.repository ?: { return ""}()
                                             : { return ""}()

   if (sdp_img_repo != "")
     return "${sdp_img_repo}/${sdp_img}"
   else
     return "${sdp_img}"

}

/***************************************************************************************************

getRegistry()  is a helper method which formulates the docker registry defenition by checking 
the library's configuration and then the default configuration. If no registry name is specified in 
either of these locations then an empty string is returned

***************************************************************************************************/ 

String getRegistry(Closure body, String agentType, Boolean forceUseDefault)
{
   println "Inside getRegistry"
   if (agentType == "docker"){
    if(forceUseDefault)
        bodyConfig = config.images
    else
        bodyConfig = body.config.images
      libConfig = config.images
   }
   else{
    if(forceUseDefault)
        bodyConfig = config.podSpec
    else
        bodyConfig = body.config.podSpec
      libConfig = config.podSpec
   }

  def sdp_img_reg =  bodyConfig ? bodyConfig.registry ?: libConfig ? libConfig.registry ?: { return ""}()
                                                                   : { return ""}()
                                : libConfig ? libConfig.registry ?: { return ""}()
                                            : { return ""}()

  return sdp_img_reg
}

/***************************************************************************************************

getRegistryCred()  is a helper method which formulates the docker registry Credential 
defenition by checking the library's configuration and then the default configuration. If no 
registry name is specified in either of these locations then an empty string is returned

***************************************************************************************************/ 

String getRegistryCred(Closure body, String agentType, Boolean forceUseDefault)
{

   println "Inside getRegistryCred"

   if (agentType == "docker"){
    if(forceUseDefault)
        bodyConfig = config.images
    else
        bodyConfig = body.config.images
      libConfig = config.images
   }
   else{
    if(forceUseDefault)
        bodyConfig = config.podSpec
    else
        bodyConfig = body.config.podSpec
      libConfig = config.podSpec
   }

  def sdp_img_reg_cred =  bodyConfig ? bodyConfig.cred ?:  libConfig ? libConfig.cred ?: { return "sdp"}()
                                                                     : { return ""}()
                                     : libConfig ? libConfig.cred ?: { return ""}()
                                                 : { return ""}()


  return sdp_img_reg_cred
}

/***************************************************************************************************

getDockerArgs()  is a helper method which formulates the docker Arguments  defenition by checking 
the library's configuration and then the default configuration. If no registry name is specified in 
either of these locations then an empty string is returned

***************************************************************************************************/ 

String getDockerArgs(Closure body, Boolean forceUseDefault)
{

   println "Inside getDockerArgs"
   if (forceUseDefault)
      docker_args =  config.images ? config.images.docker_args?: { return ""}()
                                      : { return ""}()
   else
      docker_args =  body.config.images ? body.config.images.docker_args ?: config.images ? config.images.docker_args?: { return ""}()
                                                                                           : { return ""}()
                                           : config.images ? config.images.docker_args?: { return ""}()
                                                           : { return ""}()
   return docker_args

}

/***************************************************************************************************

getNodeLabel()  is a helper method which checks the library configuration and then the default 
configuration to find the dpecified nodeLable. If no nodeLabel is specified ine ither of these 
configurations an empty string is returned

***************************************************************************************************/

String getNodeLabel(Closure body, Boolean forceUseDefault)
{

println "Inside getNodeLabel"
if (forceUseDefault)
  def nodeLabel = config.nodeLabel ?: {return "" }()
else
  def nodeLabel = body.config.nodeLabel ?: config.nodeLabel ?: {return "" }()

}

/***************************************************************************************************

getPodTemplate() returns the pod Yaml after filling in the image and imagePullSecret fields from the
library configuration or default SDP configuration. If no image name is derived from the 
configurations, then an empty string is returned

***************************************************************************************************/

String getPodTemplate(String label, Closure body, Boolean forceUseDefault) {

println "Inside getPodTemplate"
  def podYaml= """\
apiVersion: v1
kind: Pod
metadata:
  name: sdp-slave
spec:
  containers:
  - image: ${getPodImage(label,body,forceUseDefault)}
    imagePullPolicy: IfNotPresent
    imagePullSecrets: ${getRegistryCred(body,"kubernetes",forceUseDefault)}
    name: sdp-container
    tty: true
    workingDir: /home/jenkins/agent
  serviceAccount: jenkinsagentsa
  serviceAccountName: jenkinsagentsa
"""
  return podYaml
}

/***************************************************************************************************

getPodImage() s a helper method to getPodTemplate() and returns the image  for the container to be 
used to deploy the Kubernetes pod

***************************************************************************************************/

String getPodImage(String label, Closure body, Boolean forceUseDefault){

  println "Inside getPodImage"
  def sdp_img     = getImage(label,body,"kubernetes",forceUseDefault)

  def sdp_img_reg = getRegistry(body,"kubernetes",forceUseDefault)

  if (sdp_img_reg != "") 
   return "${sdp_img_reg}/${sdp_img}"
  else
     return "${sdp_img}"
}

/***************************************************************************************************

getPodNamespace() is a helper method that returns the namespace to launch the dynamic 
kubernetes jenkins agent pods

***************************************************************************************************/

String getPodNamespace(Closure body, Boolean forceUseDefault){

  println "Inside getPodNamespace"
  if(forceUseDefault)
     namespace = config.podSpec ? config.podSpec.namespace ?: { return "default" }() 
                                   : { return "default" }()
  else
     namespace = body.config.podSpec ? body.config.podSpec.namespace ?: config.podSpec ? config.podSpec.namespace ?: { return "default" }() 
                                                                                        : { return "default" }() 
                                        : config.podSpec ? config.podSpec.namespace ?: { return "default" }()
                                                         : { return "default" }() 
  return namespace
  
}

/***************************************************************************************************

getPodCloudName() is a helper method that returns the kubernetes cloud name to launch the dynamic 
kubernetes jenkins agent pods

***************************************************************************************************/

String getPodCloudName(Closure body, Boolean forceUseDefault){

  println "Inside getPodCloudName"
  if(forceUseDefault)
    cloudName = config.podSpec ? config.podSpec.cloudName ?: { return "kubernetes" }() 
                                   : { return "kubernetes" }()
  
  else
    cloudName = body.config.podSpec ? body.config.podSpec.cloudName ?: config.podSpec ? config.podSpec.cloudName ?: { return "kubernetes" }() 
                                                                                          : { return "kubernetes" }() 
                                        : config.podSpec ? config.podSpec.namespace ?: { return "kubernetes" }()
                                                         : { return "kubernetes" }() 

   return cloudName
   
}
