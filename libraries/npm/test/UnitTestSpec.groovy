/*
  Copyright © 2018 Booz Allen Hamilton. All Rights Reserved.
  This software package is licensed under the Booz Allen Public License. The license can be found in the License file or at http://boozallen.github.io/licenses/bapl
*/

package libraries.npm

public class UnitTestSpec extends JTEPipelineSpecification {

  def UnitTest = null

  def setup() {
    UnitTest = loadPipelineScriptForStep("npm","npm_invoke")
  }

  def "unit test install command used when no app env present"(){
    given:
      def config = [
        unit_test: [
          npm_install: "unit test install"
        ]
      ]
      def stepContext = [
        name: "unit_test"
      ]
      def env = [:]
      explicitlyMockPipelineStep("inside_sdp_image")
      UnitTest.getBinding().setVariable("config", config)
      UnitTest.getBinding().setVariable("stepContext", stepContext)
      UnitTest.getBinding().setVariable("env", env)
    when: 
      UnitTest()
    then:
      assert env.npm_install == "unit test install"
  }






}
