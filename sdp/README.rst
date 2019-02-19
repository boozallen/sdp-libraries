.. _SDP Library:
---
SDP
---

The SDP library provides steps used by multiple libraries within sdp-libraries.

Steps Provided
==============

- inside_sdp_images

Library Configuration Options
=============================

.. csv-table::  Docker Library Configuration Options
   :header: "Field", "Description", "Default Value"

   "images.registry", "This sets the registry the sdp library expects to find its Docker images", "*none*"
   "images.repo", "The first `path component`_. in the repository name, e.g. if your images follow the format my-registry.com/sdp/\*, this would be **sdp**", "sdp"
   "images.cred", "Credentials used for the repository where different docker pipeline tools are stored", "*none*"
   "images.docker_args", "Arguments to use when starting the container. Uses the same flags as ``docker run``", "*empty string*"

.. important::

   Unlike the Docker Library, the value in "registry" *does* include the
   protocol (http/https)

.. _path component: https://forums.docker.com/t/docker-registry-v2-spec-and-repository-naming-rule/5466

Example Configuration Snippet
=============================

.. code:: groovy

   libraries{
     sdp{
       images{
         registry = "https://docker-registry.default.svc:5000"
         repo = "sdp"
         cred = "openshift-docker-registry"
         docker_args = ""
       }
     }
   }

External Dependencies
=====================

- A Docker registry must be setup and configured. Credentials to the registry are also needed.
- A repository for the image being used by the given library is expected to be in the given registry.
- The repository name for the pipeline tools' images should be in the format *"${images.registry}/${images.repo}/tool-name"*

.. Troubleshooting
.. ===============

.. FAQ
.. ===
