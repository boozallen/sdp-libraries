/*
  Copyright © 2018 Booz Allen Hamilton. All Rights Reserved.
  This software package is licensed under the Booz Allen Public License. The license can be found in the License file or at http://boozallen.github.io/licenses/bapl
*/

package libraries.npm

public class NpmInvokeSpec extends JTEPipelineSpecification {

  def NpmInvoke = null

    public static class DummyException extends RuntimeException {
    public DummyException(String _message) { super( _message ); }
  }


  def env = [npm_version: null, scriptCommand: null, npm_install: null]

  def setup() {
    env = [npm_version: null, scriptCommand: null, npm_install: null]
    NpmInvoke = loadPipelineScriptForStep("npm","npm_invoke")
    NpmInvoke.getBinding().setVariable("env", env)
    explicitlyMockPipelineStep("inside_sdp_image")("npx:1.0.0")
    NpmInvoke.getBinding().setVariable("config", [:])
    getPipelineMock("readJSON")(['file':'package.json']) >> { return [scripts: [test: "jest"]] }
  }

  def "Fails if stepName is not supported" () {
    when:
      NpmInvoke("not_a_step")
    then:
      1 * getPipelineMock("error")('stepName must be "build" or "unit_test", got "not_a_step"')
  }

  def "Fails if npm method is not listed in package.json scripts" () {
    setup:
      NpmInvoke.getBinding().setVariable("config", [unit_test: [script: "not_found"]])
    when:
      NpmInvoke("unit_test")
    then:
      1 * getPipelineMock("error")("stepName 'not_found' not found in package.json scripts")
  }

  def "Succeeds when npm method is listed in package.json scripts" () {
    setup:
      NpmInvoke.getBinding().setVariable("config", [unit_test: [script: "test"]])
    when:
      NpmInvoke("unit_test")
    then:
      0 * getPipelineMock("error")("stepName 'test' not found in package.json scripts")
  }




//   def "npm_invoke called with app_env when present" () {
//     setup:
//       def app_env = [short_name: 'env', long_name: 'Environment', npm_build: [:]]
//       explicitlyMockPipelineStep("npm_invoke")
//       NpmInvoke.getBinding().setVariable("config", [npm_build: [script: "test", npm_install: "ci"]])
//     when:
//       NpmInvoke(app_env)
//     then:
//       1 * getPipelineMock("npm_invoke").call(['build', app_env])
//   }
}
