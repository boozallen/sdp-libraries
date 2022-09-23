/*
  Copyright © 2022 Booz Allen Hamilton. All Rights Reserved.
  This software package is licensed under the Booz Allen Public License. The license can be found in the License file or at http://boozallen.github.io/licenses/bapl
*/

package libraries.yarn.steps

@StepAlias(dynamic = { return config.keySet() })
void call(app_env = [:]) {
    // Get config for step
    LinkedHashMap libStepConfig = config?."${stepContext.name}" ?: [:]
    LinkedHashMap appStepConfig = app_env?.yarn?."${stepContext.name}" ?: [:]

    String nvmContainer = config?.nvm_container ?: "nvm:1.0.0"

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

        // run Yarn command in nvm container
        withCredentials(creds) {
            inside_sdp_image(nvmContainer) {
                unstash "workspace"

                // verify package.json script block has command to run
                def packageJson = readJSON(file: "package.json")
                if (!packageJson?.scripts?.containsKey(env.scriptCommand)) {
                    error("script: '$env.scriptCommand' not found in package.json scripts")
                }
                
                try {
                    if (env.yarnInstall != "skip") {
                        // run script command after installing dependencies
                        sh '''
                            set +x
                            source ~/.bashrc
                            nvm install $node_version
                            nvm version

                            npm install -g yarn@$yarn_version
                            yarn --version

                            echo 'Running with Yarn install'
                            yarn $yarnInstall
                            yarn $scriptCommand
                        '''
                    }
                    else {
                        // run script command without installing dependencies
                        sh '''
                            set +x
                            source ~/.bashrc
                            nvm install $node_version
                            nvm version

                            npm install -g yarn@$yarn_version
                            yarn --version

                            echo 'Running without Yarn install'
                            yarn $scriptCommand
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
                        recordIssues enabledForFailure: true, tool: esLint(pattern: 'eslint-report.xml')
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
        error (["Yarn Library Validation Errors: "] + errors.collect{ "- ${it}"})?.join("\n")
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

    env.node_version = app_env?.yarn?.node_version ?: 
                       config?.node_version ?:
                       'lts/*'
    
    env.yarn_version = app_env?.yarn?.yarn_version ?: 
                       config?.yarn_version ?:
                       'latest'
    
    String yarnInstall = appStepConfig?.yarnInstall ?:
                         libStepConfig?.yarnInstall ?:
                         "frozen-lockfile"

    if (!["install", "frozen-lockfile", "skip"].contains(yarnInstall)) {
        error("yarnInstall must be one of \"install\", \"frozen-lockfile\" or \"skip\"; got \"$yarnInstall\"")
    }

    env.yarnInstall = (yarnInstall == "frozen-lockfile")
                      ? "install --frozen-lockfile"
                      : yarnInstall

    env.scriptCommand = appStepConfig?.script ?:
                        libStepConfig?.script ?:
                        null

    if (!env.scriptCommand) {
        error("No script command found for step: " + stepContext.name)
    }
}
