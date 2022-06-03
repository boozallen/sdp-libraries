---
description: This library allows you to perform .NET build and test commands in the SDP `dotnet-sdk:5.0` agent container
---

# DotNet

This library allows you to perform .NET build and test commands in the SDP dotnet-sdk:5.0 agent container.

## Steps

| Step | Description |
| ----------- | ----------- |
| `source_build` | This step leverages the `dotnet publish` command to build your application and output the results to the specified directory via `outDir` variable. `outDir` defaults to a folder named "bin." The specified folder is archived as a Jenkins artifact. |
| `unit_test` | This step leverages the `dotnet test` command to run the unit, integration and functional tests specified in the application repository and outputs the results to a specified directory via `resultDir` variable. `resultDir` defaults to a folder named "coverage." The specified folder is archived as a Jenkins artifact.|

## Configuration

``` groovy title='pipeline_config.groovy'
libraries {
  dotnet {
    source_build {
      outDir = "applicationOutput"
    }
    unit_test {
      resultDir = "Results"
    }
  }
}
```

## Dependencies

* The SDP library
* Access to the `dotnet-sdk:5.0` build agent container via the repository defined in your SDP library configuration
