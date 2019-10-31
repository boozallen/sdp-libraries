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

Example Configuration Snippet
=============================

.. code:: groovy

   libraries{
     docker {
        build_strategy = "dockerfile"
        registry = "docker-registry.default.svc:5000"
        cred = "openshift-docker-registry"
        repo_path_prefix = "proj-images"
        remove_local_image = true
     }
   }

Configurations
==============

.. csv-table::  Docker Configuration Options
   :header: "Field", "Description", "Default Value", "Required"

   "build_strategy", "Sets how the library will build the container image(s); Must be dockerfile, docker-compose, or modules", "dockerfile", "false"
   "registry", "Where the container images produced during the pipeline builds are going to be pushed to", "none", "true"
   "cred", "Credentials used for the repository where different docker pipeline tools are stored.", "none", "true"
   "repo_path_prefix", "the part of the repository name between the registry name and the last forward-slash", "empty string", "false"
   "remove_local_image", "Determines if the pipeline should remove the local image after building or retagging", "false", "false"

External Dependencies
=====================

- A Docker registry must be set up and configured. Credentials to the repository are also needed.
- Either the github or github_enterprise library needs to be loaded as a library inside your pipeline_config.groovy file.

Troubleshooting
===============

FAQ
===
