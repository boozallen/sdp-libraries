/*
  Copyright © 2018 Booz Allen Hamilton. All Rights Reserved.
  This software package is licensed under the Booz Allen Public License. The license can be found in the License file or at http://boozallen.github.io/licenses/bapl
*/

def call(){

  def image_registry = config.registry ?:
                  {error "application_image_repository not defined in pipeline config."}()

  def path_prefix = config.repo_path_prefix ? config.repo_path_prefix + "/" : ""

  def compose_file = config.compose_file ?:
                     "docker-compose.yml"
  echo "compose file name: ${compose_file}"

  if (fileExists(compose_file) && readYaml(file: compose_file).services.any{ it. getValue().containsKey("build")} ) {
    def docker_compose = readYaml(file: compose_file)

    if (Double.parseDouble(docker_compose.get("version")) < 3.0) {
      error("docker-compose version should be at least 3.0")
    }

    stage("Build Microservice Images"){
      echo "performing image build via docker-compose"

      unstash "workspace"
      sh "git checkout -- ." // reset file permissions changed during stash/unstash
      def build_log = sh(
        script: "docker-compose -f ${compose_file} build",
        returnStdout: true
      )
      (build_log =~ /Successfully tagged.*/).collect{ it.split(" ").last() }.each{ img ->
        def image_name = img.split('/').last().split(':').first()
        sh "docker tag ${img} ${image_registry}/${path_prefix}${image_name}:${env.GIT_SHA}"
        sh "docker push ${image_registry}/${path_prefix}${image_name}:${env.GIT_SHA}"
      }
    }
  }
}
