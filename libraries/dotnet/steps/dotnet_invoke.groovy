/*
  Copyright © 2018 Booz Allen Hamilton. All Rights Reserved.
  This software package is licensed under the Booz Allen Public License. The license can be found in the License file or at http://boozallen.github.io/licenses/bapl
*/


@StepAlias(["source_build", "unit_test"])
void call() {
    String stepName = ""

    switch(stepContext.name) {
        case "source_build":
            stepName = "DotNet Build"
            outDir = config.source_build.outDir ?: "bin"
            break
        case "unit_test":
            stepName = "DotNet Unit Test"
            resultDir = config.unit_test.resultDir ?: "coverage"
            break
        default:
                error("stepName must be \"source_build\" or \"unit_test\" got \"${stepName}\"") //doesnt look right
    }

    stage(stepName) {

        // run dotnet command in dotnet container
		docker.withRegistry("https://registry.uip.sh/", "registry-creds") {
            // need to publish dotnet image to sdp-images prior to swicting to inside sdp image
      	    docker.image("registry.uip.sh/toolkit/dotnet-sdk-builder:latest").inside{

                unstash "workspace"

                if(stepName == "DotNet Build") {
                                   
                    try{
                    sh "dotnet publish -c release -o ${outDir}"
                    }
                    catch (any){
                        throw any
                    }
                    finally{
                    archiveArtifacts artifacts: "${outDir}/*.*, allowEmptyArchive: true"
                    }
                }
                
                else {
                    

                    //Execute dotnet tests and output to coverage directory
                    try{
                    sh "rm -drf ${resultDir}"
                    sh "dotnet test --collect:'XPlat Code Coverage' --results-directory  --logger trx" //${resultDir}
                    }

                    catch (any){
                        throw(any)
                    }
                    finally{
                    archiveArtifacts artifacts: "${resultDir}/**/*, allowEmptyArchive: true"
                    }

                stash "workspace"
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
        error (["DotNet Library Validation Errors: "] + errors.collect{ "- ${it}"})?.join("\n")
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

void setEnvVars(libStepConfig, appStepConfig) {
    LinkedHashMap libEnv = libStepConfig?.env?.findAll { it.key != 'secrets' } ?: [:]
    LinkedHashMap appEnv = appStepConfig?.env?.findAll { it.key != 'secrets' } ?: [:]
    LinkedHashMap envVars = libEnv + appEnv

    envVars.each {
        env[it.key] = it.value
    }
}
