/*
  Copyright © 2022 Booz Allen Hamilton. All Rights Reserved.
  This software package is licensed under the Booz Allen Public License. The license can be found in the License file or at http://boozallen.github.io/licenses/bapl
*/

package libraries.maven.steps

@StepAlias(dynamic = { return config.keySet() })
void call(app_env = [:]) {

    // Get config for step
    LinkedHashMap libStepConfig = config?."${stepContext.name}" ?: [:]
    LinkedHashMap appStepConfig = app_env?.maven?."${stepContext.name}" ?: [:]

    // Merge the two configs: app_env settings take precendence, and there is no deep copy/sublist merging
    LinkedHashMap fullConfig = libStepConfig + appStepConfig

    // Checking to make sure required fields are present (may be redundant once a library_config.groovy is added)
    ArrayList requiredFields = ["stageName", "buildContainer", "phases"]
    String missingRequired = ""
    requiredFields.each { field ->
        if (!fullConfig.containsKey(field)) {
            missingRequired += "Missing required configuration option: ${field} for step: ${stepContext.name}\n"
        }
    }
    if (missingRequired) {
        error missingRequired
    }

    stage(fullConfig["stageName"]) {
        // Gather, validate and format secrets to pull from credential store
        ArrayList creds = this.formatSecrets(libStepConfig, appStepConfig)

        // run maven command in specified container
        withCredentials(creds) {
            // inside_sdp_image fullConfig["buildContainer"], {
            docker.image(fullConfig["buildContainer"]).inside() {
                unstash "workspace"

                String command = "mvn "
                if (fullConfig["options"]) {
                    fullConfig["options"].each { option -> command += "${option} " }
                }
                if (fullConfig["goals"]) {
                    fullConfig["goals"].each { goal -> command += "${goal} " }
                }
                fullConfig["phases"].each { phase -> command += "${phase} " }

                try {
                    sh command
                }
                catch (any) {
                    throw any
                }
                finally {
                    if (fullConfig.containsKey("artifacts")) {
                        fullConfig["artifacts"].each{ artifact ->
                            archiveArtifacts artifacts: artifact, allowEmptyArchive: true
                        }
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
        error (["Maven Library Validation Errors: "] + errors.collect{ "- ${it}"})?.join("\n")
    }
}

ArrayList formatSecrets(libStepConfig, appStepConfig) {
    LinkedHashMap libSecrets = libStepConfig?.secrets ?: [:]
    LinkedHashMap appSecrets = appStepConfig?.secrets ?: [:]
    LinkedHashMap secrets = libSecrets + appSecrets

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