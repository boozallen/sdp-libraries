/*
  Copyright © 2021 Booz Allen Hamilton. All Rights Reserved.
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
        unstash: [ "TestResults" ],
        cli_parameters: [],
        test_output_dir: "TestResults",
        coverage_settings_file: "coverlet.runsettings"
    ]

    sonarqube.execute(defaults, "dotnet-sonar-scanner", {

        // dotnet sonarscanner does not use properties file. Try to get project key from env
        String projectKey = config.project_key ?: '';
        if(projectKey.isEmpty()){
            if ((env.ORG_NAME ?: '').isEmpty()){
                projectKey = "${env.REPO_NAME}"
            } else {
                projectKey = "${env.ORG_NAME}:${env.REPO_NAME}"
            }
        }

        String testOutputDir = config.test_output_dir ?: defaults.test_output_dir
        String coverageSettingsFile = config.coverage_settings_file ?: defaults.coverage_settings_file

        // build out the command to execute
        ArrayList beginCommand = [ "dotnet sonarscanner begin /k:${projectKey} /n:${env.REPO_NAME} /d:sonar.verbose=true /d:sonar.cs.opencover.reportsPaths='${testOutputDir}/**/coverage.opencover.xml' /d:sonar.cs.vstest.reportsPaths='${testOutputDir}/*.trx'" ]
        ArrayList endCommand = [ "dotnet sonarscanner end" ]

        /*
            if an API token was used, only provide /d:sonar.login
            if a username/password was used, provide both /d:sonar.login and /d:sonar.password
            
            because of how determineCredentialType() works - the env var sq_user will 
            only be present if a username/password was provided.
        */
        if(env.sq_user){
            beginCommand << "/d:sonar.login='${env.sq_user}' /d:sonar.password='${env.sq_token}'"
            endCommand << "/d:sonar.login='${env.sq_user}' /d:sonar.password='${env.sq_token}'"
        } else {
            beginCommand << "/d:sonar.login='${env.sq_token}'"
            endCommand << "/d:sonar.login='${env.sq_token}'"
        }

        // join user provided params
        beginCommand << (config.cli_parameters ?: defaults.cli_parameters)

        sh beginCommand.flatten().join(" ")
        sh "dotnet build"
        sh "rm -drf ${env.WORKSPACE}/${testOutputDir}"
        sh "dotnet test --settings ${coverageSettingsFile} --results-directory ${env.WORKSPACE}/${testOutputDir} --logger trx"
        sh endCommand.flatten().join(" ")
    })
}