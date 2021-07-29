package libraries.docker.steps

import com.cloudbees.plugins.credentials.CredentialsProvider
import org.jenkinsci.plugins.plaincredentials.impl.StringCredentialsImpl
import com.cloudbees.plugins.credentials.Credentials

void call(){
  stage "Building Multiarch Docker Image", {

    // Build Strategy must be set to buildx in order for this method to work
    if (!(config.build_strategy == "buildx")) {
      error "build strategy: ${config.build_strategy} is not buildx"
    }

    // This is so that if jenkins agents are running with a version of docker that only had buildx in the experimental features 
    // then it will set the experimental flag
    boolean setExperimentalFlag = config.containsKey("setExperimentalFlag") ? config.setExperimentalFlag : false 

    node{
      unstash "workspace"
      
      login_to_registry{
        // returns array of images to build
        ArrayList images = get_images_to_build()

        String experimentalFlag = ""
        if (setExperimentalFlag) {
          experimentalFlag = "DOCKER_CLI_EXPERIMENTAL=enabled "
        }

        // create the builder and tell it to spin it up in a container
        sh "${experimentalFlag}docker buildx create --name smartbuilder --driver docker-container --use"

        images.each{ img -> 
            echo "building image"
            // returns the platforms to build for this image in the format needed for buildx
            String platforms = getPlatforms(img.platforms)
            // each image in the array has its own build arguments so get this specific image's arguments
            def returnedArgs = getBuildArgs(img.build_args)
            def creds = returnedArgs["creds"]
            def args = returnedArgs["args"]

            String latestTag = ""
            // if the latestTag is used then add the additional tag flag
            if(img.useLatestTag ){
              latestTag = " -t ${img.registry}/${img.repo}:latest"
            }

            try {
              withCredentials(creds) {
                  //this experimental=enabled variable allows for buildx to be used on the current docker ce version. later versions do not need this flag
                  //when the docker version gets updates in the jenkins agent then this can be changed. 
                  sh "${experimentalFlag}docker buildx build ${img.context} -t ${img.registry}/${img.repo}:${img.tag}${latestTag} ${args} ${platforms} --push"
              }
            } catch (any) {
                  error "error building and pushing multiarchitecture image"
                  sh "${experimentalFlag}docker buildx rm smartbuilder"
            }             
        }
        //remove the builder so that it is not left hanging on the node
        sh "${experimentalFlag}docker buildx rm smartbuilder"
      }
    }
  }
}

def getBuildArgs(def build_args){

  echo "Getting Build Args"

  ArrayList buildArgs = []
  def creds = []

  build_args.each{ argument, value ->
    if(value instanceof Map){
      switch(value?.type){
        case "credential": // validate credential exists and is a secrettext cred
          def allCreds = CredentialsProvider.lookupCredentials(Credentials, Jenkins.get(),null, null)
          def cred = allCreds.find{ it.id.equals(value.id) }
          if(cred == null){
              error "docker library: build argument '${argument}' specified credential id '${value.id}' which does not exist."
          }
          if(!(cred instanceof StringCredentialsImpl)){
            error "docker library: build argument '${argument}' credential must be a Secret Text."
          }
          creds << string(credentialsId: value.id, variable: argument)
          buildArgs << "--build-arg ${argument}=\$${argument}"
          break;
        case null: // no build argument type provided
          error "docker library: build argument '${argument}' must specify a type"
          break;
        default: // unrecognized argument type
          error "docker library: build argument '${argument}' type of '${value.type}' is not recognized"
      }
    } else {
      buildArgs << "--build-arg ${argument}='${value}'"
    }
  }

  //due to each image having its own build args and credentials, returning this map is necessary so that each image can set the creds properly
  def returnList = ["args": buildArgs.join(" "), "creds": creds]

  return returnList
}

String getPlatforms(def platformsArr) {
  echo "Getting platforms to build image for"

  String platforms = "--platform"

  // default to amd64 if platforms are not set
  if(platformsArr.size() == 0){
    return platforms + " linux/amd64"
  }

  platformsArr.eachWithIndex { platform, index -> 

    if (index == 0){
      platforms += " ${platform}"
    } else {
          platforms += ",${platform}"
    }
  }
  return platforms
}