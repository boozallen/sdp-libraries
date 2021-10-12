# Sysdig Secure

This library leverages Sysdig Secure's [inline scanning script](https://github.com/sysdiglabs/secure-inline-scan) to scan container images, report the information to the Sysdig Secure server, and download a PDF report of the findings. 

## Steps Contributed
---

| Step | Description |
| ----------- | ----------- |
| ``scan_container_image()`` | Scans container images determined by ``get_images_to_build()`` |

## Library Configuration Options
---

Configuration Options

| Field | Type | Description | Default Value |
| ----------- | ----------- | ----------- | ----------- |
| scan_script_url | String | A URL from which to download the inline_scan.sh file | https://download.sysdig.com/stable/inline_scan.sh|
| sysdig_secure_url  | String | The Sysdig Secure URL to publish results to | https://secure.sysdig.com|
| cred | String | A string matching a credential id of a secret text credential in the Jenkins Credential store holding an API token to authenticate to the Sysdig Secure API ||
| enforce_success | Boolean  | Whether or not to fail the build if the scan fails | true |

```groovy
libraries{
  sysdig_secure{
    cred = "sysdig-secure-api-token"
  }
}
```

## Results
---

The `scan_container_images()` step will generate a PDF report of the scan if the upload to the Sysdig Secure API is successful. [Here's an example](../../assets/attachments/sysdig_secure/sysdig_secure_report.pdf).

## External Dependencies
---

This library, by nature of the inline scanning script, requires that:

* a running docker daemon is available 
* internet access to pull an image from docker.io/anchore/inline-scan

**Note** At the time of writing, this library could be expanded to pass a custom image to perform the scanning, perhaps helpful if proxying through a local registry, by setting the environment variable `SYSDIG_CI_IMAGE` as part of the command invocation.

## Troubleshooting
---
