/*
  Copyright © 2018 Booz Allen Hamilton. All Rights Reserved.
  This software package is licensed under the Booz Allen Public License. The license can be found in the License file or at http://boozallen.github.io/licenses/bapl
*/

package libraries.docker.steps

/*
  returns an array of the images that are built by this pipeline run.
  each image in the array is a hashmap with fields:
    registry: image registry
    repo: image name with repo path
    tag: image tag
    context: directory context for docker build

  a docker build command would then be:
    docker build img.context -t img.registry/img.repo:img.tag

  in the case of buildx each image in the array is a hashmap with fields:
    registry: image registry
    repo: repo name
    tag: image tag
    context: context for dockerfile
    dockerfilePath: name and path of dockerfile
    build_args: build args for the specific image
    platforms: platforms to be built for
    useLatestTag: if pipeline will use the previously defined tag + the latest tag
    
*/

def call(){

    def (image_reg) = get_registry_info() // config.registry
    def path_prefix = config.repo_path_prefix ? config.repo_path_prefix + "/" : ""

    def build_strategies = [ "docker-compose", "modules", "dockerfile", "buildx" ]
    println config
    if (config.build_strategy)
    if (!(config.build_strategy in build_strategies)) {
        error "build strategy: ${config.build_strategy} not one of ${build_strategies}"
    }

    def images = []
    def image_name = config.image_name ?: env.REPO_NAME

    switch (config.build_strategy) {
      case "docker-compose":
        error "docker-compose build strategy not implemented yet"
        break
      case "modules":
        findFiles(glob: "*/Dockerfile").collect{ it.path.split("/").first() }.each{ service ->
          images.push([
            registry: image_reg,
            repo: "${path_prefix}${image_name}_${service}".toLowerCase(),
            tag: env.GIT_SHA,
            context: service
          ])
        }
        break
      case "buildx":
        images = buildx(image_reg,path_prefix)
        break
      case "dockerfile": //same as null/default case
      case null:
        images.push([
          registry: image_reg,
          repo: "${path_prefix}${image_name}".toLowerCase(),
          tag: env.GIT_SHA,
          context: "."
        ])
        break
    }
    return images
}

ArrayList buildx(image_reg, path_prefix) {

  ArrayList images = []

  def buildx = config.buildx ?: { error "buildx not defined in Pipeline Config" } ()
  def same_repo_different_tags = config.same_repo_different_tags ?: false

  buildx.each{ name,img -> 

    String repo = ""
    String context = ""
    String tag = ""
    String dockerfilePath = ""
    boolean useLatestTag = false
    boolean latestTagUsed = false

    // if the pipeline config does not contain a context to where the dockerfile is then look at root
    if (!img.containsKey("context")) {
      context = "."
    } else {
      context = img.context
    }

    //if the pipeline config does not contain a dockerfilepath then do not include
    if (img.containsKey("dockerfile_path")) {
      dockerfilePath = " -f ${img.dockerfile_path}"
    }
    // if a custom tag is to be used then it can be specified. otherwise it will use the git sha
    if (!img.containsKey("tag")) {
      tag += env.GIT_SHA
    } else {
      tag += img.tag
    }
    // if you need a latest tag in addition to the defined tag or the git sha tag
    if(img.containsKey("useLatestTag")) {
        // if the pipeline has useLatestTag set to true and is trying to build for the same repo
        if (img.useLatestTag && same_repo_different_tags) {
          // check if latest flag has already been used for the repo and if it hasnt then set the latest tag
          if (!latestTagUsed) {
            useLatestTag = img.useLatestTag
            latestTagUsed = true
          }
          //else error because it should not override the other image that previously set the latest flag
          else {
            error "Cannot use multiple latest tags if you are building multiple images for the same repo"
          }
          // if not building with the same repo name or useLatestTag is set to false then use the config value
        } else {
          useLatestTag = img.useLatestTag
        }
    }

    //if same_repo_different_tags flag is set then it will keep the repo name the same and appends the element name to the tag
    if (same_repo_different_tags) {
      tag += "-" + name
      repo = "${path_prefix}${env.REPO_NAME}".toLowerCase()
    }
    else {
      // if there is only one element in buildx array than it does not need the name variable from the pipeline config to make the repo unique so it will skip this step
      // otherwise if it is building more than one image, than each image should have a unique name so it appends the element name to the repo
      if (!(buildx.size() == 1)) {
          repo = "${path_prefix}${env.REPO_NAME}_${name}".toLowerCase()
        }else {
          repo = "${path_prefix}${env.REPO_NAME}".toLowerCase()
        }      
    }
    //push the image into the array for the buildx method to use
    images.push([
      registry: image_reg,
      repo: repo,
      tag: tag,
      context: context,
      dockerfilePath: dockerfilePath,
      build_args: img.build_args,
      platforms: img.platforms,
      useLatestTag: useLatestTag
    ])
  }

  return images
}