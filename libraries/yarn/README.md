---
description: Run Yarn script commands in an NVM container with a specified Node version
---

# Yarn

Run Yarn script commands in an NVM container with a specified Node version.

## Configuration

All configs can be set in either the library config or the Application Environment. All configs set in Application Environment take precedence.

Environment variables and secrets set in the library config are concatenated with those set in the Application Environment.
Environment variables and secrets with the same key are set to the definition contained in the Application Environment.

## Steps

Steps are configured dynamically in either the library config or the Application Environment.

``` groovy title="pipeline_configuration.groovy"
libraries {
  yarn {
    [step_name] {
      // config fields described below
    }
    ...
  }
}
```

## Example Library Configuration

---

| Field                         | Description                                                                                                                            | Default           |
| ----------------------------- | -------------------------------------------------------------------------------------------------------------------------------------- | ----------------- |
| `nvm_container`               | The container image to use                                                                                                             | nvm:1.0.0         |
| `node_version`                | Node version to run Yarn within (installed via NVM)                                                                                    | `lts/*`           |
| `yarn_version`                | Yarn version to use                                                                                                                    | `latest`          |
| `<step name>.stageName`       | stage name displayed in the Jenkins dashboard                                                                                          | N/A               |
| `<step name>.script`          | Yarn script ran by the step                                                                                                            | N/A               |
| `<step name>.artifacts`       | array of glob patterns for artifacts that should be archived                                                                           |
| `<step name>.yarnInstall`     | Yarn install command to run; Yarn install can be skipped with value "skip"                                                             | `frozen-lockfile` |
| `<step name>.env`             | environment variables to make available to the Yarn process; can include key/value pairs and secrets                                   | `[]`              |
| `<step name>.env.secrets`     | text or username/password credentials to make available to the Yarn process; must be present and available in Jenkins credential store | `[]`              |
| `<step name>.useEslintPlugin` | if the Jenkins ESLint Plugin is installed, will run the `recordIssues` step to send lint results to the plugin dashboard               | `false`           |

### Full Configuration Example

Each available method has config options that can be specified in the Application Environment or within the library configuration.

``` groovy title="pipeline_configuration.groovy"
application_environments {
  dev
  prod {
    yarn {
      node_version = "14.16.1"
      yarn_version = "1.22.17"
      unit_test {
        stageName = "Yarn Unit Tests"
        script = "full-test-suite"
        artifacts = ["coverage/lcov.info"]
        yarnInstall = "frozen-lockfile"
        env {
          someKey = "prodValue for tests"
          // (1)
          secrets{
            someTextCredential {
              type = "text"
              name = "VARIABLE_NAME"
              id = "prod-credential-id"
            }
            someUsernamePasswordCredential {
              type = "usernamePassword"
              usernameVar = "USER"
              passwordVar = "PASS"
              id = "prod-credential-id"
            }
            // (2)
          }
        }
      }
      source_build {
        stageName = "Yarn Source Build"
        script = "prod-build"
        env {
          someKey = "prodValue for builds"
          secrets {
            someTextCredential {
              type = "text"
              name = "VARIABLE_NAME"
              id = "prod-credential-id"
            }
            someUsernamePasswordCredential {
              type = "usernamePassword"
              usernameVar = "USER"
              passwordVar = "PASS"
              id = "prod-credential-id"
            }
          }
        }
      }
    }
    lint_code {
        stageName = "Yarn Lint Code"
        script = "lint"
        artifacts = [
          "eslint-report.json",
          "eslint-report.html",
          "eslint-report.xml",
        ]
        useEslintPlugin = true
        env {
          someKey = "prodValue for linting"
          secrets {
            someTextCredential {
              type = "text"
              name = "VARIABLE_NAME"
              id = "prod-credential-id"
            }
            someUsernamePasswordCredential {
              type = "usernamePassword"
              usernameVar = "USER"
              passwordVar = "PASS"
              id = "prod-credential-id"
            }
          }
        }
      }
    }
  }
}

libraries {
  yarn {
    node_version = "lts/*"
    yarn_version = "latest"
    unit_test {
      stageName = "Yarn Unit Tests"
      script = "test"
      yarnInstall = "install"
      env {
        someKey = "someValue for tests"
        // (3)
        secrets {
          someTextCredential {
            type = "text"
            name = "VARIABLE_NAME"
            id = "some-credential-id"
          }
          someUsernamePasswordCredential {
            type = "usernamePassword"
            usernameVar = "USER"
            passwordVar = "PASS"
            id = "some-credential-id"
          }
          // (4)
        }
      }
    }
    source_build {
      stageName = "Yarn Source Build"
      script = "build"
      yarnInstall = "skip"
      env {
        someKey = "someValue for builds"
        secrets {
          someTextCredential {
            type = "text"
            name = "VARIABLE_NAME"
            id = "some-credential-id"
          }
          someUsernamePasswordCredential {
            type = "usernamePassword"
            usernameVar = "USER"
            passwordVar = "PASS"
            id = "some-credential-id"
          }
        }
      }
    }
    lint_code {
      stageName = "Yarn Lint Code"
      script = "lint"
      yarnInstall = "skip"
      env {
        someKey = "someValue for linting"
        secrets {
          someTextCredential {
            type = "text"
            name = "VARIABLE_NAME"
            id = "some-credential-id"
          }
          someUsernamePasswordCredential {
            type = "usernamePassword"
            usernameVar = "USER"
            passwordVar = "PASS"
            id = "some-credential-id"
          }
        }
      }
    }
  }
}
```

1. more envVars as needed
2. more secrets as needed
3. more envVars as needed
4. more secrets as needed

This example shows the prod Application Environment overriding configs set in the library config.
`source_build.yarnInstall` is preserved as set in library config, since it isn't overridden by the Application Environment.

### Minimal Configuration Example

The minimal configuration for this library is:

``` groovy title="pipeline_configuration.groovy"
libraries {
  yarn {
    unit_test {
      stageName = "Yarn Unit Tests"
      script = "test"
    }
  }
}
```

### Secrets

There are two types of secrets currently supported: secret text and username/password credentials.
These credentials must be stored in the Jenkins credential store and be available to the pipeline.

The name of each credential block (such as `someTextCredential`) is arbitrary.
It's just a key, used to supersede library config with Application Environment configs, and when describing configuration errors found by the step.

## Dependencies

* The [SDP library](../sdp/) must be loaded inside the `pipeline_config.groovy` file.
