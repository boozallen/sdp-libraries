/*
  Copyright © 2021 Booz Allen Hamilton. All Rights Reserved.
  This software package is licensed under the Booz Allen Public License. 
  The license can be found in the License file or at http://boozallen.github.io/licenses/bapl
*/

package libraries.sonarqube.steps

import jenkins.model.Jenkins
import com.cloudbees.plugins.credentials.Credentials
import com.cloudbees.plugins.credentials.CredentialsProvider
import com.cloudbees.plugins.credentials.impl.UsernamePasswordCredentialsImpl
import org.jenkinsci.plugins.plaincredentials.impl.StringCredentialsImpl
import hudson.plugins.sonar.SonarGlobalConfiguration

/* Shared Sonarqube code */

def execute(LinkedHashMap defaults, String sdp_image, Closure scan){

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
        inside_sdp_image sdp_image, {
            withCredentials(determineCredentialType(cred_id)) {
                withSonarQubeEnv(installation_name){
                    // fetch the source code 
                    unstash "workspace"
                    
                    /*
                      checks for the existence of a stash called "test-results"
                      which may have been created by previous steps to store results
                      that sonarqube will consume
                    */
                    unstashList.each{ l ->
                        try{ unstash l }catch(ex){}
                    }
                    
                    /* Run scanner */
                    scan()
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