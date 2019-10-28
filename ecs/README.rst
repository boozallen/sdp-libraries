.. _ECS Library:
------
ECS
------

The ECS library will build container images and push them into an AWS ECS repository.
This library will update an existing task definition to reference a new container image and then redeploy the service managing the task definition.
This library will only work if the master is hosted in an AWS resource (ECS container, EC2 instance) that has IAM permissions to perform the deployment

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
