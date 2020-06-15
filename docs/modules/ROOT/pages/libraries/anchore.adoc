= Anchore

The Anchore library implements a comprehensive container image vulnerability and compliance scan, and generates JSON reports as well as tabluar output that can be reviewed as part of your container image build step.  The library communicates with your on-premises Anchore Enterprise or Anchore Engine deployment the Anchore API.  For more information on deploying and using Anchore, see the https://docs.anchore.com[Anchore Documentation].

== Steps Contributed

.Steps
|===
| *Step* | *Description* 

| ``scan_container_image()``
| Scan the container image built and pushed to a registry, with the image tag identifiers to scan fetched by get_images_to_build()

|===

== Library Configuration Options

.Configuration Options
|===
| *Field* | *Type* | *Description* | *Default Value*

| cred
| String
| Name of the Jenkins Credential that holds the username/password for authentication against your locally deployed Anchore Engine
| None (required to be specified)

| anchore_engine_url
| String
| Full URL of your Anchore Engine API endpoint.  Example: http://anchore.yourdomain.com:8228/v1/
| None (required to be specified)

| policy_id
| String
| ID of the policy to use when performing policy evaluation.  If specified, the policy ID must be present in your Anchore Engine system.
| default (will use the currently default/active policy configured in your Anchore Engine)

| image_wait_timeout
| Integer
| Number of seconds to wait for an image to complete analysis.
| 300

| archive_only
| Boolean
| If set to true, instruct library to skip displaying vulnerability / policy evaluation results to stdout.
| false

| bail_on_fail
| Boolean
| If set to true, cause the library to fail the build if the Anchore Policy Evaluation step results in a 'STOP' final action.  Leave this set to default (true) if you would like your build to fail when your Anchore Policy Evaluation is successful, but the image does not conform to your specified policy requirements.
| true

| perform_vulnerability_scan
| Boolean
| If set to true, cause the library to perform an Anchore Software Vulnerability scan and generate a report.
| true

| perform_policy_evaluation
| Boolean
| If set to true, cause the library to perform an Anchore Policy Evaluation compliance scan and generate a report.
| true

|===


[source,groovy]
----
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
  }
}
----

== Results

Results for this library are directly displayed in tabular form in the output of the scan_container_image() step, and also stored in parsable/raw form in your job's workspace as anchore_vulnerabilities.json and anchore_policy_evaluations.json for the vulnerability scan and policy evaluation result, respectively.

== External Dependencies 

The Anchore library requires that an on-premises Anchore Enterprise or Anchore Engine deployment is up, configured and running, as the library acts as a client against the Anchore API.  Any image that is to be scanned must first be pushed to a registry that is also accessible to the Anchore Engine deployment (with registry credentials added if needed via regular Anchore Engine mechanisms for accessing registries).  For more information on deploying Anchore Engine, see the https://docs.anchore.com[Anchore Documentation].

== Troubleshooting

The library will output both the raw HTTP as well as any JSON error payloads that may be returned when attempting to access the Anchore API.  As this library is mostly a client, typically issues will be due to a configuration or other problem with the Anchore Engine installation.  See https://docs.anchore.com/current/docs/troubleshooting/[Anchore Troubleshooting Guide] for help interpreting Anchore Engine error responses and common configuration issues.

