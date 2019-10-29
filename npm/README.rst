.. _NPM:
-------
NPM
-------

This library provides a wrapper for NPM functionality. Currently, functionality around build, unit testing

=============
Configuration
=============

.. code:: groovy 

    libraries{
      npm{
         build_install = "install"
         build_cmd = "build"
         node_image = "node:10.16.0-stretch-slim"
      }
    }


Steps Provided
==============
- unit_test()
- npm_audit()
- npm_install()
- build()

.. csv-table:: NPM Helper Methods
   :header: "Method", "Description"

   "unit_test", "runs unit test configured via test_cmd and test_install: ``npm run test``"
   "build", "build the code/project via build_cmd and build_install: ``npm run build``"
   "npm_audit", "run ``npm audit``"
   "npm_install", "run ``npm install``"


Library Configuration Options
=============================

.. csv-table:: NPM Library Configuration Options
   :header: "Field", "Description", "Default Value", "Options"
   "node_image", "the image in which the commands are executed", "node:latest"
   "stash.excludes", "files to exclude", "node_modules/**",
   "stash.includes", "files to include", "**",
   "test_cmd", "the command in 'npm run $test_cmd' that is executed for testing in the 'unit_test' step", "test"
   "test_install", "the command in 'npm $test_install' that is executed prior to the test execution", "install"
   "test_stash", "the name of the stash for tests", "workspace"
   "test_stash_always", "stashes results of tests into $stash.name regardless of success", false
   "build_cmd", "the command in 'npm run $build_cmd' that is executed for the 'build' step", "build"
   "build_install", "the command in 'npm $build_install' that is executed prior to the build execution", "install"
   "npm_base", "the base directory under workspace used by 'npm audit'", ""
   "use_npm_default_registry", "whether to use 'https://registry.npmjs.org'", false
   "test_report_path", "the path for the test report output file", "junit.xml"


External Dependencies
=====================

