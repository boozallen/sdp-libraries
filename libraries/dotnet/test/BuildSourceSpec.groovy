/*
  Copyright © 2018 Booz Allen Hamilton. All Rights Reserved.
  This software package is licensed under the Booz Allen Public License. The license can be found in the License file or at http://boozallen.github.io/licenses/bapl
*/

package libraries.dotnet

public class BuildSourceSpec extends JTEPipelineSpecification {
    def BuildSource = null
    
    // expect lib to call build_unity() then build_dotnet() if unity_app == true

    // expect lib to call build_dotnet() only if unity_app == false

    LinkedHashMap minimalSourceBuildConfig = [
        source_build: [
            stepName: "source_build",
            outDir: "OutTest"
        ]
    ]
    LinkedHashMap minimalUnitTestConfig = [
        unit_test: [
            stepName: "unit_test",
            resultDir: "test"
        ]
    ]

    def setup() {
        explicitlyMockPipelineStep("inside_sdp_image")
        explicitlyMockPipelineStep("login_to_registry")
        explicitlyMockPipelineStep("build_dotnet")
        explicitlyMockPipelineVariable("workspace")   

        BuildSource = loadPipelineScriptForStep("dotnet", "build_source")    
    }

    // would like to go over this with Conner.
    /*  def  "Lib to print skip if sonarqube lib is loaded" () { 
        setup:  
              BuildSource.getBinding().setVariable("config", [unit_test: [resultDir: "test"]])  
              BuildSource.getBinding().setVariable("unity_app", "true")
        when:
              def result = BuildSource()
             //def result = BuildSource.if(a,b)
        then:  
              //1 * getPipelineMock("if").call('Skipping this step, build occurs during static code analysis.')
              // Use 
              1 * getPipelineMock("config").call()
              result == expected
        where: 
              a     |  b     || expected
              null  |  null  || false
              ""    |  ""    || false
            "test"  |  "foo" || true 
    }   */

    def "Unit tests run successfully" () {
        setup:
            BuildSource.getBinding().setVariable("config", [unit_test: [resultDir: "test"]]) 
        when:
            BuildSource()
        then:
            noExceptionThrown()
           1 * getPipelineMock("build_dotnet").toString()
           1 * getPipelineMock("build_dotnet").call()
    }

}
