/*
  Copyright © 2018 Booz Allen Hamilton. All Rights Reserved.
  This software package is licensed under the Booz Allen Public License. The license can be found in the License file or at http://boozallen.github.io/licenses/bapl
*/

package libraries.sonarqube

public class StaticCodeAnalysisSpec extends JTEPipelineSpecification {
    def StaticCodeAnalysis = null

      public static class DummyException extends RuntimeException {
        public DummyException(String _message) { super (_message); }
      }

    LinkedHashMap minimalUnitTestConfig = [
        unit_test: [
            stepName: "unit_test",
            resultDir: "test"
        ]
    ]   
     

    def setup() {
        explicitlyMockPipelineStep("dotnet_scanner_analysis")
        StaticCodeAnalysis = loadPipelineScriptForStep("sonarqube", "static_code_analysis")   
         explicitlyMockPipelineVariable("out")  
    }

    def "Is jte.libraries.dotnet library loaded?" ()  {    // test definition
    setup:
      boolean jteLibraryLoaded = true 
    when:
      boolean  resultIfLibraryLoaded = jteLibraryLoaded 
    then:
      resultIfLibraryLoaded == true           // implicit assertion
   }

   def "Pipeline Fails When Config Is Undefined" () {
    setup:
        explicitlyMockPipelineStep("scanner_analysis") 
        StaticCodeAnalysis.getBinding().setVariable("config", null)
    when:
        StaticCodeAnalysis() // Run the pipeline step we loaded, with no parameters
    then:
        1 * getPipelineMock("scanner_analysis").call()
        1 * getPipelineMock("scanner_analysis").toString()
   } 

 def "Pipeline has an error caught in try catch block" () {
    setup:
        explicitlyMockPipelineStep("sh")
        explicitlyMockPipelineStep("dotnet_scanner_analysis")
        explicitlyMockPipelineStep("scanner_analysis") 
        getPipelineMock("sh")("echo 'This is for Dummy Test'") >> { throw new DummyException("This is for Dummy Test")}
    when:
        try {
        StaticCodeAnalysis() // Run the pipeline step we loaded, with no parameters
        } catch( DummyException e ) {}
    then:
       // 1 * getPipelineMock("dotnet_scanner_analysis")("ERROR: config is not defined")
        1 * getPipelineMock("scanner_analysis").call()
        1 * getPipelineMock("scanner_analysis").toString()
        //1 * getPipelineMock("sh")( _ as Map )
        1 * getPipelineMock("sh").toString()


}
    
}