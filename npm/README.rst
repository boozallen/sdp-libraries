.. _NPM:
-------
NPM
-------

This library provides methods that help with the business logic
defined within pipeline templates. 

.. note:: 
  
    It also provides additional functionality that can be useful for library
    developers to get scm metadata or interact with a remote gitlab repository.

=============
Configuration
=============

.. code:: groovy 

    libraries{
      npm
    }

================================
Pipeline Template Business Logic
================================

The NPM library contributes some helper methods to help with
pipeline template orchestration.

.. csv-table:: NPM Helper Methods
   :header: "Method", "Description"

   "unit_test", "run unit test: ``npm run test``"
   "build", "build the code/project: ``npm run build``"
   "npm_audit", "run ``npm audit``"


Library Configuration Options
=============================

.. csv-table:: NPM Library Configuration Options
   :header: "Field", "Description", "Default Value", "Options"
   "stash.excludes", "files to exclude", "node_modules/**",
   "stash.includes", "files to exclude", "**",


External Dependencies
=====================

