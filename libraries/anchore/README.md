---
description: Performs comprehensive container image vulnerability scan and compliance policy evaluation using your Anchore Enterprise or Anchore Engine installation
---

# Anchore

The Anchore library implements a comprehensive container image vulnerability and compliance scan,
and generates JSON reports as well as tabular output that can be reviewed as part of your container image build step.
The library communicates with your on-premises Anchore Enterprise or Anchore Engine deployment the Anchore API.
For more information on deploying and using Anchore, see the [Anchore Documentation](https://docs.anchore.com).

## Steps

---

| Step | Description |
| ----------- | ----------- |
| `scan_container_image()` | Scan the container image built and pushed to a registry, with the image tag identifiers to scan fetched by `get_images_to_build()` |
| `add_registry_creds()` | Add container registry credentials to Anchore if they don't already exist, so it can pull an image from a private registry. Can run this step before `scan_container_image` to ensure Anchore has access to an image in a private registry. |

## Configuration

---

| Field | Type | Description | Default Value |
| ----------- | ----------- | ----------- | ----------- |
| `cred` | String | Name of the Jenkins Credential that holds the username/password for authentication against your locally deployed Anchore Engine | None (required to be specified) |
| `anchore_engine_url` | String | Full address of your Anchore Engine API endpoint. Example: <http://anchore.yourdomain.com:8228/v1/> | None (required to be specified) |
| `policy_id` | String | ID of the policy to use when performing policy evaluation. If specified, the policy ID must be present in your Anchore Engine system. |  default (will use the currently default/active policy configured in your Anchore Engine)
| `image_wait_timeout` | Integer | Number of seconds to wait for an image to complete analysis. | `300` |
| `archive_only` | Boolean | If set to `true`, instruct library to skip displaying vulnerability / policy evaluation results to standard output. | `false` |
| `bail_on_fail` | Boolean | If set to `true`, cause the library to fail the build if the Anchore Policy Evaluation step results in a 'STOP' final action. Leave this set to default (`true`) if you would like your build to fail when your Anchore Policy Evaluation is successful, but the image doesn't conform to your specified policy requirements. | `true` |
| `perform_vulnerability_scan` | Boolean | If set to `true`, cause the library to perform an Anchore Software Vulnerability scan and generate a report. | `true` |
| `perform_policy_evaluation` | Boolean | If set to `true`, cause the library to perform an Anchore Policy Evaluation compliance scan and generate a report. | `true` |
| `docker_registry_credential_id` | String | Credential id of private docker registry | `true` |
| `docker_registry_name` | String | Address of private docker registry | `true` |
| `k8s_credential` | String | Credential id of kubeconfig credential | `true` |
| `k8s_context` | String | Cluster context to use in kubeconfig | `true` |

```groovy
libraries{
  anchore {
    cred = "anchore_admin"
    anchore_engine_url = "http://anchore.yourdomain.com:8228/v1/"
    //policy_id = "anchore_security_only"
    //image_wait_timeout = 600
    //archive_only = false
    //bail_on_fail = false
    //perform_vulnerability_scan = true
    //perform_policy_evaluation = true
    //docker_registry_credential_id = docker_registry
    //docker_registry_name = ""
    //k8s_credential
    //k8s_context
  }
}
```

## Results

---

Results for this library are directly displayed in tabular form in the output of the `scan_container_image()` step,
and also stored in parsable/raw form in your job's workspace as `anchore_vulnerabilities.json` and
`anchore_policy_evaluations.json` for the vulnerability scan and policy evaluation result, respectively.

## Dependencies

---

The Anchore library requires that an on-premises Anchore Enterprise or Anchore Engine deployment is up,
configured, and running, as the library acts as a client against the Anchore API.
Any image that's to be scanned must first be pushed to a registry that's also accessible to the Anchore Engine deployment
(with registry credentials added if needed via regular Anchore Engine mechanisms for accessing registries).
For more information on deploying Anchore Engine, see the [Anchore Documentation](https://docs.anchore.com).

## Troubleshooting

---

The library will output both the raw HTTP as well as any JSON error payloads that may be returned when attempting to access the Anchore API.
As this library is mostly a client, typically issues will be due to a configuration or other problem with the Anchore Engine installation.
See the [Anchore Troubleshooting Guide](https://docs.anchore.com/current/docs/troubleshooting/) for help interpreting Anchore Engine error responses and common configuration issues.
