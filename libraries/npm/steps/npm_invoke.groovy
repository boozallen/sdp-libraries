/*
  Copyright © 2018 Booz Allen Hamilton. All Rights Reserved.
  This software package is licensed under the Booz Allen Public License. The license can be found in the License file or at http://boozallen.github.io/licenses/bapl
*/

package libraries.npm.steps

void call(String stepName, app_env = []) {
    stage("Npm Invoke"){

        // Get config for stepName, fail if stepName is not supported
        (libConfig, appConfig) = this.getConfigs(stepName, config, app_env)

        // Gather, validate and format secrets to pull from credential store
        ArrayList creds = this.formatSecrets(libConfig, appConfig)

        // Gather and set non-secret environment variables
        this.setEnvVars(libConfig, appConfig)

        // run npm command in nvm container
        withCredentials(creds){
            inside_sdp_image "npx:1.0.0", {
                unstash "workspace"
    
                // verify package.json script block has command to run
                def packageJson = readJSON(file: "package.json")
                if(!packageJson?.scripts?.containsKey(env.scriptCommand)) error("stepName $env.scriptCommand not found in package.json scripts")

                sh '''
                    set +x
                    source ~/.bashrc
                    nvm install $npm_version
                    nvm version
                    
                    [[ ! -z "$npm_install" ]] && npm $npm_install  # only run npm install step if npm_install is specified
                    npm run $scriptCommand
                '''
            }
        }
    }
}

ArrayList getConfigs(stepName, config, app_env) {
    LinkedHashMap libConfig = [:]
    LinkedHashMap appConfig = [:]

    switch(stepName){
        case "build": 
            libConfig = config?.build ?: [:]
            appConfig = app_env?.npm?.build ?: [:]
            break
        case "unit_test":
            libConfig = config?.unit_test ?: [:]
            appConfig = app_env?.npm?.unit_test ?: [:]
            break
        default: 
            error("stepName must be \"build\" or \"unit_test\", got \"$stepName\"")
    }

    return [libConfig, appConfig]
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
        error (["Npm Library Validation Errors: "] + errors.collect{ "- ${it}"}).join("\n")
    }
}

ArrayList formatSecrets(libConfig, appConfig) {
    LinkedHashMap libSecrets = libConfig?.env?.secrets ?: [:]
    LinkedHashMap envSecrets = appConfig?.env?.secrets ?: [:]
    LinkedHashMap secrets = libSecrets + envSecrets

    this.validateParameters(secrets)

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
    return creds
}

void setEnvVars(libConfig, appConfig){
    LinkedHashMap libEnv = libConfig?.env?.findAll { it.key != 'secrets' } ?: [:]
    LinkedHashMap appEnv = appConfig?.env?.findAll { it.key != 'secrets' } ?: [:]
    LinkedHashMap envVars = libEnv + appEnv

    env.npm_version = config.npm_version ?: 'lts/*'
    envVars.each {
        env[it.key] = it.value
    }

    env.scriptCommand = libConfig?.script      ?:
                            appConfig?.script  ?: 
                            ""
                                
    env.npm_install = libConfig?.npm_install       ?:
                            appConfig?.npm_install ?: 
                            ""
                                
    if(!["install", "i", "ci", ""].contains(env.npm_install)) error("npm_install must be one of \"install\", \"i\", \"ci\" or \"\"; got \"$npm_install\"")
}