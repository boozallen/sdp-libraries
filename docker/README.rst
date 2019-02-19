.. _Docker Library:
------
Docker
------

The Docker library will build docker images and push them into a docker repository.

Steps Provided
==============

- build()
- get_images_to_build()
- login_to_registry()
- retag()

Library Configuration Options
=============================

.. csv-table::  Docker Library Configuration Options
   :header: "Field", "Description", "Default Value"

   "build_strategy", "This defines the type of docker build to be used. There are three options to choose from; docker-compose, modules, and dockerfile", "dockerfile"

Example Configuration Snippet
=============================

.. code:: groovy

   libraries{
     docker {
        build_strategy = docker-compose | modules | dockerfile
     }
   }

Extra Configurations
====================

If you are using a separate set of credentials change the variables listed below inside the configuration file.

.. csv-table::  Docker Extra Configuration Options
   :header: "Field", "Description", "Default Value"

   "sdp.image.registry", "The location where the container images required for the different pipeline tools are stored.", "none"
   "sdp.image.cred", "Credentials used for the repository where pipeline builds are going to be pushed to", "none"
   "docker.registry", "Where the container images produced during the pipeline builds are going to be pushed to", "none"
   "docker.cred", "Credentials used for the repository where different docker pipeline tools are stored.", "none"

External Dependencies
=====================

- A Docker repository must be setup and configured. Credentials to the repository are also needed.
- The github_enterprise library and the sdp library needs to be loaded as libraries inside your pipeline_config.groovy file.

Troubleshooting
===============

FAQ
===
