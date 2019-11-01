.. _MVN:
-------
MVN
-------

This library provides a wrapper for NPM functionality. Currently, functionality around build, unit testing

=============
Configuration
=============

.. code:: groovy 

    libraries{
      mvn{
         build_cmd = "clean package -Dmaven.test.skip=true"
         image = "maven:3.6-jdk-8"
      }
    }


Steps Provided
==============
- unit_test()
- build_source()

.. csv-table:: Maven Steps Methods
   :header: "Method", "Description"

   "unit_test", "runs unit test configured via test_cmd and test_install: ``mvn clean verify jacoco:report``"
   "build", "build the code/project via build_cmd: ``mvn clean package -Dmaven.test.skip=true``"


Library Configuration Options
=============================

.. csv-table:: NPM Library Configuration Options
   :header: "Field", "Description", "Default Value", "Options"
   "image", "the image in which the commands are executed", "maven:3.6-jdk-8"
   "stash.excludes", "files to exclude", "**/*Test.java",
   "stash.includes", "files to include", "**",
   "stash.name", "the stash name to which the workspace is saved", "workspace"
   "test_cmd", "the command in 'mvn $test_cmd' that is executed for testing in the 'unit_test' step", "clean verify jacoco:report"
   "test_fail_on_exception", "whether or not exceptions in the test run call 'error'(fail) or 'unstable'", true
   "test_stash", "the name of the stash for tests", "$stash.name ?: workspace"
   "test_stash_always", "stashes results of tests into $stash.name regardless of success", false
   "build_cmd", "the command in 'mvn $build_cmd' that is executed for the 'build' step", "clean package -Dmaven.test.skip=true"
   "test_report_path", "the path for the test report output file", "junit.xml"


External Dependencies
=====================
   Uses the sdp.inside_sdp_images step from the sdp-libraries
