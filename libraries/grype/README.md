---
description: Uses the Grype CLI to scan container images for vulnerabilities.
---

# Grype

Uses the [Grype CLI](https://github.com/anchore/grype) to scan container images for vulnerabilities.

## Steps

| Step | Description |
|------|-------------|
|   container_image_scan()   | Performs the Grype scan against your scaffold build image.             |

## Configuration

| Library Configuration | Type | Default Value |
|-----------------------|------|---------------|
| grype_container | String | grype:0.38.0 |
| output_format | String | json |
| fail_on_severity | String | high |

## Dependencies

---
* docker is required to be installed on your grype container. The packages needed are docker-ce, docker-ce-cli and containerd.io.