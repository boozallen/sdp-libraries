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
|  output_format | String | json |
| fail_on_severity | String | high|

## Dependencies

---
* Docker-ce