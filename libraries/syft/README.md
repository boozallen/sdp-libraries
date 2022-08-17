---
description: This library allows you to generate a Software Bill of Materials (SBOM) for each container built in your project
---

# Syft

This library allows you to generate a Software Bill of Materials (SBOM) for each container built in your project using the [Syft tool](https://github.com/anchore/syft).

## Steps

| Step              | Description                                      |
|-------------------|--------------------------------------------------|
| `generate_sbom()` | Generates and archives SBOM files in JSON format |

## Configuration

| Library Configuration | Type   | Default Value            |
|-----------------------|--------|--------------------------|
| `raw_results_file`    | String | `syft-sbom-results.json` |
| `sbom_container`      | String | `syft:latest`            |

``` groovy title='pipeline_config.groovy'
libraries {
  syft {
    raw_results_file = "syft-scan.json"
    sbom_container = "syft:v0.47.0"
  }
}
```

## Dependencies

* Base SDP library
* Docker SDP library
