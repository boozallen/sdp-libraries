/*
  Copyright © 2018-present Booz Allen Hamilton. All Rights Reserved.
  This software package is licensed under the Booz Allen Public License. The license can be found in the License file or at http://boozallen.github.io/licenses/bapl
*/

/*
    This step runs python unit tests using the pytest framework and generates a
    html report for archiving and junit xml for Jenkins consumption and display. 

    By default, failiing tests will fail the build.  To override this, set 
    enforceSuccess to false in the pytest library configuration. 
*/
void call(){
    boolean enforceSuccess = config.containsKey("enforceSuccess") ? config.enforceSuccess : true 
    stage("unit test: pytest"){
        docker.image("python").inside{
            unstash "workspace" 
            String resultsDir = "pytest-results"
            try{
                sh "pip install pytest pytest-html && pytest --html=${resultsDir}/report.html --junitxml=${resultsDir}/junit.xml"
            }catch(any){
                String message = "error running unit tests." 
                enforceSuccess ? error(message) : warning(message) 
            }finally{
                archiveArtifacts allowEmptyArchive: true, artifacts: "${resultsDir}/**/*"
                junit allowEmptyResults: true, healthScaleFactor: 0.0, testResults: "${resultsDir}/junit.xml"
            }
        }
    }
}