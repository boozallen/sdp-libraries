/*
  Copyright © 2022 Booz Allen Hamilton. All Rights Reserved.
  This software package is licensed under the Booz Allen Public License. The license can be found in the License file or at http://boozallen.github.io/licenses/bapl
*/

package libraries.yarn

public class YarnInvokeSpec extends JTEPipelineSpecification {
  def YarnInvoke = null

  def shellCommandWithYarnInstall = '''
                            set +x
                            source ~/.bashrc
                            nvm install $node_version
                            nvm version

                            npm i -g yarn@latest

                            echo 'Running with Yarn install'
                            yarn $yarnInstall
                            yarn $scriptCommand
                        '''

  def shellCommandWithoutYarnInstall = '''
                            set +x
                            source ~/.bashrc
                            nvm install $node_version
                            nvm version

                            npm i -g yarn@latest

                            echo 'Running without Yarn install'
                            yarn $scriptCommand
                        '''

  LinkedHashMap minimalUnitTestConfig = [
    unit_test: [
      stageName: "Yarn Unit Tests",
      script: "test"
    ]
  ]

  def setup() {
    LinkedHashMap config = [:]
    LinkedHashMap stepContext = [
      name: "unit_test"
    ]
    LinkedHashMap env = [:]

    YarnInvoke = loadPipelineScriptForStep("yarn", "yarn_invoke")
    
    explicitlyMockPipelineStep("inside_sdp_image")
    explicitlyMockPipelineVariable("out")

    YarnInvoke.getBinding().setVariable("config", config)
    YarnInvoke.getBinding().setVariable("stepContext", stepContext)
    YarnInvoke.getBinding().setVariable("env", env)

    getPipelineMock("readJSON")(['file': 'package.json']) >> {
      return [
        scripts: [
          test: "jest",
          lint: "eslint"
        ]
      ]
    }
  }

  def "Fails if Yarn script is not listed in package.json scripts" () {
    setup:
      YarnInvoke.getBinding().setVariable("config", [unit_test: [stageName: "Yarn Unit Tests", script: "not_found"]])
    when:
      YarnInvoke()
    then:
      1 * getPipelineMock("error")("script: 'not_found' not found in package.json scripts")
  }

  def "Succeeds when Yarn script is listed in package.json scripts" () {
    setup:
      YarnInvoke.getBinding().setVariable("config", minimalUnitTestConfig)
    when:
      YarnInvoke()
    then:
      0 * getPipelineMock("error")("script: 'test' not found in package.json scripts")
  }

  def "defaults node_version, yarn_version, and yarnInstall correctly if they are not otherwise specified" () {
    setup:
      YarnInvoke.getBinding().setVariable("config", minimalUnitTestConfig)
    when:
      YarnInvoke()
    then:
      YarnInvoke.getBinding().variables.env.node_version == 'lts/*'
      YarnInvoke.getBinding().variables.env.yarn_version == 'latest'
      YarnInvoke.getBinding().variables.env.yarnInstall == "frozen-lockfile"
  }

  def "Library sets config for node_version, yarn_version, yarnInstall, scriptCommand, and environment variables when specified and App Env does not" () {
    setup:
      YarnInvoke.getBinding().setVariable("config", [
        node_version: "config_node_version",
        yarn_version: "config_yarn_version",
        unit_test: [
          stageName: "Yarn Unit Tests",
          script: "config_scriptCommand",
          yarnInstall: "config_yarn_install",
          env: [
            someKey: "some_config_value"
          ]
        ]
      ])
    when:
      YarnInvoke()
    then:
      YarnInvoke.getBinding().variables.env.node_version == "config_node_version"
      YarnInvoke.getBinding().variables.env.yarn_version == "config_yarn_version"
      YarnInvoke.getBinding().variables.env.yarnInstall == "config_yarn_install"
      YarnInvoke.getBinding().variables.env.scriptCommand == "config_scriptCommand"
      YarnInvoke.getBinding().variables.env.someKey == "some_config_value"
  }

  def "App Env overrides library config for node_version, yarn_version, yarnInstall, scriptCommand and environment variables" () {
    setup:
      YarnInvoke.getBinding().setVariable("config", [
        node_version: "config_node_version",
        yarn_version: "config_yarn_version",
        unit_test: [
          stageName: "Yarn Unit Tests",
          script: "config_scriptCommand",
          yarnInstall: "config_yarn_install",
          env: [
            someKey: "some_config_value"
          ]
        ]
      ])
    when:
      YarnInvoke([
        yarn: [
          node_version: "appEnv_node_version",
          yarn_version: "appEnv_yarn_version",
          unit_test: [
            stageName: "Yarn Unit Tests",
            script: "appEnv_scriptCommand",
            yarnInstall: "appEnv_yarn_install",
            env: [
              someKey: "some_appEnv_value"
            ]
          ]
        ]
      ])
    then:
      YarnInvoke.getBinding().variables.env.node_version == "appEnv_node_version"
      YarnInvoke.getBinding().variables.env.yarnInstall == "appEnv_yarn_install"
      YarnInvoke.getBinding().variables.env.scriptCommand == "appEnv_scriptCommand"
      YarnInvoke.getBinding().variables.env.someKey == "some_appEnv_value"
  }

  def "Defaults Yarn install to 'frozen-lockfile' when yarnInstall is not set; runs yarn install step" () {
    setup:
      YarnInvoke.getBinding().setVariable("config", minimalUnitTestConfig)
    when:
      YarnInvoke()
    then:
      YarnInvoke.getBinding().variables.env.yarnInstall == "install --frozen-lockfile"
      1 * getPipelineMock("sh")(shellCommandWithYarnInstall)
  }

  def "Skips Yarn install step when yarnInstall is set to \"skip\"" () {
    setup:
      YarnInvoke.getBinding().setVariable("config", [unit_test: [stageName: "Yarn Unit Tests", script: "test", yarnInstall: "skip"]])
    when:
      YarnInvoke()
    then:
      1 * getPipelineMock("sh")(shellCommandWithoutYarnInstall)
  }

  def "Archives artifacts correctly" () {
    setup:
      YarnInvoke.getBinding().setVariable("config", [
        unit_test: [
          stageName: "Yarn Unit Tests",
          script: "test",
          artifacts: [
            "coverage/lcov.info",
            "coverage/lcov-report/**/*"
          ]
        ]
      ])
    when:
      YarnInvoke()
    then:
      2 * getPipelineMock("archiveArtifacts.call")(_ as Map)
  }

  def "Records ESLint results when useEslintPlugin is true" () {
    setup:
      YarnInvoke.getBinding().setVariable("stepContext", [name: "lint_code"])
      YarnInvoke.getBinding().setVariable("config", [
        lint_code: [
          stageName: "Yarn Linting",
          script: "lint",
          useEslintPlugin: true
        ]
      ])
    when:
      YarnInvoke()
    then:
      1 * explicitlyMockPipelineStep("esLint")(_ as Map)
      1 * explicitlyMockPipelineStep("recordIssues")(_ as Map)
  }

  def "Secrets set by library config when specified in library config and not specified in App Env" () {
    setup:
      YarnInvoke.getBinding().setVariable("config", [
        node_version: "config_node_version",
        unit_test: [
          stageName: "Yarn Unit Tests",
          script: "test",
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
      YarnInvoke()
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
      YarnInvoke.getBinding().setVariable("config", [
        node_version: "config_node_version",
        unit_test: [
          stageName: "Yarn Unit Tests",
          script: "test",
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
      YarnInvoke([
        yarn: [
          unit_test: [
            stageName: "Yarn Unit Tests",
            script: "test",
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
      YarnInvoke.getBinding().setVariable("config", [
        node_version: "config_node_version",
        unit_test: [
          stageName: "Yarn Unit Tests",
          script: "test",
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
      YarnInvoke()
    then:
      1* getPipelineMock("error")([
        "Yarn Library Validation Errors: ",
        "- secret 'someTextSecret' must define 'id'"
      ])
  }

  def "Secrets of invalid type cause an error" () {
    setup:
      YarnInvoke.getBinding().setVariable("config", [
        node_version: "config_node_version",
        unit_test: [
          stageName: "Yarn Unit Tests",
          script: "test",
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
      YarnInvoke()
    then:
      1* getPipelineMock("error")([
        "Yarn Library Validation Errors: ",
        "- secret 'someSecret': type 'not_a_type' is not defined"
      ])
  }

  def "Text type secrets of invalid format cause an error" () {
    setup:
      YarnInvoke.getBinding().setVariable("config", [
        node_version: "config_node_version",
        unit_test: [
          stageName: "Yarn Unit Tests",
          script: "test",
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
      YarnInvoke()
    then:
      1* getPipelineMock("error")([
        "Yarn Library Validation Errors: ",
        "- secret 'someTextSecret' must define 'name'"
      ])
  }

  def "usernamePassword type secrets of invalid format cause an error" () {
    setup:
      YarnInvoke.getBinding().setVariable("config", [
        node_version: "config_node_version",
        unit_test: [
          stageName: "Yarn Unit Tests",
          script: "test",
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
      YarnInvoke()
    then:
      1* getPipelineMock("error")([
        "Yarn Library Validation Errors: ",
        "- secret 'someUsernamePasswordSecret' must define 'usernameVar'",
        "- secret 'someUsernamePasswordSecret' must define 'passwordVar'"
      ])
  }
}
