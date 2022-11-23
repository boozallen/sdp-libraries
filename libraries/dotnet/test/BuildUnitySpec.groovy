/*
  Copyright © 2018 Booz Allen Hamilton. All Rights Reserved.
  This software package is licensed under the Booz Allen Public License. The license can be found in the License file or at http://boozallen.github.io/licenses/bapl
*/

package libraries.dotnet

public class BuildUnitySpec extends JTEPipelineSpecification {


    def UnityBuild = null 
    def UnityBuild2 = null 

     LinkedHashMap minimalUnitTestConfig = [
        unit_test: [
            stepName: "unit_test",
            resultDir: "test"
        ]
    ] 

    def setup() {
        explicitlyMockPipelineStep("inside_sdp_image")
        explicitlyMockPipelineStep("login_to_registry")
        explicitlyMockPipelineVariable("USERNAME")   
        explicitlyMockPipelineVariable("PASSWORD")   
        explicitlyMockPipelineVariable("SERIAL") 
        explicitlyMockPipelineVariable("workspace")   

        UnityBuild = loadPipelineScriptForStep("dotnet", "build_unity")    
    }

    def  "Shared Library Variables" () { 
    setup: 
          def MyFunction = loadPipelineScriptForTest("dotnet/steps/build_unity.groovy")
    when:
          MyFunction.getBinding().setVariable("BRANCH_NAME", "master")
    then:  
          0 * getPipelineMock("unstash").call('workspace')
    }   

    def  "Ustash" () { 
    setup: 
          UnityBuild = loadPipelineScriptForStep("dotnet", "build_unity")   
          UnityBuild.getBinding().setVariable("config", [unit_test: [resultDir: "test"]])  
    when:
          UnityBuild()
    then:  
          1 * getPipelineMock("unstash").call('workspace')
    }   

     def  "Credentials" () { 
      setup:
          UnityBuild = loadPipelineScriptForStep("dotnet", "build_unity")   
          UnityBuild.getBinding().setVariable("config", [unit_test: [resultDir: "test"]])  
      when:
           UnityBuild()
      then:  
          1 * getPipelineMock("usernamePassword.call").call(['credentialsId':'unitycreds', 'usernameVariable':'USERNAME', 'passwordVariable':'PASSWORD'])
        
    }   


    def "Unit tests run successfully" () {
        setup:
            def sharedLib = loadPipelineScriptForTest("dotnet/steps/build_unity.groovy")
            sharedLib.getBinding().setVariable("BRANCH_NAME", "master")
            UnityBuild.getBinding().setVariable("config", [unit_test: [resultDir: "test"]]) 
        when:
            UnityBuild()
        then:
            noExceptionThrown()
           2 * getPipelineMock("sh").toString()
           1 * getPipelineMock("stage").call('Unity Build', _)
           1 * getPipelineMock("string.call").call(['credentialsId':'unityserial', 'variable':'SERIAL'])
           1 * getPipelineMock("usernamePassword.call").call(['credentialsId':'unitycreds', 'usernameVariable':'USERNAME', 'passwordVariable':'PASSWORD'])
           1 * getPipelineMock("inside_sdp_image").call('unity:ubuntu-2020.3.30f1-base-1.0.1-1.1', _)
           1 * getPipelineMock("withCredentials").call([null, null], _)
           1 * getPipelineMock("stash").toString()
           1 * getPipelineMock("usernamePassword.call").toString()
           1 * getPipelineMock("unstash").toString()
           1 * getPipelineMock("stage").toString()
           1 * getPipelineMock("inside_sdp_image").toString()
           1 * getPipelineMock("withCredentials").toString()
           1 * getPipelineMock("string.call").toString()
           1 * getPipelineMock("sh").call('unity-editor -projectPath=Mock Generator for [workspace] -executeMethod UnityEditor.SyncVS.SyncSolution -quit -nographics -logFile=/dev/stdout')  
           1 * getPipelineMock("sh").call('unity-editor -username \'Mock Generator for [USERNAME]\' -password \'Mock Generator for [PASSWORD]\' -serial \'Mock Generator for [SERIAL]\' -projectPath=Mock Generator for [workspace]  -quit -nographics -logFile=/dev/stdout')
           1 * getPipelineMock("stash").call('workspace')
    }



}
