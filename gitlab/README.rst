.. _Gitlab Library: 
-------
Gitlab 
-------

This library is unique in that rather than provide functional step 
implementations, it provides methods that help with the business logic
defined within pipeline templates. 

.. note:: 
  
    It also provides additional functionality that can be useful for library
    developers to get scm metadata or interact with a remote gitlab repository.

=============
Configuration
=============

.. code:: groovy 

    libraries{
      gitlab
    }

================================
Pipeline Template Business Logic 
================================

The Gitlab library contributes some helper methods to help with 
pipeline template orchestration.  You can achieve fine grained control over 
what happens when in response to different Gitlab events such as commits, 
merge requests, and merges. 

.. csv-table:: Gitlab Flow Helper Methods 
   :header: "Method", "Build Cause" 

   "on_commit", "A direct commit to a branch" 
   "on_merge_request", "A merge request was created or a developer pushed a commit to the source branch"
   "on_change", "A combination of ``on_commit`` and ``on_merge_request``" 
   "on_merge", "A merge request was merged into the branch." 

These methods take named parameters ``to`` and ``from`` indicating direction of the gitlab
whose value is a regular expression to compare the branch names against.

SDP provides some default keywords for branch name regular expressions:

.. csv-table:: Default Branch Regular Expressions
   :header: "Keyword", "Regular Expression" 

   "master", "/^[Mm]aster$/"
   "develop", "/^[Dd]evelop(ment|er|)$/"
   "hotfix", "/^[Hh]ot[Ff]ix-/"
   "release", "/^[Rr]elease-(\d+.)*\d$/"

.. note:: 

    These branch name regular expressions are not a part of the Gitlab
    library but rather leveraged by defining :ref:`Keywords<Keywords>` in the SDP configuration file

==========================
Example Pipeline Templates
==========================

**Full example using keywords** 

.. code:: groovy 

    on_commit{
      continuous_integration()
    }

    on_merge_request to: develop, {
      continuous_integration()
      deploy_to dev
      parallel "508 Testing": { accessibility_compliance_test() },
              "Functional Testing": { functional_test() },
              "Penetration Testing": { penetration_test() }
      deploy_to staging
      performance_test()
    }

    on_merge to: master, from: develop, {
      deploy_to prod
      smoke_test()
    }

**Example using regular expressions directly**

.. code:: groovy 

    on_commit to: /^[Ff]eature-.*/, {
      // will be triggered on feature branches
    }
    on_merge_request from: /^[Ff]eature-.*/, to: develop, {
      // will be triggered on MR's from feature to develop
    }

**Example using on_change**

.. code:: groovy 

    on_change{
      // do CI on every commit or MR
      continuous_integration()
    }
    on_merge_request to: master, {
      // do some stuff on MR to master
    }
    on_merge to: master, {
      // MR was merged into master
    }

External Dependencies
=====================

- The gitlab library and the sdp library needs to be loaded as libraries inside your pipeline_config.groovy file.
