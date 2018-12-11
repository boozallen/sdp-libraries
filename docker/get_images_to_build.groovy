/*
  Copyright © 2018 Booz Allen Hamilton. All Rights Reserved.
  This software package is licensed under the Booz Allen Public License. The license can be found in the License file or at http://boozallen.github.io/licenses/bapl
*/

/*
  returns an of the images that are built by this pipeline run.
  each image in the array is a hashmap with fields:
    repo: base repo for image
    path: path on repo
    tag: image tag
    context: directory context for docker build

  a docker build command would then be:
    docker build img.context -t img.repo/img.path:img.tag
*/
def call(){

    def image_repo = pipelineConfig?.application_image_repository ?:
                    {error "application_image_repository not defined in pipeline config."}()

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
            repo: image_repo,
            path: "${env.REPO_NAME}_${service}",
            context: service,
            tag: env.GIT_SHA
          ])
        }
        break
      case "dockerfile":
      case null:
        images.push([
          repo: image_repo,
          path: env.REPO_NAME,
          context: ".",
          tag: env.GIT_SHA
        ])
        break
    }

    return images
}
