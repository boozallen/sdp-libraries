/*
  Copyright © 2022 Booz Allen Hamilton. All Rights Reserved.
  This software package is licensed under the Booz Allen Public License.
  The license can be found in the License file or at http://boozallen.github.io/licenses/bapl
*/

package libraries.maven.steps

@StepAlias(dynamic = { return config.keySet() })
void call(app_env = [:]) {
    // Get config for step
    LinkedHashMap libStepConfig = config?."${stepContext.name}" ?: [:]
    LinkedHashMap appStepConfig = app_env?.maven?."${stepContext.name}" ?: [:]

    def options = appStepConfig?.options ?:
                  libStepConfig?.options ?:
                  [] as String[]

    def goals = appStepConfig?.goals ?:
                libStepConfig?.goals ?:
                [] as String[]

    def phases = appStepConfig?.phases ?:
                 libStepConfig?.phases ?:
                 [] as String[]

    def artifacts = appStepConfig?.artifacts ?:
                    libStepConfig?.artifacts ?:
                    [] as String[]

    def withMavenParams = appStepConfig?.withMavenParams ?:
                    libStepConfig?.withMavenParams ?:
                    [] as String[]

    // Gather and set non-secret environment variables
    this.setEnvVars(libStepConfig, appStepConfig)

    stage(env.stageName) {
        // Gather, validate and format secrets to pull from credential store
        ArrayList creds = this.formatSecrets(libStepConfig, appStepConfig)

        // run maven command in specified container
        withCredentials(creds) {
            inside_sdp_image "${env.buildContainer}", {
                unstash 'workspace'

                String command = withMavenParams ? '$MVN_CMD ' : 'mvn '
                options.each { value -> command += "${value} " }
                goals.each { value -> command += "${value} " }
                phases.each { value -> command += "${value} " }

                try {
                    if (withMavenParams) {
                      withMaven (withMavenParams) {
                          sh command
                      }
                    } else {
                      sh command
                    }
                }
                catch (any) {
                    throw any
                }
                finally {
                    artifacts.each { artifact ->
                        archiveArtifacts artifacts: artifact, allowEmptyArchive: true
                    }
                }
            }
        }
    }
}

void validateSecrets(secrets) {
    ArrayList errors = []
    secrets.keySet().each { key ->
        def secret = secrets[key]
        println "secret -> ${secret}"
        if (!secret.id) {
            errors << "secret '${key}' must define 'id'"
        }
        switch (secret.type) {
            case 'text':
                if (!secret.name) { errors << "secret '${key}' must define 'name'" }
                break
            case 'usernamePassword':
                if (!secret.usernameVar) { errors << "secret '${key}' must define 'usernameVar'" }
                if (!secret.passwordVar) { errors << "secret '${key}' must define 'passwordVar'" }
                break
            default:
                errors << "secret '${key}': type '${secret.type}' is not defined"
        }
    }

    if (errors) {
        error (['Maven Library Validation Errors: '] + errors.collect { "- ${it}" })?.join("\n")
    }
}

ArrayList formatSecrets(libStepConfig, appStepConfig) {
    LinkedHashMap libSecrets = libStepConfig?.secrets ?: [:]
    LinkedHashMap appSecrets = appStepConfig?.secrets ?: [:]
    LinkedHashMap secrets = libSecrets + appSecrets

    this.validateSecrets(secrets)

    ArrayList creds = [] 
    secrets.keySet().each { key -> 
        def secret = secrets[key]
        switch (secret.type) {
            case 'text':
                creds << string(credentialsId: secret.id, variable: secret.name)
                break
            case 'usernamePassword':
                creds << usernamePassword(credentialsId: secret.id, usernameVariable: secret.usernameVar, passwordVariable: secret.passwordVar)
                break
        }
    }
    return creds
}

void setEnvVars(libStepConfig, appStepConfig) {
    LinkedHashMap libEnv = libStepConfig?.findAll { it.key != 'secrets' } ?: [:]
    LinkedHashMap appEnv = appStepConfig?.findAll { it.key != 'secrets' } ?: [:]
    LinkedHashMap envVars = libEnv + appEnv

    envVars.each {
        env[it.key] = it.value
    }

    // Checking to make sure required fields are present (may be redundant once a library_config.groovy is added)
    ArrayList requiredFields = ['stageName', 'buildContainer']
    String missingRequired = ''
    requiredFields.each { field ->
        if (!envVars.containsKey(field)) {
            missingRequired += "Missing required configuration option: ${field} for step: ${stepContext.name}\n"
        }
    }
    if (missingRequired) {
        error missingRequired
    }
}
