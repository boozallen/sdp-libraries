/*
  Copyright © 2018 Booz Allen Hamilton. All Rights Reserved.
  This software package is licensed under the Booz Allen Public License. The license can be found in the License file or at http://boozallen.github.io/licenses/bapl
*/

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
                   pipeline_config().github_credential              ?:
                   {error "GitHub Credential For Configuration Repository Not Defined"}()

    /*
       tiller namespace for this repository.
       can be specific in library spec or per application environment as "tiller_namespace"
    */
    def tiller_namespace = app_env.tiller_namespace ?:
                           config.tiller_namespace  ?:
                           {error "Tiller Namespace Not Defined"}()

    /*
       Jenkins credential for tiller (typically the service account running the tiller server).
       can be specific in library spec or per application environment.
    */
    def tiller_credential = app_env.tiller_credential ?:
                            config.tiller_credential  ?:
                            {error "Tiller Credential Not Defined"}()
    /*
       ocp url
       can be specific in library spec as "url"
       or per application environment as "openshift_url"
    */
    def ocp_url = app_env.openshift_url ?:
                  config.url            ?:
                  {error "OpenShift URL Not Defined"}()

    /*
       helm release name.
       will use "tiller_release_name" if present on app env object
       or will use "short_name" if present on app_env object.
       will fail otherwise.
    */
    def release = app_env.tiller_release_name ?:
                  app_env.short_name          ?:
                  {error "App Env Must Specify tiller_release_name or short_name"}()


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
    if (env.FEATURE_SHA) retag(env.FEATURE_SHA, env.GIT_SHA)

    withGit url: config_repo, cred: git_cred, {
      inside_sdp_image "openshift_helm", {
        withCredentials([usernamePassword(credentialsId: tiller_credential, passwordVariable: 'token', usernameVariable: 'user')]) {
          withEnv(["TILLER_NAMESPACE=${tiller_namespace}"]) {
            this.update_values_file( values_file, config_repo )
            this.oc_login ocp_url, token
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
  def chart_doesnt_exist = sh(returnStatus: true, script: "helm history --max 1 ${release}")
  if (chart_doesnt_exist){
    sh "helm install . -n ${release} -f ${values_file}"
  }else{
    sh "helm upgrade ${release} . -f ${values_file}"
  }
}

void oc_login(ocp_url, token){
  try {
    echo "Trying to log in via token..."
    sh "oc login --insecure-skip-tls-verify ${ocp_url} --token=${token} > /dev/null"
  } catch (any){
    echo "Trying to log in via user/pass..."
    sh "oc login --insecure-skip-tls-verify ${ocp_url} -u ${user} -p ${token} > /dev/null"
  }
}

void push_config_update(values_file){
  echo "updating values file -> ${values_file}"
  git add: values_file
  git commit: "Updating ${values_file} for ${env.REPO_NAME} images"
  git push
}
