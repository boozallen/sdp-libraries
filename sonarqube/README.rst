.. _SonarQube Library: 
---------
SonarQube
---------

SonarQube is a tool used for **static code analysis**. Static code analysis is validating code as-written against
industry standard practices.  It will help you find best practice violations and potential security vulnerabilities.

Organizations can define Quality Profiles which are custom rule profiles that projects must use.  Quality Gates are then
rules defining the organizational policies for code quality. SDP will, by default, fail the build if the Quality Gate fails.

Steps Contributed
=================
* static_code_analysis()

Optional Steps Used
=================
* build_source()

If your sonarqube needs to build before your run of sonarqube,
you should implement a *step* called `build_source`, in another library or as a Default Step Implementation, to create the build artifacts


Library Configuration Options
=============================


.. csv-table::  SonarQube Library Configuration Options
   :header: "Field", "Description", "Default Value"

   "build_step", "the name of the a step to run, *before* sonarqube runs, to create build artifacts", ""
   "require_build_step", "if this step should fail if a step is not found for the build_step name", "<null>"
   "credential_id", "A new credential can be used", "sonarqube"
   "enforce_quality_gate", "Determine whether the build will fail if the code does not pass the quality gate", "true"


Example Configuration Snippet
=============================

.. code:: groovy

   libraries{
     sonarqube{
       require_build_step = true
       build_step = "build_source"
       enforce_quality_gate = true
       credential_id = sonarqube
     }
   }

Sonar Scanner Configurations
============================

Extra configuration options are available by leveraging SonarQube's sonar-project.properties_ file.
the sonar-project.properties file should be added to root of the source repositoy.

.. _sonar-project.properties: https://docs.sonarqube.org/display/SONAR/Analysis+Parameters

External Dependencies
=====================

* SonarQube must already be deployed. Reference the deployment script for SDP.
* Jenkins must have a credential to access SonarQube, this is done by default when using the deployment script.
* The SonarQube URL must be configured in ``Manage Jenkins > Configure System``

Troubleshooting
===============

FAQ
===
