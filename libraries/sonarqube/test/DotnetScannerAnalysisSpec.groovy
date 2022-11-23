/*
  Copyright © 2018 Booz Allen Hamilton. All Rights Reserved.
  This software package is licensed under the Booz Allen Public License. The license can be found in the License file or at http://boozallen.github.io/licenses/bapl
*/

package libraries.dotnet

public class DotnetScannerAnalysisSpec extends JTEPipelineSpecification {
    def DotnetScannerAnalysis = null
    
    LinkedHashMap minimalSourceBuildConfig = [
        source_build: [
            stepName: "dotnet_scanner_analysis",
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
        explicitlyMockPipelineStep("dotnet_scanner_analysis")
        explicitlyMockPipelineVariable("workspace")  
        explicitlyMockPipelineVariable("SONAR_HOST_URL")  

        DotnetScannerAnalysis = loadPipelineScriptForStep("sonarqube", "dotnet_scanner_analysis")    
    }

    def "Unit tests run successfully" () {
        setup:
            DotnetScannerAnalysis.getBinding().setVariable("config", [unit_test: [resultDir: "test"]]) 
        when:
            DotnetScannerAnalysis()
        then:
            noExceptionThrown()
            1 * getPipelineMock("inside_sdp_image").toString()
            1 * getPipelineMock("stage").toString()
            1 * getPipelineMock("withSonarQubeEnv.call").toString()
            1 * getPipelineMock("withCredentials").toString()
            1 * getPipelineMock("string.call").toString()
            1 * getPipelineMock("unstash").toString()
            1 * getPipelineMock("env.getProperty").call('REPO_NAME')
            1 * getPipelineMock("string.call").call(['credentialsId':'sonarqube-token', 'variable':'sq_token'])
            2 * getPipelineMock("env.getProperty").call('sq_token')
            1 * getPipelineMock("unstash").call('workspace')
            1 * getPipelineMock("env.getProperty").call('ORG_NAME')

    }

}
