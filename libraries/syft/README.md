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

| Library Configuration | Description                                                   | Type        | Default Value       | Options                                                                                                   |
|-----------------------|---------------------------------------------------------------|-------------|---------------------|-----------------------------------------------------------------------------------------------------------|
| `raw_results_file`    | The base name of the report file generated. Omit Extension.   | String      | `syft-sbom-results` |                                                                                                           |
| `sbom_container`      | Name of the container image containing the syft executable.   | String      | `syft:0.47.0`       |                                                                                                           |
| `sbom_format`         | The valid formats a report can be generated in.               | ArrayList   | `['json']`          | `['json', 'text', 'cyclonedx-xml', 'cyclonedx-json', 'spdx-tag-value', 'spdx-json', 'github', 'table']`   |

``` groovy title='pipeline_config.groovy'
libraries {
  syft {
    raw_results_file = "syft-scan"
    sbom_container = "syft:v0.47.0"
    sbom_format = ['json', 'spdx-json', 'table']
  }
}
```

## Dependencies

* Base SDP library
* Docker SDP library
