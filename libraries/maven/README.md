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
      buildContainer = 'mvn:3.8.5-openjdk-11'
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
      buildContainer = 'mvn'
      phases = ['build']
      artifacts = ['target/*.jar']
    }
    aThirdMavenStep {
      stageName = 'Maven Build'
      buildContainer = 'mvn'
      phases = ['build']
      artifacts = ['target/*.jar']
      withMavenParams = [
        'globalMavenSettingsConfig': '1c11594b-8b27-4f9e-a83c-3f2e72969514',
        'mavenOpts': '-Dsettings.security=/var/lib/jenkins/settings-security.xml',
        'traceability': false
      ]
    }
  }
}
```

## Dependencies

* The `sdp` library
* Access to an appropriate Maven build agent container via the repository defined in your `sdp` library configuration

## Migrating to 4.0

SDP `4.0` reworked this library to use dynamic step aliasing.

The Maven tool configuration within Jenkins is no longer required to use this library.

To recreate the previous `maven.run()` functionality of prior versions, the below minimal pipeline configuration and template can be used:

### Sample Pipeline Configuration

=== "Post-4.0"
    ``` groovy title="pipeline_config.groovy"
    libraries {
      maven {
        build {
          stageName = "Maven Build"
          buildContainer = 'mvn'
          phases = ['clean', 'install']
          options = ['-P integration-test']
        }
      }
    }
    ```
=== "Pre-4.0"
    ``` groovy title="pipeline_config.groovy"
    libraries {
      maven {
        mavenId = "maven"
      }
    }
    ```

### Sample Pipeline Template

=== "Post-4.0"
    ``` groovy title="Jenkinsfile"
    build()
    ```
=== "Pre-4.0"
    ``` groovy title="Jenkinsfile"
    maven.run(["clean", "install"], profiles: ["integration-test"])
    ```
