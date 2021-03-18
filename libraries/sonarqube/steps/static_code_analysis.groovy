/*
  Copyright © 2018 Booz Allen Hamilton. All Rights Reserved.
  This software package is licensed under the Booz Allen Public License. The license can be found in the License file or at http://boozallen.github.io/licenses/bapl
*/

package libraries.sonarqube.steps

import jenkins.model.Jenkins
import com.cloudbees.plugins.credentials.Credentials
import com.cloudbees.plugins.credentials.CredentialsProvider
import com.cloudbees.plugins.credentials.impl.UsernamePasswordCredentialsImpl
import org.jenkinsci.plugins.plaincredentials.impl.StringCredentialsImpl
import hudson.plugins.sonar.SonarGlobalConfiguration

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

    // whether or not to wait for the quality gate 
    Boolean wait = defaults.wait_for_quality_gate 
    if(config.containsKey("wait_for_quality_gate")){
        wait = config.wait_for_quality_gate
    }
    // whether or not to enforce the SQ QG
    Boolean enforce = defaults.enforce_quality_gate 
    if(config.containsKey("enforce_quality_gate")){
        enforce = config.enforce_quality_gate
    }

    // name of installation to use, as configured in Manage Jenkins > Configure System > SonarQube Installations
    String installation_name = config.installation_name ?: defaults.installation_name
    validateInstallationExists(installation_name)

    // credential ID for SonarQube Auth
    String cred_id = config.credential_id ?: fetchCredentialFromInstallation(installation_name) ?: defaults.credential_id

    // purely aesthetic.  the name of the "Stage" for this task. 
    String stage_display_name = config.stage_display_name ?: defaults.stage_display_name

    // timeout settings
    def timeout_duration = config.timeout_duration ?: defaults.timeout_duration
    String timeout_unit = config.timeout_unit ?: defaults.timeout_unit

    ArrayList unstashList = config.unstash ?: defaults.unstash

    stage(stage_display_name){
        inside_sdp_image "sonar-scanner", {
            withCredentials(determineCredentialType(cred_id)) {
                withSonarQubeEnv(installation_name){
                    // fetch the source code 
                    unstash "workspace"
                    
                    /*
                      checks for the existence of a stash called "test-results"
                      which may have been created by previous steps to store results
                      that sonarqube will consume
                    */
                    unstashList.each{ ->
                        try{ unstash it }catch(ex){}
                    }
                    
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

                }
                
                if(wait){
                    timeout(time: timeout_duration, unit: timeout_unit) {
                        def qg = waitForQualityGate()
                        if (qg.status != 'OK' && enforce) {
                            error "Pipeline aborted due to quality gate failure: ${qg.status}"
                        }
                    }
                }
            }
        }
    }
}

def determineCredentialType(String cred_id){
    def allCreds = CredentialsProvider.lookupCredentials(Credentials, Jenkins.get(),null, null)
    def cred = allCreds.find{ it.id.equals(cred_id) } 

    if(cred == null){
        error "SonarQube: Credential with id '${cred_id}' does not exist."
    }

    if(!(cred.getClass() in [UsernamePasswordCredentialsImpl, StringCredentialsImpl])){
        error """
        SonarQube: Credential with id '${cred_id}' must be either: 
          1. a valid username/password for SonarQube
          2. a secret text credential storing an API Token. 
        Found credential type: ${cred.getClass()}
        """.trim().stripIndent(8)
    }

    if(cred instanceof UsernamePasswordCredentialsImpl){
        return [ usernamePassword(credentialsId: cred_id, passwordVariable: 'sq_token', usernameVariable: 'sq_user') ]
    }

    if(cred instanceof StringCredentialsImpl){
        return [ string(credentialsId: cred_id, variable: 'sq_token') ] 
    }
}

void validateInstallationExists(installation_name){
    boolean exists = SonarGlobalConfiguration.get().getInstallations().find{
        it.getName() == installation_name
    } as boolean
    if(!exists){
        error "SonarQube: installation '${installation_name}' does not exist"
    }
}

/*
    when not set - this returns an empty string, "" 
    which evaluates to false when used in an elvis operator. 
*/
String fetchCredentialFromInstallation(installation_name){
    String id = SonarGlobalConfiguration.get().getInstallations().find{
        it.getName() == installation_name
    }.getCredentialsId()
    return id
}