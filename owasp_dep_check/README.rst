.. _Owasp Dependency Check Library:
----------------------
OWASP Dependency Check
----------------------

The OWASP Dependency Check library will use the namesake tool to scan a project's
source code to identify components with known vulnerabilities.

Official Site: https://www.owasp.org/index.php/OWASP_Dependency_Check
Documentation: https://jeremylong.github.io/DependencyCheck/

Steps Provided
==============
* static_dependency_check_analysis()

Library Configuration Options
=============================

.. csv-table:: Owasp Dependency Check Library Configuration Options
   :header: "Field", "Description", "Default Value"

   "exclude_dirs", "This is a comma-separated list of directories that should not be scanned", "(Empty String; doesn't exclude any directories)"
   "scan_target", "The directory the scanner runs agains; the root directory of the source code", "(Empty String; Scans the base directory of the GitHub repository)"
   "cvss_threshold", "The pipeline should fail if a vulnerability is detected with a cvss score at or above this. Details below", "pass"
   "image_version", "The tag/version for the scanner docker image used", "latest"
   "report_format", "The output format to write scan results to (XML, HTML, CSV, JSON, VULN, ALL)", "ALL"

Example Configuration Snippet
=============================

.. code:: groovy

   libraries{
     owasp_dep_check {
       scan_target = "src"
       cvss_threshold = "9"
     }
   }

viewing The Reports
===================

By default, the static_dependency_check_analysis step outputs the results of its
analysis in multiple formats: HTML, JSON, XML, and CSV. It also outputs an
abbreviated report in HTML that contains just the detected vulnerabilities.

You can view these reports in the "Artifacts" tab of the build in the Blue Ocean
View. In the standard Jenkins view, at the "Stage View", you can view the
reports by clicking on the small blue arrow to the left of the build's
progress bar.


CVSS Threshold & Scores
=======================
From the `Wikipedia article`_, "The Common Vulnerability Scoring System (CVSS)
is a free and open industry standard for assessing the severity of computer
system security vulnerabilities ... Scores range from 0 to 10, with 10 being the
most severe"

The pipeline has the ability to fail if vulnerability is detected at or above
a given threshold. This threshold is set with the "cvss_threshold" configuration
option. For example, if cvss_threshold is set to 7, and a vulnerabily with a
CVSS score of 7.5 is detected, the pipeline will fail. If the vulnerability
remains, but the cvss_threshold is set to 9, the pipeline will pass the OWASP
Dependency Check scan.

If you wish for the scan to pass regardless of the CVSS scores of detected
vulnerabilities, you may set cvss_threshold to a number higher than 10 or to
"pass". For example:

.. code:: groovy

   libraries{
     owasp_dep_check {
       cvss_threshold = "pass"
     }
   }

.. _Wikipedia article: https://en.wikipedia.org/wiki/Common_Vulnerability_Scoring_System

Troubleshooting
===============
Coming Soon!

FAQ
===
Coming Soon!
