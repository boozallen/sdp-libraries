/*
  Copyright © 2018 Booz Allen Hamilton. All Rights Reserved.
  This software package is licensed under the Booz Allen Public License. The license can be found in the License file or at http://boozallen.github.io/licenses/bapl
*/

void deploy_to(def app_env){

    String workingDir = app_env.terraform?.working_directory ?: 
                        config.working_directory ?: "."

    ArrayList creds = [] 
    (config.secrets + app_env.terraform?.secrets).each{ secret -> 
        switch(secret.type){
            case "text": 
                creds << string(credentialsId: secret.id, variable: secret.name)
                break
            case "usernamePassword":
                creds << usernamePassword(credentialsId: secret.id, usernameVariable: secret.usernameVar, passwordVariable: secret.passwordVar)
                break
        }
    }

    inside_sdp_image "terraform", {
        unstash "workspace"
        if(!fileExists(workingDir)){
            error "specified working directory '${workingDir}' does not exist"
        }
        dir(workingDir){
            withCredentials(creds){
                sh "terraform init -plugin-dir=/plugins -input=false"
                sh "terraform apply -auto-approve -input=false"
            }
        }
    }
}