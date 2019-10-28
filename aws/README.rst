.. _AWS:
------
AWS
------

The AWS library will pull parameter values from AWS parameter store and inject them into the application environment config.
It uses the Jenkins Environment Variables.

Jenkins Environment Variables Used
==================================
- AWS_REGION
- PROJECT_NAME: used as default for project name

Steps Provided
==============

- assumeRole()
- getAwsParameters()
- paramStoreDecorator()
- setAppEnvConfig()

Example Configuration Snippet
=============================

.. code:: groovy

   libraries{
     aws {
        projectName = "aws"
        identifier = "app1"
     }
   }

Configurations
==============

.. csv-table::  Docker Configuration Options
   :header: "Field", "Description", "Default Value", "Required"

   "projectName", "the name of the overall project ", "none", "false"
   "identifier", "the name of the specific application. used in the parameter store path", "none", "true"

External Dependencies
=====================

- A container registry must be set up and configured. It must contain an 'aws' image

Troubleshooting
===============

FAQ
===
