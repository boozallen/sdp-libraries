/*
  Copyright © 2018-present Booz Allen Hamilton. All Rights Reserved.
  This software package is licensed under the Booz Allen Public License. The license can be found in the License file or at http://boozallen.github.io/licenses/bapl
*/

/*
    This step runs python unit tests using the pytest framework and generates a
    html report for archiving and junit xml for Jenkins consumption and display. 

    for full configuration options, refer to docs/modules/pytest/pages/index.adoc

    more configuration options are welcome, please submit a pull request
*/
void call(){
    stage("unit test: pytest"){
        boolean enforceSuccess = config.containsKey("enforceSuccess") ? config.enforceSuccess : true 
        String requirementsFile = config.requirementsFile ?: "requirements.txt"
        docker.image("python:slim").inside{
            unstash "workspace" 
            String resultsDir = "pytest-results"
            try{
                sh "pip install pytest pytest-html" 
                if(fileExists(requirementsFile)){
                    sh "pip install -r ${requirementsFile}"
                }                 
                sh "pytest --html=${resultsDir}/report.html --junitxml=${resultsDir}/junit.xml"
            }catch(any){
                String message = "error running unit tests." 
                enforceSuccess ? error(message) : warning(message)
            }finally{
                archiveArtifacts allowEmptyArchive: true, artifacts: "${resultsDir}/"
                junit allowEmptyResults: true, healthScaleFactor: 0.0, testResults: "${resultsDir}/junit.xml"
            }
        }
    }
}