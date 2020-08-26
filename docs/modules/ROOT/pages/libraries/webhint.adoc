= Webhint 

https://webhint.io[Webhint] is a customizable linting tool that helps you improve your site's accessibility, speed, cross-browser compatibility, and more by checking your code for best practices and common errors.

== Steps Contributed

.Steps
|===
| *Step* | *Description*

| ``accessibility_compliance_scan()``
| generates website developer hints from the given url

|===

== Library Configuration Options

.Configuration Options
|===
| *Field* | *Description* | *Default Value*

| url
| web address to analyze
|
| extender
| Array - Optional - Hint types. See https://webhint.io/docs/user-guide/configurations/configuration-development/[the documentation] for more information
| ["accessibility"]
| failThreshold
| Optional - Hint limit at which the jenkins build will fail
| 25
| warnThreshold
| Optional - Hint limit at which the jenkins build will issue a warning
| 10

|===


[source,groovy]
----
libraries{
  url = "your_url_here"
  extender = ["progressive-web-apps"]
  failThreshold = 35
  warnThreshold = 25
}
----

== Results

// if images are required, create a new directory: docs/modules/ROOT/images/<library_name>

An example html report has been saved to PDF.

link:{attachmentsdir}/webhint/webhint_mockaroo.pdf[click here to download.]

== External Dependencies
* none

== Troubleshooting

* none
