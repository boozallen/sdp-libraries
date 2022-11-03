/*
  Copyright © 2018 Booz Allen Hamilton. All Rights Reserved.
  This software package is licensed under the Booz Allen Public License. The license can be found in the License file or at http://boozallen.github.io/licenses/bapl
*/

package libraries.dotnet

public class BuildDotnetSpec extends JTEPipelineSpecification {


    def DotNetBuild = null 

     LinkedHashMap minimalUnitTestConfig = [
        unit_test: [
            stepName: "unit_test",
            resultDir: "test"
        ]
    ] 

    def setup() {
        explicitlyMockPipelineStep("inside_sdp_image")
        explicitlyMockPipelineStep("login_to_registry")
        explicitlyMockPipelineVariable("workspace")   

        DotNetBuild = loadPipelineScriptForStep("dotnet", "build_dotnet")    
    }

    def  "Ustash" () { 
    setup: 
          DotNetBuild = loadPipelineScriptForStep("dotnet", "build_dotnet")   
          DotNetBuild.getBinding().setVariable("config", [unit_test: [resultDir: "test"]])  
    when:
          DotNetBuild()
    then:  
          1 * getPipelineMock("unstash").call('workspace')
    }   


    def "Unit tests run successfully" () {
        setup:
            def sharedLib = loadPipelineScriptForTest("dotnet/steps/build_dotnet.groovy")
            sharedLib.getBinding().setVariable("BRANCH_NAME", "master")
            DotNetBuild.getBinding().setVariable("config", [unit_test: [resultDir: "test"]]) 
        when:
            DotNetBuild()
        then:
            noExceptionThrown()
           1 * getPipelineMock("sh").call('dotnet build')
           1 * getPipelineMock("unstash").call('workspace')
           1 * getPipelineMock("stage").call('Dotnet Build', _)
           1 * getPipelineMock("inside_sdp_image").call('dotnet-sonar-scanner:5.2.2-1.1', _)
           1 * getPipelineMock("sh").toString()
           1 * getPipelineMock("stash").toString()
           1 * getPipelineMock("unstash").toString()
           1 * getPipelineMock("stage").toString()
           1 * getPipelineMock("inside_sdp_image").toString()
           1 * getPipelineMock("stash").call('workspace')
    }
}
