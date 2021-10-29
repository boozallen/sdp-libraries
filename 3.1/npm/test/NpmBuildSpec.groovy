/*
  Copyright © 2018 Booz Allen Hamilton. All Rights Reserved.
  This software package is licensed under the Booz Allen Public License. The license can be found in the License file or at http://boozallen.github.io/licenses/bapl
*/

package libraries.npm

public class NpmBuildSpec extends JTEPipelineSpecification {

  def NpmBuild = null

  def setup() {
    NpmBuild = loadPipelineScriptForStep("npm","npm_build")
  }

  def "npm_invoke called" () {
    setup:
      explicitlyMockPipelineStep("npm_invoke")
      NpmBuild.getBinding().setVariable("config", [npm_build: [script: "test", npm_install: "ci"]])
    when:
      NpmBuild()
    then:
      1 * getPipelineMock("npm_invoke").call(['build', []])
  }

  def "npm_invoke called with app_env when present" () {
    setup:
      def app_env = [short_name: 'env', long_name: 'Environment', npm_build: [:]]
      explicitlyMockPipelineStep("npm_invoke")
      NpmBuild.getBinding().setVariable("config", [npm_build: [script: "test", npm_install: "ci"]])
    when:
      NpmBuild(app_env)
    then:
      1 * getPipelineMock("npm_invoke").call(['build', app_env])
  }
}
