.. _Gradle:
---
SDP
---

The Gradle library provides steps used by multiple libraries within sdp-libraries.

Steps Provided
==============

- unit_test
- build
- invokeGradle
- publish

Library Configuration Options
=============================

.. csv-table::  Docker Library Configuration Options
   :header: "Field", "Description", "Default Value"

   "build.modules", "The sub directories to be used for build", "*none*"
   "build.order", "ordering for directories if necessary", "*none"
   "test.modules", "The sub directories to be used for test", "*none*"
   "test.order", "ordering for directories if necessary", "*none"
   "publish.modules", "The sub directories to be used for publish", "*none*"
   "publish.order", "ordering for directories if necessary", "*none"


Example Configuration Snippet
=============================

.. code:: groovy

   libraries{
     gradle_modules{
       test{
         tasks{
           test
         }
         module_1{
         }
       }
       build{
         tasks{
         }
         tasksExcludes{
           test
         }
         module_1{
         }
       }
     }
   }

External Dependencies
=====================

- A Docker registry must be setup and configured.

.. Troubleshooting
.. ===============

.. FAQ
.. ===
