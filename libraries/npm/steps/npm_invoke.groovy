/*
  Copyright © 2018 Booz Allen Hamilton. All Rights Reserved.
  This software package is licensed under the Booz Allen Public License. The license can be found in the License file or at http://boozallen.github.io/licenses/bapl
*/

package libraries.npm.steps

@StepAlias(["source_build", "unit_test", "lint_code"])
void call(app_env = []) {
    String stepName = ""
    LinkedHashMap libStepConfig = [:]
    LinkedHashMap appStepConfig = [:]

    // Get config for stepName, fail if stepName is not supported
    switch(stepContext.name) {
        case "source_build":
            stepName = "NPM Build"
            libStepConfig = config?.source_build ?: [:]
            appStepConfig = app_env?.npm?.source_build ?: [:]
            break
        case "unit_test":
            stepName = "NPM Unit Tests"
            libStepConfig = config?.unit_test ?: [:]
            appStepConfig = app_env?.npm?.unit_test ?: [:]
            break
        case "lint_code":
            stepName = "NPM Lint Code (ESLint)"
            libStepConfig = config?.lint_code ?: [:]
            appStepConfig = app_env?.npm?.lint_code ?: [:]
            break
        default:
            error("stepName must be \"source_build\", \"unit_test\", or \"lint_code\", got \"${stepName}\"")
    }

    stage(stepName) {
        // Gather, validate and format secrets to pull from credential store
        ArrayList creds = this.formatSecrets(libStepConfig, appStepConfig)

        // Gather and set non-secret environment variables
        this.setEnvVars(libStepConfig, appStepConfig, config, app_env, stepName)

        // run npm command in nvm container
        withCredentials(creds) {
            inside_sdp_image "nvm:1.0.0", {
                unstash "workspace"

                // verify package.json script block has command to run
                def packageJson = readJSON(file: "package.json")
                if (!packageJson?.scripts?.containsKey(env.scriptCommand)) error("scriptCommand: '$env.scriptCommand' not found in package.json scripts")
                
                if (env.npm_install != "skip") {

                    withCredentials([string(credentialsId: 'bah-npm-registry', variable: 'AUTH_TOKEN')]) {
                        if (env.npm_private_repo_name != "skip") {
                            sh '''
                                set +x
                                npm config set $npm_private_repo_name $npm_private_repo_url
                                npm config set $npm_private_repo_auth $AUTH_TOKEN
                            '''
                        }
                    }
                    sh '''
                        set +x
                        echo 'running with install'
                        npm $npm_install
                        npm run $scriptCommand
                    '''
                    stash name: 'test-results', excludes: "**/node_modules/**"
                }
                else {
                    sh '''
                        set +x
                        echo 'running without install'
                        npm run $scriptCommand
                    '''
                }

                // archive generated reports
                ArrayList reports = [
                    "eslint-report.json",
                    "eslint-report.html",
                    "eslint-report.xml",
                    "coverage/lcov.info"
                ]
                reports.each{ report ->
                    try {
                        if (fileExists(report)) { archiveArtifacts artifacts: "${report}" }
                    } catch(any) {
                        println "Error archiving expected artifact: ${report}"
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

void setEnvVars(libStepConfig, appStepConfig, config, app_env, stepName) {
    LinkedHashMap libEnv = libStepConfig?.env?.findAll { it.key != 'secrets' } ?: [:]
    LinkedHashMap appEnv = appStepConfig?.env?.findAll { it.key != 'secrets' } ?: [:]
    LinkedHashMap envVars = libEnv + appEnv

    envVars.each {
        env[it.key] = it.value
    }

    env.node_version = app_env?.npm?.node_version ?: 
                       config?.node_version  ?:
                       'lts/*'

    env.npm_private_repo_name = appStepConfig?.npm_private_repo_name ?:
                                libStepConfig?.npm_private_repo_name ?: 
                                "skip"

    env.npm_private_repo_url = appStepConfig?.npm_private_repo_url ?:
                               libStepConfig?.npm_private_repo_url

    env.npm_private_repo_auth = appStepConfig?.npm_private_repo_auth ?:
                                libStepConfig?.npm_private_repo_auth
    
    env.npm_install = appStepConfig?.npm_install ?:
                      libStepConfig?.npm_install ?:
                      "ci"

    env.scriptCommand = appStepConfig?.script ?:
                        libStepConfig?.script ?:
                        stepName == "NPM Build" ? "build" : 
                        stepName == "NPM Unit Tests" ? "test" :
                        "lint"

    if (!["install", "i", "ci", "skip"].contains(env.npm_install)) 
        error("npm_install must be one of \"install\", \"i\", \"ci\" or \"skip\"; got \"$env.npm_install\"")
}
