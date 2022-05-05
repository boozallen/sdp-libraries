/*
  Copyright © 2018 Booz Allen Hamilton. All Rights Reserved.
  This software package is licensed under the Booz Allen Public License. The license can be found in the License file or at http://boozallen.github.io/licenses/bapl
*/

package libraries.npm

public class SourceBuildSpec extends JTEPipelineSpecification {

  def SourceBuild = null

  def setup() {
    SourceBuild = loadPipelineScriptForStep("npm","source_build")
  }

  def "npm_invoke called" () {
    setup:
      explicitlyMockPipelineStep("npm_invoke")
      SourceBuild.getBinding().setVariable("config", [source_build: [script: "test", npm_install: "ci"]])
    when:
      SourceBuild()
    then:
      1 * getPipelineMock("npm_invoke").call(['source_build', []])
  }

  def "npm_invoke called with app_env when present" () {
    setup:
      def app_env = [short_name: 'env', long_name: 'Environment', source_build: [:]]
      explicitlyMockPipelineStep("npm_invoke")
      SourceBuild.getBinding().setVariable("config", [source_build: [script: "test", npm_install: "ci"]])
    when:
      SourceBuild(app_env)
    then:
      1 * getPipelineMock("npm_invoke").call(['source_build', app_env])
  }
}
