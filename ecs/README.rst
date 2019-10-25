.. _ECS Library:
------
ECS
------

The ecs library will build container images and push them into an AWS ECS repository.


Steps Provided
==============

- deploy_to()

Example Configuration Snippet
=============================

.. code:: groovy

   libraries{
     ecs
   }


External Dependencies
=====================

- A Docker registry must be set up and configured and containing an aws cli image. Credentials to the repository are also needed.
- 'aws' library needs to be loaded as a library inside your pipeline_config.groovy file. provides app_env values

Troubleshooting
===============

FAQ
===
