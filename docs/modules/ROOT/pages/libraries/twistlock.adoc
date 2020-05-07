= Twistlock

Twistlock is an automated and scalable container cybersecurity platform. Twistlock manages a full-lifecycle vulnerability and compliance management to application-tailored runtime defense and cloud native firewalls, Twistlock helps secure your containers and modern applications against threats across the entire application lifecycle.

SDP can integrate with Twistlock to perform *container image scanning*

== Steps Provided

.Steps
|===
| Step | Description

| ``scan_container_image()``
| Downloads the Twistlock CLI from the Twistlock Console and performs container image scanning 

|===

== Library Configurations

.Twistlock Library Configuration Options
|===
| Field | Description | Default Value

| url
| The Twistlock Console url
| 

| credential
| The Jenkins credential ID to access Twistlock Console
| 

|===

=== Example Configuration Snippet

[source,groovy]
----
libraries{
  twistlock{
    url = "https://twistlock.apps.ocp.microcaas.net"
    credential = "twistlock"
  }
}
----

== External Dependencies

* Twistlock is deployed and accessible from Jenkins
* A credential has been placed in the Jenkins credential store to access the console
* A separater container building library that implements ``get_images_to_build()``

== Twistlock Scan Results

Jenkins will output a text based table of the scan results. A more descriptive JSON file is archived that contains details of CVE and compliance vulnerabilities found during the scan.

[source,]
----
CVE Results:
-----------------------------------------
Low:      [0-9]*  Number of Low vulnerabilities
Medium:   [0-9]*  Number of Medium vulnerabilities
High:     [0-9]*  Number of High vulnerabilities
Critical: [0-9]*  Number of Critical vulnerabilities

Compliance Results:
-----------------------------------------
Low:      [0-9]*  Number of Low compliance violations
Medium:   [0-9]*  Number of Medium compliance violations
High:     [0-9]*  Number of High compliance violations
Critical: [0-9]*  Number of Critical compliance violations
----