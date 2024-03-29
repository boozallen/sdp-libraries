---
description: Uses the Grype CLI to scan container images for vulnerabilities.
---

# Grype

Uses the [Grype CLI](https://github.com/anchore/grype) to scan container images for vulnerabilities.

## Steps

| Step                   | Description                                                |
|------------------------|------------------------------------------------------------|
| container_image_scan() | Performs the Grype scan against your scaffold build image. |

## Configuration

| Library Configuration | Description                                              | Type    | Default Value | Options                                                   |
|-----------------------|----------------------------------------------------------|---------|---------------|-----------------------------------------------------------|
| `grype_container`     | The container image to execute the scan within           | String  | grype:0.38.0  |                                                           |
| `report_format`       | The output format of the generated report                | String  | json          | `json`, `table`, `cyclonedx`, `template`                  |
| `fail_on_severity`    | The severity level threshold that will fail the pipeline | String  | high          | `none`, `negligible`, `low`, `medium`, `high`, `critical` |
| `grype_config`        | A custom path to a grype configuration file              | String  | `null`        |                                                           |
| `scan_sbom`           | Boolean to turn on SBOM scanning                         | Boolean | false         | true, false                                               |

``` groovy title='pipeline_config.groovy'
libraries {
  grype {
    grype_container = "grype:0.38.0"
    report_format = "json"
    fail_on_severity = "high"
    grype_config = "Path/to/Grype.yaml"
    scan_sbom = false
  }
}
```

## Grype Configuration File

If `grype_config` isn't provided, the default locations for an application are `.grype.yaml`, `.grype/config.yaml`.


Read [the grype docs](https://github.com/anchore/grype#configuration) to learn more about the Grype configuration file

## Dependencies

---

* This library requires that the `docker` library also be loaded and `build()` be invoked before `container_image_scan()`
* If the default `grype_container` is replaced, it must be able to run docker containers (packages: docker-ce, docker-ce-cli and containerd.io).
