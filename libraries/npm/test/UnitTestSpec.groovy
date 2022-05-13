/*
  Copyright © 2018 Booz Allen Hamilton. All Rights Reserved.
  This software package is licensed under the Booz Allen Public License. The license can be found in the License file or at http://boozallen.github.io/licenses/bapl
*/

package libraries.npm
import spock.lang.Unroll

public class UnitTestSpec extends JTEPipelineSpecification {

  def NpmInvoke = null

  def setup() {
    NpmInvoke = loadPipelineScriptForStep("npm","npm_invoke")
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
      NpmInvoke.getBinding().setVariable("config", config)
      NpmInvoke.getBinding().setVariable("stepContext", stepContext)
      NpmInvoke.getBinding().setVariable("env", env)
    when: 
      NpmInvoke()
    then:
      assert env.npm_install == "unit test install"
  }

  @Unroll
  def "step #step: install command reads from lib config when app env is null"(){
      given:
        def config = [
          (step): [
            npm_install: "unit test install"
          ]
        ]
        def stepContext = [
          name: step
        ]
        def env = [:]
        explicitlyMockPipelineStep("inside_sdp_image")
        NpmInvoke.getBinding().setVariable("config", config)
        NpmInvoke.getBinding().setVariable("stepContext", stepContext)
        NpmInvoke.getBinding().setVariable("env", env)
      when: 
        NpmInvoke()
      then:
        assert env.npm_install == "unit test install"
      where:
        step | _ 
        "source_build" | _ 
        "unit_test" | _
        "lint_code" | _ 
  }

}
