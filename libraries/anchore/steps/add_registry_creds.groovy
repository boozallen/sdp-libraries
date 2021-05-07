/*
  Copyright © 2018 Booz Allen Hamilton. All Rights Reserved.
  This software package is licensed under the Booz Allen Public License. The license can be found in the License file or at http://boozallen.github.io/licenses/bapl
*/

package libraries.anchore.steps

import groovy.json.*

def generate_post_data(registry, registry_user, registry_password){
    return JsonOutput.toJson([
            registry: "$registry",
            registry_name: "$registry",
            registry_type: 'docker_v2',
            registry_user: "$registry_user",
            registry_verify: true,
            registry_pass: "$registry_password"
        ])
}

void call(app_env){
    stage("Ensure Anchore has Docker Creds"){

        /*
        Docker registry creds for pulling helm image (has kubectl)
        */
        def docker_registry_credential_id = app_env.docker_registry_credential_id ?: 
                                                config.docker_registry_credential_id ?: 
                                                "docker_registry"
                                                
        def docker_registry_name = app_env.docker_registry_name ?: 
                                        config.docker_registry_name ?: 
                                        ""

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
                            config.k8s_context ?:
                            {error "Kubernetes Context Not Defined"}()

        /*
        Anchore engine url
        */
        env.ANCHORE_ENGINE_URL = app_env.anchore_engine_url ?: 
                                    config.anchore_engine_url ?: 
                                    {error "Anchore Engine Url Not Defined"}()

        node{

            try {
                withCredentials([
                    usernamePassword(credentialsId: config.cred, usernameVariable: 'ANCHORE_USERNAME', passwordVariable: "ANCHORE_PASSWORD"),
                    usernamePassword(credentialsId: docker_registry_credential_id, usernameVariable: 'REGISTRY_USERNAME', passwordVariable: "REGISTRY_PASSWORD")
                ]) {
                    inside_sdp_image "helm", { 
                        withKubeConfig([credentialsId: k8s_credential , contextName: k8s_context]) {
                            sh "curl --header \"Content-Type: application/json\" -X POST -u $ANCHORE_USERNAME:$ANCHORE_PASSWORD $ANCHORE_ENGINE_URL/registries -d '${generate_post_data(docker_registry_name, REGISTRY_USERNAME, REGISTRY_PASSWORD)}'"

                            sh 'curl -u $ANCHORE_USERNAME:$ANCHORE_PASSWORD $ANCHORE_ENGINE_URL/registries'
                        }
                    }
                }
            } catch (e) {
                println "anchore add_registry_creds step failed: $e"
            }
        }
    }
}