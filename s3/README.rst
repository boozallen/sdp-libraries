.. _S3:
------
S3
------

The S3 library will upload files into AWS S3 based on configuration.
It uses the Jenkins Environment Variables. The configuration uses the app environment first then the library configuration

Jenkins Environment Variables Used
==================================
- AWS_REGION

Steps Provided
==============

- call()

Example Configuration Snippet
=============================

.. code:: groovy
   application_environments{
     Dev{
       s3{
         excludePattern = ".git/*"
       }
     }
   }
   libraries{
     aws {
       projectName = "aws"
       identifier = "app1"
     }
     s3{
       workingDir = "dist/xxx-ui"
       excludePattern = ".git/*"
     }
   }

Configurations
==============

.. csv-table::  Configuration Options
   :header: "Field", "Description", "Default Value", "Required"

   "workingDir", "the sub directory of the project that will be the root of S3 upload", "", "true"
   "includePattern", "the include pattern for the s3 sync call ", "none", "false"
   "excludePattern", "the exclude pattern for the s3 sync call ", "none", "false"
   "envFilePath", "the location of an env config file ", "", "false"
   "S3_BUCKET", "the S3 Bucket of S3 upload", "", "true"

Parameters: only needed if using configuration file generation
==============

.. csv-table::  AWS Parameters
   :header: "Field", "Description", "Default Value", "Required"

   "S3_BUCKET", "the S3 Bucket of S3 upload", "", "true"
   "COGNITO_USER_POOL_ID", "the include pattern for the s3 sync call ", "none", "false"
   "COGNITO_USER_POOL_CLIENT_ID"
   "BASE_API_URL", "the exclude pattern for the s3 sync call ", "none", "false"
   "SERVICE_PORTAL_URL", "the location of an env config file ", "assets/env.json", "false"

External Dependencies
=====================

- the `aws` sdp library is needed
- requires AWS Parameter Store values
- A container registry must be set up and configured and contain an 'aws' image

Troubleshooting
===============

FAQ
===
