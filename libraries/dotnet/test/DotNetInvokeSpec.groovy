/*
  Copyright © 2018 Booz Allen Hamilton. All Rights Reserved.
  This software package is licensed under the Booz Allen Public License. The license can be found in the License file or at http://boozallen.github.io/licenses/bapl
*/

package libraries.dotnet

public class DotNetInvokeSpec extends JTEPipelineSpecification {
    def DotNetInvoke = null 

    def buildCommand = "dotnet publish -c release -o ${outDir}"

    def testCommand = '''
                rm -drf ${ResultDir}
                dotnet test --collect:'XPlat Code Coverage' --results-directory ${resultDir} --logger trx
            '''

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
      LinkedHashMap stepContext = [
        name: "unit_test"
    ]

    DotNetInvoke = loadPipelineScriptForStep("dotnet", "dotnet_invoke")
    
    DotNetInvoke.getBinding().setVariable("stepContext", stepContext)

    }


}

def "Succeeds when result dir is specified" () {
    setup:
        //DotNetInvoke.getBinding().setVariable("config", [unit_test: [resultDir: "test"]])
    when:
        //DotNetInvoke()
    then:
    0 * getPipelineMock("error")
}
