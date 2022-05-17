/*
  Copyright © 2018 Booz Allen Hamilton. All Rights Reserved.
  This software package is licensed under the Booz Allen Public License. The license can be found in the License file or at http://boozallen.github.io/licenses/bapl
*/

package libraries.terraform.steps

void call(app_env){

    LinkedHashMap libSecrets = config.secrets ?: [:]
    LinkedHashMap envSecrets = app_env.terraform?.secrets ?: [:]
    LinkedHashMap secrets = libSecrets + envSecrets
    this.validateParameters(secrets)

    String workingDir = app_env.terraform?.working_directory ?: 
                        config.working_directory ?: "."

    ArrayList creds = [] 
    secrets.keySet().each{ key -> 
        def secret = secrets[key]
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

void validateParameters(secrets){
    ArrayList errors = []
    secrets.keySet().each{ key ->
        def secret = secrets[key]
        println "secret -> ${secret}"
        if(!secret.id){
            errors << "secret '${key}' must define 'id'"
        }
        switch(secret.type){
            case "text": 
                if(!secret.name) errors << "secret '${key}' must define 'name'" 
                break
            case "usernamePassword":
                if(!secret.usernameVar) errors << "secret '${key}' must define 'usernameVar'"
                if(!secret.passwordVar) errors << "secret '${key}' must define 'passwordVar'"
                break
            default: 
                errors << "secret '${key}': type '${secret.type}' is not defined"
        }
    }

    if(errors){
        error (["Terraform Library Validation Errors: "] + errors.collect{ "- ${it}"}).join("\n")
    }

}