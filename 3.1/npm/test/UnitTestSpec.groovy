/*
  Copyright © 2018 Booz Allen Hamilton. All Rights Reserved.
  This software package is licensed under the Booz Allen Public License. The license can be found in the License file or at http://boozallen.github.io/licenses/bapl
*/

package libraries.npm

public class UnitTestSpec extends JTEPipelineSpecification {

  def UnitTest = null

  def setup() {
    UnitTest = loadPipelineScriptForStep("npm","unit_test")
  }

  def "npm_invoke called" () {
    setup:
      explicitlyMockPipelineStep("npm_invoke")
      UnitTest.getBinding().setVariable("config", [unit_test: [script: "test", npm_install: "ci"]])
    when:
      UnitTest()
    then:
      1 * getPipelineMock("npm_invoke").call(['unit_test', []])
  }

  def "npm_invoke called with app_env when present" () {
    setup:
      def app_env = [short_name: 'env', long_name: 'Environment', unit_test: [:]]
      explicitlyMockPipelineStep("npm_invoke")
      UnitTest.getBinding().setVariable("config", [unit_test: [script: "test", npm_install: "ci"]])
    when:
      UnitTest(app_env)
    then:
      1 * getPipelineMock("npm_invoke").call(['unit_test', app_env])
  }
}
