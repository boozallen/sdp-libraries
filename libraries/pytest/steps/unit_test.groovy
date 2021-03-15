/*
  Copyright © 2018-present Booz Allen Hamilton. All Rights Reserved.
  This software package is licensed under the Booz Allen Public License. The license can be found in the License file or at http://boozallen.github.io/licenses/bapl
*/

package libraries.pytest.steps

/*
    This step runs python unit tests using the pytest framework and generates a
    html report for archiving and junit xml for Jenkins consumption and display. 

    for full configuration options, refer to docs/modules/pytest/pages/index.adoc

    more configuration options are welcome, please submit a pull request
*/
void call(){
    stage("unit test: pytest"){
        boolean enforceSuccess = config.containsKey("enforce_success") ? config.enforce_success : true 
        String requirementsFile = config.requirements_file ?: "requirements.txt"
        inside_sdp_image "pytest", {
            unstash "workspace" 
            String resultsDir = "pytest-results"
            try{
                if(fileExists(requirementsFile)){
                    sh "pip install -r ${requirementsFile}"
                } else if (config.containsKey("requirements_file")){
                    unstable "PyTest: Configured requirements file '${requirementsFile}' does not exist"
                }       
                sh "pytest --html=${resultsDir}/report.html --junitxml=${resultsDir}/junit.xml"
            }catch(any){
                String message = "error running unit tests." 
                enforceSuccess ? error(message) : unstable(message)
            }finally{
                archiveArtifacts allowEmptyArchive: true, artifacts: "${resultsDir}/"
                junit allowEmptyResults: true, healthScaleFactor: 0.0, testResults: "${resultsDir}/junit.xml"
            }
        }
    }
}