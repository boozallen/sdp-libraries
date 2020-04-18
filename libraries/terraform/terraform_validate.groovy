@Validate
void call(context){
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