/*
  Copyright © 2018 Booz Allen Hamilton. All Rights Reserved.
  This software package is licensed under the Booz Allen Public License. The license can be found in the License file or at http://boozallen.github.io/licenses/bapl
*/

package libraries.npm.steps

void call(String stepName, app_env = []) {
    stage("Npm Invoke"){

        // Get config for stepName, fail if stepName is not supported
        (libStepConfig, appStepConfig) = this.getStepConfigs(stepName, config, app_env)

        // Gather, validate and format secrets to pull from credential store
        ArrayList creds = this.formatSecrets(libStepConfig, appStepConfig)

        // Gather and set non-secret environment variables
        this.setEnvVars(libStepConfig, appStepConfig, config, app_env, stepName)

        // run npm command in nvm container
        withCredentials(creds){
            inside_sdp_image "npx:1.0.0", {
                unstash "workspace"
    
                // verify package.json script block has command to run
                def packageJson = readJSON(file: "package.json")
                if(!packageJson?.scripts?.containsKey(env.scriptCommand)) error("stepName '$env.scriptCommand' not found in package.json scripts")

                if(env.npm_install != "skip") {
                    sh '''
                        set +x
                        source ~/.bashrc
                        nvm install $node_version
                        nvm version
                        
                        npm $npm_install
                        npm run $scriptCommand
                    '''
                } else {
                    sh '''
                        set +x
                        source ~/.bashrc
                        nvm install $node_version
                        nvm version

                        npm run $scriptCommand
                    '''
                }
            }
        }
    }
}

ArrayList getStepConfigs(stepName, config, app_env) {
    LinkedHashMap libStepConfig = [:]
    LinkedHashMap appStepConfig = [:]

    switch(stepName){
        case "build": 
            libStepConfig = config?.build ?: [:]
            appStepConfig = app_env?.npm?.build ?: [:]
            break
        case "unit_test":
            libStepConfig = config?.unit_test ?: [:]
            appStepConfig = app_env?.npm?.unit_test ?: [:]
            break
        default: 
            error("stepName must be \"build\" or \"unit_test\", got \"$stepName\"")
    }

    return [libStepConfig, appStepConfig]
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
        error (["Npm Library Validation Errors: "] + errors.collect{ "- ${it}"})?.join("\n")
    }
}

ArrayList formatSecrets(libStepConfig, appStepConfig) {
    LinkedHashMap libSecrets = libStepConfig?.env?.secrets ?: [:]
    LinkedHashMap envSecrets = appStepConfig?.env?.secrets ?: [:]
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

void setEnvVars(libStepConfig, appStepConfig, config, app_env, stepName){
    LinkedHashMap libEnv = libStepConfig?.env?.findAll { it.key != 'secrets' } ?: [:]
    LinkedHashMap appEnv = appStepConfig?.env?.findAll { it.key != 'secrets' } ?: [:]
    LinkedHashMap envVars = libEnv + appEnv

    envVars.each {
        env[it.key] = it.value
    }

    env.node_version = app_env?.npm?.node_version ?: 
                            config?.node_version  ?:
                            'lts/*'

    env.npm_install = appStepConfig?.npm_install       ?:
                            libStepConfig?.npm_install ?: 
                            "ci"

    env.scriptCommand = appStepConfig?.script             ?:
                            libStepConfig?.script         ?:
                            stepName == "build" ? "build" : 
                            "test"
                                                          
    if(!["install", "i", "ci", "skip"].contains(env.npm_install)) error("npm_install must be one of \"install\", \"i\", \"ci\" or \"skip\"; got \"$env.npm_install\"")
}