/*
  Copyright © 2018 Booz Allen Hamilton. All Rights Reserved.
  This software package is licensed under the Booz Allen Public License. The license can be found in the License file or at http://boozallen.github.io/licenses/bapl
*/

package libraries.sonarqube.steps

def call(){

    // default values for config options
    LinkedHashMap defaults = [
        credential_id: "sonarqube",
        wait_for_quality_gate: true, 
        enforce_quality_gate: true,
        installation_name: "SonarQube",
        timeout_duration: 1,
        timeout_unit: "HOURS",
        stage_display_name: "SonarQube Analysis",
        unstash: [ "test-results" ],
        cli_parameters: []
    ]

    sonarqube.execute(defaults, "sonar-scanner", {
        /*
            creates an empty directory in the event that a value for 
            sonar.java.binaries needs to be provided when the binaries
            are not present during sonarqube analysis
        */
        sh "mkdir -p empty"
        
        // build out the command to execute
        ArrayList command = [ "sonar-scanner -X" ]

        /*
            if an API token was used, only provide -Dsonar.login 
            if a username/password was used, provide both -Dsonar.login and -Dsonar.password
            
            because of how determineCredentialType() works - the env var sq_user will 
            only be present if a username/password was provided.
        */
        if(env.sq_user){
            command << "-Dsonar.login='${env.sq_user}' -Dsonar.password='${env.sq_token}'"
        } else {
            command << "-Dsonar.login='${env.sq_token}'"
        }

        // join user provided params
        command << (config.cli_parameters ?: defaults.cli_parameters)

        sh command.flatten().join(" ")
    })
}