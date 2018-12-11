/*
  Copyright © 2018 Booz Allen Hamilton. All Rights Reserved.
  This software package is licensed under the Booz Allen Public License. The license can be found in the License file or at http://boozallen.github.io/licenses/bapl
*/

void call(app_env, Closure body){
    stage "Creating Ephemeral Environment", {
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

        withGit url: config_repo, cred: git_cred, {
            withCredentials([usernamePassword(credentialsId: tiller_credential, passwordVariable: 'token', usernameVariable: 'user')]) {
                withEnv(["TILLER_NAMESPACE=${tiller_namespace}"]) {
                    def project
                    def release_env = [:]
                    this.update_values_file values_file
                    timeout 60, {
                        try {
                            inside_sdp_image "openshift_helm", {
                                this.oc_login ocp_url, token
                                project = this.prep_project image_repo_project
                                release_env = this.do_release project, values_file
                            }
                            withEnv(release_env.collect{k,v -> "${k}=${v}"}) {
                                body()
                            }
                        } catch (any) {
                            throw any
                        } finally{
                            inside_sdp_image "openshift_helm", {
                                this.oc_login ocp_url, token
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
    def name = (1..10).collect([]){ ("a".."z").getAt(new Random().nextInt(26) % 26) }.join()
    echo "Ephemeral Environment Name: ${name}"
    def projectDisplayName = "${env.REPO_NAME}: ${env.JOB_NAME.split("/").last()}, Build: ${env.BUILD_NUMBER}"
    echo "Project Display Name: ${projectDisplayName}"
    try {
        sh "oc new-project ${name} --display-name='${projectDisplayName}'"
        sh "oc process -p TILLER_NAMESPACE=${env.TILLER_NAMESPACE} -p PROJECT=${name} tiller-project-role -n openshift | oc apply -f -"
        sh "oc adm policy add-role-to-user system:image-puller system:serviceaccount:${name}:default -n ${image_repo_project}"
    }catch(any){
        sh "oc delete project ${name}"
        throw any
    }
    return name
}

def do_release(project, values_file){
    def helm_output = sh script: "helm install . -n ${project} -f ${values_file} --wait",
                         returnStdout: true

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

void oc_login(ocp_url, token){
    try {
        echo "Trying to log in via token.."
        sh "oc login --insecure-skip-tls-verify ${ocp_url} --token=${token} > /dev/null"
    } catch (any){
        echo "Trying to log in via user/pass.."
        sh "oc login --insecure-skip-tls-verify ${ocp_url} -u ${user} -p ${token} > /dev/null"
    }
}

void cleanup(project){
    sh "helm del --purge ${project}  || true"
    sh "oc delete project ${project} || true"
}
