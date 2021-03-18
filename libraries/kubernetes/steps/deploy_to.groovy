/*
  Copyright © 2018 Booz Allen Hamilton. All Rights Reserved.
  This software package is licensed under the Booz Allen Public License. The license can be found in the License file or at http://boozallen.github.io/licenses/bapl
*/

package libraries.kubernetes.steps

void call(app_env){
  stage "Deploy to ${app_env.long_name}", {
    // validate required parameters

    // configuration repository storing the chart
    def config_repo = app_env.helm_configuration_repository ?:
                      config.helm_configuration_repository  ?:
                      {error "helm_configuration_repository not defined in library config or application environment config"}()

    // jenkins credential ID for user to access config repo
    // definable in library spec or app env spec via "helm_configuration_repository_credential"
    // or - a globally defined github credential at the root of the pipeline config "github_credential"
    def git_cred = app_env.helm_configuration_repository_credential ?:
                   config.helm_configuration_repository_credential  ?:
                   pipelineConfig.github_credential              ?:
                   {error "GitHub Credential For Configuration Repository Not Defined"}()

    def branch = app_env.helm_configuration_repository_branch ?: 
                 config.helm_configuration_repository_branch ?:
                 "main"

    def working_directory = app_env.helm_configuration_repository_start_path ?: 
                            config.helm_configuration_repository_start_path ?:
                            "."

    /*
       k8s credential with kubeconfig 
    */
    def k8s_credential = app_env.k8s_credential ?:
                            config.k8s_credential  ?:
                            {error "Kubernetes Credential Not Defined"}()
    /*
       k8s context
    */
    def k8s_context = app_env.k8s_context ?:
                  config.k8s_context            ?:
                  {error "Kubernetes Context Not Defined"}()

    /*
       helm release name.
       will use "release_name" if present on app env object
       or will use "short_name" if present on app_env object.
       will fail otherwise.
    */
    def release = app_env.release_name ?:
                  app_env.short_name          ?:
                  {error "App Env Must Specify release_name or short_name"}()


    /*
       values file to be used when deploying chart
       can specify per application environment object "app_env.chart_values_file"
       otherwise "values.${app_env.short_name}.yaml" will be present if defined and exists
       otherwise - will fail
    */
    def values_file = app_env.chart_values_file ?:
                      app_env.short_name ? "values.${app_env.short_name}.yaml" :
                      {error "Values File To Use For This Chart Not Defined"}()


    /*
       if this is a merge commit we need to retag the image so that the sha
       referenced in the values file represents the merge commit rather than
       the head of the feature branch.  this is primarily for auditing purposes
       of being able to see all the features based merged based on the image shas.

        NOTE: this puts a dependency on the docker library (or whatever image building library
        is used.  this library must supply a retag method)
    */
    def promote_image = app_env.promote_previous_image != null ? app_env.promote_previous_image :
                        config.promote_previous_image != null ? config.promote_previous_image :
                        true
    if (!(promote_image instanceof Boolean)){
      error "Kubernetes Library expects 'promote_previous_image' configuration to be true or false."
    }

    if (promote_image){
      if (env.FEATURE_SHA){
        retag(env.FEATURE_SHA, env.GIT_SHA)
      }
    } else{
      echo "expecting image was already built"
    }

    withGit url: config_repo, cred: git_cred, branch: branch, {
      inside_sdp_image "helm", { 
        withKubeConfig([credentialsId: k8s_credential , contextName: k8s_context]) {
            dir(working_directory){
                this.update_values_file( values_file, config_repo )
                this.do_release release, values_file
                this.push_config_update values_file
            }
        }
      }
    }
  }
}

void update_values_file(values_file, config_repo){
  if (!fileExists(values_file))
    error "Values File ${values_file} does not exist in ${config_repo}"

  values = readYaml file: values_file
  key = env.REPO_NAME.replaceAll("-","_")
  echo "writing new Git SHA ${env.GIT_SHA} to image_shas.${key} in ${values_file}"
  values.image_shas[key] = env.GIT_SHA
  sh "rm ${values_file}"
  writeYaml file: values_file, data: values

}

void do_release(release, values_file){
  sh "helm upgrade --install -f ${values_file} ${release} ."
}

void push_config_update(values_file){
  echo "updating values file -> ${values_file}"
  git add: values_file
  git commit: "Updating ${values_file} for ${env.REPO_NAME} images"
  git push
}

