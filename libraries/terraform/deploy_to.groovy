/*
  Copyright © 2018 Booz Allen Hamilton. All Rights Reserved.
  This software package is licensed under the Booz Allen Public License. The license can be found in the License file or at http://boozallen.github.io/licenses/bapl
*/

void call(app_env){

    this.validateParameters(app_env)

    String workingDir = app_env.terraform?.working_directory ?: 
                        config.working_directory ?: "."

    ArrayList creds = [] 

    LinkedHashMap libSecrets = config.secrets ?: [:]
    LinkedHashMap envSecrets = app_env.terraform?.secrets ?: [:]

    println libSecrets
    println envSecrets

    (libSecrets + envSecrets).each{ secret -> 
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

void validateParameters(def app_env){
    ArrayList errors = ["Terraform Library Validation Errors: "]
    (config.secrets + app_env.terraform?.secrets).each{ key, secret -> 
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