/*
  Copyright © 2018 Booz Allen Hamilton. All Rights Reserved.
  This software package is licensed under the Booz Allen Public License. The license can be found in the License file or at http://boozallen.github.io/licenses/bapl
*/

package libraries.npm.steps

@StepAlias(dynamic = { return config.keySet() })
void call(app_env = [:]) {
    // Get config for step
    LinkedHashMap libStepConfig = config?."${stepContext.name}" ?: [:]
    LinkedHashMap appStepConfig = app_env?.npm?."${stepContext.name}" ?: [:]

    String stageName = appStepConfig?.stageName ?:
                       libStepConfig?.stageName ?:
                       null

    if (!stageName) {
        error("No stage name found for step: " + stepContext.name)
    }

    def artifacts = appStepConfig?.artifacts ?:
                    libStepConfig?.artifacts ?:
                    [] as String[]

    stage(stageName) {
        // Gather, validate and format secrets to pull from credential store
        ArrayList creds = this.formatSecrets(libStepConfig, appStepConfig)

        // Gather and set non-secret environment variables
        this.setEnvVars(libStepConfig, appStepConfig, config, app_env)

        // run npm command in nvm container
        withCredentials(creds) {
            inside_sdp_image "nvm:1.0.0", {
                unstash "workspace"

                // verify package.json script block has command to run
                def packageJson = readJSON(file: "package.json")
                if (!packageJson?.scripts?.containsKey(env.scriptCommand)) {
                    error("script: '$env.scriptCommand' not found in package.json scripts")
                }
                
                try {
                    if (env.npmInstall != "skip") {
                        // run script command after installing dependencies
                        sh '''
                            set +x
                            source ~/.bashrc
                            nvm install $node_version
                            nvm version

                            echo 'Running with NPM install'
                            npm $npmInstall
                            npm run $scriptCommand
                        '''
                    }
                    else {
                        // run script command without installing dependencies
                        sh '''
                            set +x
                            source ~/.bashrc
                            nvm install $node_version
                            nvm version

                            echo 'Running without NPM install'
                            npm run $scriptCommand
                        '''
                    }
                }
                catch (any) {
                    throw any
                }
                finally {
                    // archive artifacts
                    artifacts.each{ artifact ->
                        archiveArtifacts artifacts: artifact, allowEmptyArchive: true
                    }

                    // check if using ESLint plugin
                    def usingEslintPlugin = appStepConfig?.useEslintPlugin ?:
                                            libStepConfig?.useEslintPlugin ?:
                                            false

                    if (usingEslintPlugin) {
                        evaluate "recordIssues enabledForFailure: true, tool: esLint(pattern: 'eslint-report.xml')"
                    }
                }
            }
        }
    }
}

void validateSecrets(secrets) {
    ArrayList errors = []
    secrets.keySet().each{ key ->
        def secret = secrets[key]
        println "secret -> ${secret}"
        if (!secret.id) {
            errors << "secret '${key}' must define 'id'"
        }
        switch(secret.type) {
            case "text":
                if (!secret.name) errors << "secret '${key}' must define 'name'" 
                break
            case "usernamePassword":
                if (!secret.usernameVar) errors << "secret '${key}' must define 'usernameVar'"
                if (!secret.passwordVar) errors << "secret '${key}' must define 'passwordVar'"
                break
            default:
                errors << "secret '${key}': type '${secret.type}' is not defined"
        }
    }

    if (errors) {
        error (["NPM Library Validation Errors: "] + errors.collect{ "- ${it}"})?.join("\n")
    }
}

ArrayList formatSecrets(libStepConfig, appStepConfig) {
    LinkedHashMap libSecrets = libStepConfig?.env?.secrets ?: [:]
    LinkedHashMap envSecrets = appStepConfig?.env?.secrets ?: [:]
    LinkedHashMap secrets = libSecrets + envSecrets

    this.validateSecrets(secrets)

    ArrayList creds = [] 
    secrets.keySet().each{ key -> 
        def secret = secrets[key]
        switch(secret.type) {
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

void setEnvVars(libStepConfig, appStepConfig, config, app_env) {
    LinkedHashMap libEnv = libStepConfig?.env?.findAll { it.key != 'secrets' } ?: [:]
    LinkedHashMap appEnv = appStepConfig?.env?.findAll { it.key != 'secrets' } ?: [:]
    LinkedHashMap envVars = libEnv + appEnv

    envVars.each {
        env[it.key] = it.value
    }

    env.node_version = app_env?.npm?.node_version ?: 
                       config?.node_version ?:
                       'lts/*'
    
    env.npmInstall = appStepConfig?.npmInstall ?:
                     libStepConfig?.npmInstall ?:
                     "ci"

    if (!["install", "i", "ci", "skip"].contains(env.npmInstall)) {
        error("npmInstall must be one of \"install\", \"i\", \"ci\" or \"skip\"; got \"$env.npmInstall\"")
    }

    env.scriptCommand = appStepConfig?.script ?:
                        libStepConfig?.script ?:
                        null

    if (!env.scriptCommand) {
        error("No script command found for step: " + stepContext.name)
    }
}
