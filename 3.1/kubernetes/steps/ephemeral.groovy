/*
Copyright © 2018 Booz Allen Hamilton. All Rights Reserved.
This software package is licensed under the Booz Allen Public License. The license can be found in the License file or at http://boozallen.github.io/licenses/bapl
*/

package libraries.kubernetes.steps

void call(app_env, Closure body){
  stage "Creating Ephemeral Environment", {  

    // configuration repository storing the chart
    def config_repo = app_env.helm_configuration_repository ?:
                      config.helm_configuration_repository  ?:
                      {error "helm_configuration_repository not defined in library config or application environment config"}()

    // jenkins credential ID for user to access config repo
    // definable in library spec or app env spec via "helm_configuration_repository_credential"
    // or - a globally defined github credential at the root of the pipeline config "github_credential"
    def git_cred = app_env.helm_configuration_repository_credential ?:
                   config.helm_configuration_repository_credential  ?:
                   pipelineConfig.github_credential ?:
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
                         {error "k8s Credential Not Defined"}()
    /*
      k8s_context
    */
    def k8s_context = app_env.k8s_context ?:
                      config.k8s_context ?:
                      {error "k8s_context Not Defined"}()

    /*
    values file to be used when deploying chart
    can specify per application environment object "app_env.chart_values_file"
    otherwise "values.${app_env.short_name}.yaml" will be present if defined and exists
    otherwise - will fail
    */
    def values_file = app_env.chart_values_file ?:
                      app_env.short_name ? "values.${app_env.short_name}.yaml" :
                      {error "Values File To Use For This Chart Not Defined"}()

    def image_repo_project = config.image_repository_project ?:
                             {error "You must define image_repository_project where images are pushed" }()

    withGit url: config_repo, cred: git_cred, branch: branch, {
        dir(working_directory){
            def project
            def release_env = [:]
            this.update_values_file values_file
            timeout 60, {
                try {
                inside_sdp_image "helm", {
                    withKubeConfig([credentialsId: k8s_credential , contextName: k8s_context]) {
                    project = this.prep_project image_repo_project
                    release_env = this.do_release project, values_file
                    }
                }
                withEnv(release_env.collect{k,v -> "${k}=${v}"}) {
                    body()
                }
                } catch (any) {
                throw any
                } finally{
                inside_sdp_image "helm", {
                    withKubeConfig([credentialsId: k8s_credential , contextName: k8s_context]) {
                    this.cleanup project
                    }
                }
                }
            }
        }
    }
  }
}

void update_values_file(values_file){
  if (!fileExists(values_file))
    error "Values File ${values_file} does not exist in the given Helm configuration repo"

  values = readYaml file: values_file
  repo = env.REPO_NAME.replaceAll("-","_")
  echo "writing new Git SHA ${env.GIT_SHA} to image_shas.${repo} in ${values_file}"
  values.image_shas[repo] = env.GIT_SHA
  values.is_ephemeral = true
  sh "rm ${values_file}"
  writeYaml file: values_file, data: values
}

def prep_project(image_repo_project){
  def name = (env.REPO_NAME + '-' + env.GIT_SHA).replaceAll("_","-").replaceAll("/","-").replaceAll("[^a-zA-Z0-9-.]", "").take(53)
  echo "Ephemeral Environment Name: ${name}"
  try {
    sh "kubectl create namespace  ${name}"
  }catch(any){
    sh "kubectl delete namespace ${name} || true"
    throw any
  }
  return name
}

def do_release(project, values_file){
  def helm_output = sh script: "helm upgrade --install  --namespace ${project} ${project} . -f ${values_file} --wait", returnStdout: true

  echo helm_output

  def start = false
  def release_env = [:]
  helm_output.split("\n").each{ line ->
    if (start){
      def split = line.split(":",2)
      if(split.size().equals(2)){
        release_env[split.getAt(0)] = split.getAt(1)
      }
    }
    if (line.trim().equals("ENV:")) start = true
    if (!line) start = false
  }
  sleep 45
  return release_env
}

void cleanup(project){
  sh "helm del --purge ${project}  || true"
  sh "kubectl delete namespace ${project} || true"
}
