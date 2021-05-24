/*
  Copyright © 2018 Booz Allen Hamilton. All Rights Reserved.
  This software package is licensed under the Booz Allen Public License. The license can be found in the License file or at http://boozallen.github.io/licenses/bapl
*/

package libraries.npm

public class NpmInvokeSpec extends JTEPipelineSpecification {

  def NpmInvoke = null

  def env = [:]
  def shellCommandWithNpmInstall = '\n                        set +x\n                        source ~/.bashrc\n                        nvm install $node_version\n                        nvm version\n                        \n                        npm $npm_install\n                        npm run $scriptCommand\n                    '

  def shellCommandWithOutNpmInstall = '\n                        set +x\n                        source ~/.bashrc\n                        nvm install $node_version\n                        nvm version\n\n                        npm run $scriptCommand\n                    '

  def setup() {
    env = [:]
    NpmInvoke = loadPipelineScriptForStep("npm","npm_invoke")
    NpmInvoke.getBinding().setVariable("env", env)
    explicitlyMockPipelineStep("inside_sdp_image")("npx:1.0.0")
    explicitlyMockPipelineVariable("out")
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

  def "Defaults node_version, npm_install and scriptCommand correctly if they are not otherwise specified" () {
    when:
      NpmInvoke("unit_test")
    then:
      NpmInvoke.getBinding().variables.env.node_version == 'lts/*'
      NpmInvoke.getBinding().variables.env.npm_install == ""
      NpmInvoke.getBinding().variables.env.scriptCommand == ""
  }

  def "Library sets config for node_version, npm_install, scriptCommand and environment variables when specified and App Env does not" () {
    setup:
      NpmInvoke.getBinding().setVariable("config", [
        node_version: "config_node_version",
        unit_test: [
          npm_install: "config_npm_install", 
          script: "config_scriptCommand",
          env: [
            someKey: "some_config_value"
          ]
        ]
      ])
    when:
      NpmInvoke("unit_test")
    then:
      NpmInvoke.getBinding().variables.env.node_version == "config_node_version"
      NpmInvoke.getBinding().variables.env.npm_install == "config_npm_install"
      NpmInvoke.getBinding().variables.env.scriptCommand == "config_scriptCommand"
      NpmInvoke.getBinding().variables.env.someKey == "some_config_value"
  }

  def "App Env overrides library config for node_version, npm_install, scriptCommand and environment variables" () {
    setup:
      NpmInvoke.getBinding().setVariable("config", [
        node_version: "config_node_version",
        unit_test: [
          npm_install: "config_npm_install", 
          script: "config_scriptCommand",
          env: [
            someKey: "some_config_value"
          ]
        ]
      ])
    when:
      NpmInvoke("unit_test", [
        npm: [
          node_version: "appEnv_node_version",
          unit_test: [
            npm_install: "appEnv_npm_install", 
            script: "appEnv_scriptCommand",
            env: [
              someKey: "some_appEnv_value"
            ]
          ]
        ]
      ])
    then:
      NpmInvoke.getBinding().variables.env.node_version == "appEnv_node_version"
      NpmInvoke.getBinding().variables.env.npm_install == "appEnv_npm_install"
      NpmInvoke.getBinding().variables.env.scriptCommand == "appEnv_scriptCommand"
      NpmInvoke.getBinding().variables.env.someKey == "some_appEnv_value"
  }

  def "Runs npm install step when npm_install is set to an allowable option" () {
    setup:
      NpmInvoke.getBinding().setVariable("config", [unit_test: [script: "test", npm_install: "install"]])
    when:
      NpmInvoke("unit_test")
    then:
      1 * getPipelineMock("sh")(shellCommandWithNpmInstall)
  }

  def "Skips npm install step when npm_install is not set" () {
    setup:
      NpmInvoke.getBinding().setVariable("config", [unit_test: [script: "test"]])
    when:
      NpmInvoke("unit_test")
    then:
      1 * getPipelineMock("sh")(shellCommandWithOutNpmInstall)
  }

  def "Skips npm install step when npm_install is set to \"\"" () {
    setup:
      NpmInvoke.getBinding().setVariable("config", [unit_test: [script: "test", npm_install: ""]])
    when:
      NpmInvoke("unit_test")
    then:
      1 * getPipelineMock("sh")(shellCommandWithOutNpmInstall)
  }

  def "Secrets set by library config when specified in library config and not specified in App Env" () {
    setup:
      NpmInvoke.getBinding().setVariable("config", [
        node_version: "config_node_version",
        unit_test: [
          env: [
            secrets: [
              someTextSecret: [
                type: "text",
                name: "TEXT_TOKEN",
                id: "credId"
              ]
            ]
          ]
        ]
      ])
    when:
      NpmInvoke("unit_test")
    then:
      1 * getPipelineMock("string.call")([
        'credentialsId':'credId',
        'variable':'TEXT_TOKEN'
      ]) >> "string('credentialsId':'credId', 'variable':'TEXT_TOKEN')"
      1 * getPipelineMock("withCredentials")(_) >> {_arguments -> 
            assert _arguments[0][0] == ["string('credentialsId':'credId', 'variable':'TEXT_TOKEN')"]
      }
  }

  def "Secrets set by App Env override same secrets set by library config when specified in both" () {
    setup:
      NpmInvoke.getBinding().setVariable("config", [
        node_version: "config_node_version",
        unit_test: [
          env: [
            secrets: [
              someTextSecret: [
                type: "text",
                name: "config_TEXT_TOKEN",
                id: "config_credId"
              ]
            ]
          ]
        ]
      ])
    when:
      NpmInvoke("unit_test", [
        npm: [
          unit_test: [
            env: [
            secrets: [
              someTextSecret: [
                type: "text",
                name: "appEnv_TEXT_TOKEN",
                id: "appEnv_credId"
              ]
            ]
            ]
          ]
        ]
      ])
    then:
      1 * getPipelineMock("string.call")([
        'credentialsId':'appEnv_credId',
        'variable':'appEnv_TEXT_TOKEN'
      ]) >> "string('credentialsId':'appEnv_credId', 'variable':'appEnv_TEXT_TOKEN')"
      1 * getPipelineMock("withCredentials")(_) >> {_arguments -> 
            assert _arguments[0][0] == ["string('credentialsId':'appEnv_credId', 'variable':'appEnv_TEXT_TOKEN')"]
      }
  }

  def "Secrets without an id cause an error" () {
    setup:
      NpmInvoke.getBinding().setVariable("config", [
        node_version: "config_node_version",
        unit_test: [
          env: [
            secrets: [
              someTextSecret: [
                type: "text",
                name: "TEXT_TOKEN"
              ]
            ]
          ]
        ]
      ])
    when:
      NpmInvoke("unit_test")
    then:
      1* getPipelineMock("error")([
        "Npm Library Validation Errors: ",
        "- secret 'someTextSecret' must define 'id'"
      ])
  }

  def "Secrets of invalid type cause an error" () {
    setup:
      NpmInvoke.getBinding().setVariable("config", [
        node_version: "config_node_version",
        unit_test: [
          env: [
            secrets: [
              someSecret: [
                type: "not_a_type",
                name: "TEXT_TOKEN",
                id: "credId"
              ]
            ]
          ]
        ]
      ])
    when:
      NpmInvoke("unit_test")
    then:
      1* getPipelineMock("error")([
        "Npm Library Validation Errors: ",
        "- secret 'someSecret': type 'not_a_type' is not defined"
      ])
  }

  def "Text type secrets of invalid format cause an error" () {
    setup:
      NpmInvoke.getBinding().setVariable("config", [
        node_version: "config_node_version",
        unit_test: [
          env: [
            secrets: [
              someTextSecret: [
                type: "text",
                id: "credId"
              ]
            ]
          ]
        ]
      ])
    when:
      NpmInvoke("unit_test")
    then:
      1* getPipelineMock("error")([
        "Npm Library Validation Errors: ",
        "- secret 'someTextSecret' must define 'name'"
      ])
  }

  def "usernamePassword type secrets of invalid format cause an error" () {
    setup:
      NpmInvoke.getBinding().setVariable("config", [
        node_version: "config_node_version",
        unit_test: [
          env: [
            secrets: [
              someUsernamePasswordSecret: [
                type: "usernamePassword",
                id: "credId"
              ]
            ]
          ]
        ]
      ])
    when:
      NpmInvoke("unit_test")
    then:
      1* getPipelineMock("error")([
        "Npm Library Validation Errors: ",
        "- secret 'someUsernamePasswordSecret' must define 'usernameVar'",
        "- secret 'someUsernamePasswordSecret' must define 'passwordVar'"
      ])
  }
  
}
