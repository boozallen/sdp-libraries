/*
  Copyright © 2018 Booz Allen Hamilton. All Rights Reserved.
  This software package is licensed under the Booz Allen Public License. The license can be found in the License file or at http://boozallen.github.io/licenses/bapl
*/

package libraries.npm

public class LintCodeSpec extends JTEPipelineSpecification {

  def LintCode = null

  def setup() {
    LintCode = loadPipelineScriptForStep("npm","lint_code")
  }

  def "npm_invoke called" () {
    setup:
      explicitlyMockPipelineStep("npm_invoke")
      LintCode.getBinding().setVariable("config", [lint_code: [script: "test", npm_install: "ci"]])
    when:
      LintCode()
    then:
      1 * getPipelineMock("npm_invoke").call(['lint_code', []])
  }

  def "npm_invoke called with app_env when present" () {
    setup:
      def app_env = [short_name: 'env', long_name: 'Environment', lint_code: [:]]
      explicitlyMockPipelineStep("npm_invoke")
      LintCode.getBinding().setVariable("config", [lint_code: [script: "test", npm_install: "ci"]])
    when:
      LintCode(app_env)
    then:
      1 * getPipelineMock("npm_invoke").call(['lint_code', app_env])
  }
}
