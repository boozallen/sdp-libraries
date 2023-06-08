/*
  Copyright © 2018 Booz Allen Hamilton. All Rights Reserved.
  This software package is licensed under the Booz Allen Public License. The license can be found in the License file or at http://boozallen.github.io/licenses/bapl
*/

package libraries.npm

public class NpmInvokeSpec extends JTEPipelineSpecification {
  def NpmInvoke = null

  def shellCommandWithNpmInstall = '''
                                set +x
                                source ~/.bashrc
                                nvm install $node_version
                                nvm version

                                echo 'Running with NPM install'
                                npm $npmInstall
                                npm run $scriptCommand
                            '''

  def shellCommandWithoutNpmInstall = '''
                                set +x
                                source ~/.bashrc
                                nvm install $node_version
                                nvm version

                                echo 'Running without NPM install'
                                npm run $scriptCommand
                            '''

  LinkedHashMap minimalUnitTestConfig = [
    unit_test: [
      stageName: "NPM Unit Tests",
      script: "test"
    ]
  ]

  def setup() {
    LinkedHashMap config = [:]
    LinkedHashMap stepContext = [
      name: "unit_test"
    ]
    LinkedHashMap env = [:]

    NpmInvoke = loadPipelineScriptForStep("npm", "npm_invoke")
    
    explicitlyMockPipelineStep("inside_sdp_image")
    explicitlyMockPipelineStep("withGit")
    explicitlyMockPipelineVariable("out")

    NpmInvoke.getBinding().setVariable("config", config)
    NpmInvoke.getBinding().setVariable("stepContext", stepContext)
    NpmInvoke.getBinding().setVariable("env", env)

    getPipelineMock("readJSON")(['file': 'package.json']) >> {
      return [
        scripts: [
          test: "jest",
          lint: "eslint"
        ]
      ]
    }
  }

  def "Fails if npm script is not listed in package.json scripts" () {
    setup:
      NpmInvoke.getBinding().setVariable("config", [unit_test: [stageName: "NPM Unit Tests", script: "not_found"]])
    when:
      NpmInvoke()
    then:
      1 * getPipelineMock("error")("script: 'not_found' not found in package.json scripts")
  }

  def "Succeeds when npm script is listed in package.json scripts" () {
    setup:
      NpmInvoke.getBinding().setVariable("config", minimalUnitTestConfig)
    when:
      NpmInvoke()
    then:
      0 * getPipelineMock("error")("script: 'test' not found in package.json scripts")
  }

  def "defaults node_version and npm_install correctly if they are not otherwise specified" () {
    setup:
      NpmInvoke.getBinding().setVariable("config", minimalUnitTestConfig)
    when:
      NpmInvoke()
    then:
      NpmInvoke.getBinding().variables.env.node_version == 'lts/*'
      NpmInvoke.getBinding().variables.env.npmInstall == "ci"
  }

  def "Library sets config for node_version, npm_install, scriptCommand, and environment variables when specified and App Env does not" () {
    setup:
      NpmInvoke.getBinding().setVariable("config", [
        node_version: "config_node_version",
        unit_test: [
          stageName: "NPM Unit Tests",
          script: "config_scriptCommand",
          npmInstall: "config_npm_install",
          env: [
            someKey: "some_config_value"
          ]
        ]
      ])
    when:
      NpmInvoke()
    then:
      NpmInvoke.getBinding().variables.env.node_version == "config_node_version"
      NpmInvoke.getBinding().variables.env.npmInstall == "config_npm_install"
      NpmInvoke.getBinding().variables.env.scriptCommand == "config_scriptCommand"
      NpmInvoke.getBinding().variables.env.someKey == "some_config_value"
  }

  def "App Env overrides library config for node_version, npm_install, scriptCommand and environment variables" () {
    setup:
      NpmInvoke.getBinding().setVariable("config", [
        node_version: "config_node_version",
        unit_test: [
          stageName: "NPM Unit Tests",
          script: "config_scriptCommand",
          npmInstall: "config_npm_install",
          env: [
            someKey: "some_config_value"
          ]
        ]
      ])
    when:
      NpmInvoke([
        npm: [
          node_version: "appEnv_node_version",
          unit_test: [
            stageName: "NPM Unit Tests",
            script: "appEnv_scriptCommand",
            npmInstall: "appEnv_npm_install",
            env: [
              someKey: "some_appEnv_value"
            ]
          ]
        ]
      ])
    then:
      NpmInvoke.getBinding().variables.env.node_version == "appEnv_node_version"
      NpmInvoke.getBinding().variables.env.npmInstall == "appEnv_npm_install"
      NpmInvoke.getBinding().variables.env.scriptCommand == "appEnv_scriptCommand"
      NpmInvoke.getBinding().variables.env.someKey == "some_appEnv_value"
  }

  def "Defaults npm install to 'ci' when npmInstall is not set; runs npm install step" () {
    setup:
      NpmInvoke.getBinding().setVariable("config", minimalUnitTestConfig)
    when:
      NpmInvoke()
    then:
      NpmInvoke.getBinding().variables.env.npmInstall == "ci"
      1 * getPipelineMock("sh")(shellCommandWithNpmInstall)
  }

  def "Skips npm install step when npmInstall is set to \"skip\"" () {
    setup:
      NpmInvoke.getBinding().setVariable("config", [unit_test: [stageName: "NPM Unit Tests", script: "test", npmInstall: "skip"]])
    when:
      NpmInvoke()
    then:
      1 * getPipelineMock("sh")(shellCommandWithoutNpmInstall)
  }

  def "Archives artifacts correctly" () {
    setup:
      NpmInvoke.getBinding().setVariable("config", [
        unit_test: [
          stageName: "NPM Unit Tests",
          script: "test",
          artifacts: [
            "coverage/lcov.info",
            "coverage/lcov-report/**/*"
          ]
        ]
      ])
    when:
      NpmInvoke()
    then:
      2 * getPipelineMock("archiveArtifacts.call")(_ as Map)
  }

  def "Records ESLint results when useEslintPlugin is true" () {
    setup:
      NpmInvoke.getBinding().setVariable("stepContext", [name: "lint_code"])
      NpmInvoke.getBinding().setVariable("config", [
        lint_code: [
          stageName: "NPM Linting",
          script: "lint",
          useEslintPlugin: true
        ]
      ])
    when:
      NpmInvoke()
    then:
      1 * explicitlyMockPipelineStep("esLint")(_ as Map)
      1 * explicitlyMockPipelineStep("recordIssues")(_ as Map)
  }

  def "Secrets set by library config when specified in library config and not specified in App Env" () {
    setup:
      NpmInvoke.getBinding().setVariable("config", [
        node_version: "config_node_version",
        unit_test: [
          stageName: "NPM Unit Tests",
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
      NpmInvoke()
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
          stageName: "NPM Unit Tests",
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
      NpmInvoke([
        npm: [
          unit_test: [
            stageName: "NPM Unit Tests",
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
      NpmInvoke.getBinding().setVariable("config", [
        node_version: "config_node_version",
        unit_test: [
          stageName: "NPM Unit Tests",
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
      NpmInvoke()
    then:
      1* getPipelineMock("error")([
        "NPM Library Validation Errors: ",
        "- secret 'someTextSecret' must define 'id'"
      ])
  }

  def "Secrets of invalid type cause an error" () {
    setup:
      NpmInvoke.getBinding().setVariable("config", [
        node_version: "config_node_version",
        unit_test: [
          stageName: "NPM Unit Tests",
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
      NpmInvoke()
    then:
      1* getPipelineMock("error")([
        "NPM Library Validation Errors: ",
        "- secret 'someSecret': type 'not_a_type' is not defined"
      ])
  }

  def "Text type secrets of invalid format cause an error" () {
    setup:
      NpmInvoke.getBinding().setVariable("config", [
        node_version: "config_node_version",
        unit_test: [
          stageName: "NPM Unit Tests",
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
      NpmInvoke()
    then:
      1* getPipelineMock("error")([
        "NPM Library Validation Errors: ",
        "- secret 'someTextSecret' must define 'name'"
      ])
  }

  def "usernamePassword type secrets of invalid format cause an error" () {
    setup:
      NpmInvoke.getBinding().setVariable("config", [
        node_version: "config_node_version",
        unit_test: [
          stageName: "NPM Unit Tests",
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
      NpmInvoke()
    then:
      1* getPipelineMock("error")([
        "NPM Library Validation Errors: ",
        "- secret 'someUsernamePasswordSecret' must define 'usernameVar'",
        "- secret 'someUsernamePasswordSecret' must define 'passwordVar'"
      ])
  }

  def "Runs within a withGit block if git config is set" () {
    setup:
      NpmInvoke.getBinding().setVariable("config", [
        unit_test: [
          stageName: "NPM End-to-End Test",
          script: "test",
          git: [
            url: "https://www.github.com",
            cred: "my-github-pat",
            branch: "main"
          ]
        ]
      ])
    when:
      NpmInvoke()
    then:
      1 * getPipelineMock("withGit").call(_)
  }

  def "Does not use withGit block if git config is not set" () {
    setup:
      NpmInvoke.getBinding().setVariable("config", minimalUnitTestConfig)
    when:
      NpmInvoke()
    then:
      0 * getPipelineMock("withGit").call(_)
  }
}
