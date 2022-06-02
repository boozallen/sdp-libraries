---
description: This library allows you to perform .Net build and test commands in the sdp dotnet-sdk:5.0 agent container
---

# Dotnet

This library allows you to perform .Net build and test commands in the sdp dotnet-sdk:5.0 agent container.

## Steps

| Step | Description |
| ----------- | ----------- |
| `source_build` | This step leverages the dotnet publish command to build your application and output the results to the specified directory via outDir Variable. outDir defaults to a folder named "bin." The specified folder is archived as a jenkins artifact. |
| `unit_test` | This step leverages the dotnet test command to run the unit, integration and functional tests specified in the application repository and outputs the results to a specified directory via resultDir variable. resultDir defaults to a folder named "coverage." The specified folder is archived as a jenkins artifact.|

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

* The `sdp` library
* Access to the dotnet-sdk:5.0 build agent container via the repository defined in your `sdp` library configuration
