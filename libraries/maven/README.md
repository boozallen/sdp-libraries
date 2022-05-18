---
description: This library allows you to perform Maven commands in a defined build agent container.
---

# Maven

This library allows you to perform Maven commands in a defined build agent container.

## Steps

| Step | Description |
| ----------- | ----------- |
| `[dynamic]()` | Step name can be any non-reserved word. This library uses [dynamic step aliasing](https://jenkinsci.github.io/templating-engine-plugin/2.4/concepts/library-development/step-aliasing/#dynamic-step-aliases) to run the Maven phases, goals, and options defined in the step configuration. |

## Configuration

``` groovy
libraries {
  maven {
    myMavenStep {
      stageName = "Initial Maven Lifecycle"
      buildContainer = "mvn-builder:1.0"
      phases = ["clean", "validate"]
      goals = ["compiler:testCompile"]
      options = ["-q"]
      artifacts = ["target/*.jar"]
      secrets = []
    }
    anotherMavenStep {

    }
  }
}
```

## Dependencies

* The `sdp` library
* Access to an appropriate Maven build agent container via the repository defined in your `sdp` library configuration
