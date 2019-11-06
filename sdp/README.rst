.. _SDP Library:
---
SDP
---

The SDP library provides steps used by multiple libraries within sdp-libraries.

Steps Provided
==============
The following steps mirror steps from docker-workflow:

- inside_sdp_images(String img, Map params = [args:String], Closure body)
.. csv-table::  inside_sdp_images arguments
   :header: "Field", "Description", "Required/Optional"

   "img", "the name of the docker image", "required"
   "params", "a map arguments and options for docker", "optional"
   "params.args", "the options for docker run", "optional"
   "body", "the closure to be run in the containers context", "optional"

- with_run_sdp_images(String img, Map params = [args:String, command:String], Closure body)
.. csv-table::  with_run_sdp_images arguments
   :header: "Field", "Description", "Required/Optional"

   "img", "the name of the docker image", "required"
   "params", "a map arguments and options for docker", "optional"
   "params.args", "the options for docker run", "optional"
   "params.command", "the arguments passed to the container entrypoint", "optional"
   "body", "the closure to be run", "optional"

Library Configuration Options
=============================

.. csv-table::  SDP Library Configuration Options
   :header: "Field", "Description", "Default Value"

   "images.registry", "This sets the registry the sdp library expects to find its Docker images", "*none*"
   "images.repository", "The first `path component`_. in the repository name, e.g. if your images follow the format my-registry.com/sdp/\*, this would be **sdp**", "sdp"
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
         repository = "sdp"
         cred = "openshift-docker-registry"
         docker_args = ""
       }
     }
   }

.. code:: groovy

   libraries{
     sdp{
       images{
         registry = "https://docker-registry.default.svc:5000"
         repository = "sdp"
         cred = "openshift-docker-registry"
         docker_args = ""
       }
     }
     libA{
       images{
         repository = "libA"
         cred = "libA-docker-registry"
         docker_args = "--network=host"
         imageX{
           registry = "https://docker.pkg.github.com"
           repository = "imageX"
           cred = "github-pkg"
         }
       }
     }
   }

External Dependencies
=====================

- A Docker registry must be setup and configured. Credentials to the registry are also needed.
- A repository for the image being used by the given library is expected to be in the given registry.
- The repository name for the pipeline tools' images should be in the format *"${images.registry}/${images.repository}/tool-name"*

.. Troubleshooting
.. ===============

.. FAQ
.. ===
