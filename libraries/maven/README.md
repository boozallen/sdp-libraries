---
description: This library allows you to perform Maven commands in a defined build agent container
---

# Maven

This library allows you to perform Maven commands in a defined build agent container.

## Steps

| Step | Description |
| ----------- | ----------- |
| `[dynamic]()` | Step name can be any non-reserved word. This library uses [dynamic step aliasing](https://jenkinsci.github.io/templating-engine-plugin/2.4/concepts/library-development/step-aliasing/#dynamic-step-aliases) to run the Maven phases, goals, and options defined in the step configuration. |

## Configuration

``` groovy title='pipeline_config.groovy'
libraries {
  maven {
    myMavenStep {
      stageName = 'Initial Maven Lifecycle'
      buildContainer = 'maven:3.8.5-openjdk-11'
      phases = ['clean', 'validate']
      goals = ['compiler:testCompile']
      options = ['-q']
      secrets {
        myToken {
          type = 'text'
          name = 'token-name'
          id = 'my-token-id'
        }
        myCredentials {
          type = 'usernamePassword'
          usernameVar = 'USER'
          passwordVar = 'PASS'
          id = 'my-credentials-id'
        }
      }
    }
    anotherMavenStep {
      stageName = 'Maven Build'
      buildContainer = 'maven:3.8.5-openjdk-11'
      phases = ['build']
      artifacts = ['target/*.jar']
    }
  }
}
```

## Dependencies

* The `sdp` library
* Access to an appropriate Maven build agent container via the repository defined in your `sdp` library configuration
