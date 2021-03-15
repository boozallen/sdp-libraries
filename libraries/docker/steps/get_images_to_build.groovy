/*
  Copyright © 2018 Booz Allen Hamilton. All Rights Reserved.
  This software package is licensed under the Booz Allen Public License. The license can be found in the License file or at http://boozallen.github.io/licenses/bapl
*/

package libraries.docker.steps

/*
  returns an of the images that are built by this pipeline run.
  each image in the array is a hashmap with fields:
    registry: image registry
    repo: repo name
    tag: image tag
    context: directory context for docker build

  a docker build command would then be:
    docker build img.context -t img.registry/img.repo:img.tag
*/
def call(){

    def (image_reg) = get_registry_info() // config.registry
    def path_prefix = config.repo_path_prefix ? config.repo_path_prefix + "/" : ""

    def build_strategies = [ "docker-compose", "modules", "dockerfile" ]
    if (config.build_strategy)
    if (!(config.build_strategy in build_strategies))
      error "build strategy: ${config.build_strategy} not one of ${build_strategies}"

    def images = []

    switch (config.build_strategy) {
      case "docker-compose":
        error "docker-compose build strategy not implemented yet"
        break
      case "modules":
        findFiles(glob: "*/Dockerfile").collect{ it.path.split("/").first() }.each{ service ->
          images.push([
            registry: image_reg,
            repo: "${path_prefix}${env.REPO_NAME}_${service}".toLowerCase(),
            tag: env.GIT_SHA,
            context: service
          ])
        }
        break
      case "dockerfile": //same as null/default case
      case null:
        images.push([
          registry: image_reg,
          repo: "${path_prefix}${env.REPO_NAME}".toLowerCase(),
          tag: env.GIT_SHA,
          context: "."
        ])
        break
    }

    return images
}
