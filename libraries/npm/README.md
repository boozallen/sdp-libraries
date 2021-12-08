---
description: Run npm `test` and `build` commands in an nvm container with a specified Node version
---

# npm

Run npm `test` and `build` commands in an nvm container with a specified Node version.

## Steps

---

| Step | Description |
| ----------- | ----------- |
| ``unit_test()`` | Calls npm_invoke to run `npm run test` command |
| ``npm_build()`` | Calls npm_invoke to run `npm run build` command |
| ``npm_invoke(String stepName)`` | Runs npm command in nvm container |

## Configuration

All configs can be set in either the library config or the Application Environment. All configs set in Application Environment take precedence.

Environment variables and secrets set in the library config are concatenated with those set in the Application Environment.
Environment variables and secrets with the same key are set to the definition contained in the Application Environment.

## Example Library Configuration

---

| Field | Description | Default |
| ----------- | ----------- | ----------- |
| `node_version` | node version to run npm within (installed via nvm) | `lts/*` |
| `unit_test.script` | npm command to run; must be present in package.json scripts block | `test` |
| `build.script` | npm command to run; must be present in package.json scripts block | `build` |
| `<step name>.npm_install` | npm install command to run; npm install can be skipped with value "skip" | `ci` |
| `<step name>.env` | environment variables to make available to npm process; can include key/value pairs and secrets| `[]` |
| `<step name>.env.secrets` | text or username/password credentials to make available to npm process; must be present and available in Jenkins credential store | `[]` |

### Full Configuration Example

Each available method has config options that can be specified in the application environment or within the library configuration.

``` groovy title="pipeline_configuration.groovy"
application_environments{
  dev
  prod{
    npm{
      node_version = "14.16.1"
      unit_test{
        script = "full-test-suite"
        npm_install = "ci"
        env{
          someKey = "prodValue for tests"
          // (1)
          secrets{
            someTextCredential{
              type = "text"
              name = "VARIABLE_NAME"
              id = "prod-credential-id"
            }
            someUsernamePasswordCredential{
              type = "usernamePassword"
              usernameVar = "USER"
              passwordVar = "PASS"
              id = "prod-credential-id"
            }
            // (2)
          }
        }
      }
      npm_build{
        script = "prod-build"
        env{
          someKey = "prodValue for builds"
          // (3)
          secrets{
            someTextCredential{
              type = "text"
              name = "VARIABLE_NAME"
              id = "prod-credential-id"
            }
            someUsernamePasswordCredential{
              type = "usernamePassword"
              usernameVar = "USER"
              passwordVar = "PASS"
              id = "prod-credential-id"
            }
            // (4)
          }
        }
      }
    }
  }
}

libraries{
  npm{
    node_version = "lts/*"
    unit_test{
      script = "test"
      npm_install = "install"
      env{
        someKey = "someValue for tests"
        // (5)
        secrets{
          someTextCredential{
            type = "text"
            name = "VARIABLE_NAME"
            id = "some-credential-id"
          }
          someUsernamePasswordCredential{
            type = "usernamePassword"
            usernameVar = "USER"
            passwordVar = "PASS"
            id = "some-credential-id"
          }
          // (6)
        }
      }
    }
    npm_build{
      script = "build"
      npm_install = "skip"
      env{
        someKey = "someValue for builds"
        // (7)
        secrets{
          someTextCredential{
            type = "text"
            name = "VARIABLE_NAME"
            id = "some-credential-id"
          }
          someUsernamePasswordCredential{
            type = "usernamePassword"
            usernameVar = "USER"
            passwordVar = "PASS"
            id = "some-credential-id"
          }
          // (8)
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
5. more envVars as needed
6. more secrets as needed
7. more envVars as needed
8. more secrets as needed

This example shows the prod Application Environment overriding configs set in the library config.
`npm_build.npm_install` is preserved as set in library config, since it isn't overridden by the Application Environment.

### Minimal Configuration Example

The minimal configuration for this library is:

``` groovy
//pipeline_configuration.groovy
libraries{
  npm
}
```

### Secrets

There are two types of secrets currently supported: secret text and username/password credentials.
These credentials must be stored in the Jenkins credential store and be available to the pipeline.

The name of each credential block (such as `someTextCredential`) is arbitrary.
It's just a key, used to supersede library config with Application Environment configs, and when describing configuration errors found by the step.

## Dependencies

* The [SDP library](../sdp/) must be loaded inside the `pipeline_config.groovy` file.
