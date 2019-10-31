.. _`Contribution Guide`:

==================
Contribution Guide
==================

Thanks for taking the time to read this! Whether you have an idea for a great
feature, or have already made some improvements that you want to contribute,
we really value your help in helping this project grow.

This guide should help you find answers to common questions:

* `What is an SDP library?`_
* `How can I contribute?`_
* `What coding or style conventions should I follow?`_
* `How is the documentation formatted?`_
* `How do I test my changes?`_
* `Where can I report a bug?`_
* `Where can I make a feature suggestion?`_
* `Where can I ask other questions?`_


What is an SDP library?
-----------------------

Glad you asked! An SDP library is a collection of Jenkins pipeline steps that
can be loaded and called using the Templating Engine plugin. These libraries
are usually for using a certain tool (e.g. GitHub) in a software delivery
pipeline. Check out our `documentation on SDP libraries`_ for more information,
and browse this repository for examples.


How can I contribute?
---------------------

Start by `opening a GitHub Pull Request`_ to the latest versioned branch
(e.g. ``1.1``) and filling out the template. We're incredibly grateful for
contributions, including

* new SDP libraries
* new features for existing SDP libraries
* improvements to existing SDP libraries
* bug fixes

If you're looking to contribute a feature suggestion, or want to help
identify a bug, we appreciate that, too!


What coding or style conventions should I follow?
-------------------------------------------------

While we're still in the process of establishing a style guide, here are a few
general conventions we've adopted:


* Groovy files that define pipeline steps use snake_case for method and variable names
* Indents are two (2) spaces.

* Pipeline steps should not take any arguments. Any variables should be set in
  the `pipeline configuration`_
* Pipeline steps' code should be within a `stage`_ and a `node`_

* Application source code is stored in a "workspace" `stash`_.

  * Unstash the workspace if using the source code

  * Stash the workspace again to capture any changes used by other steps


A good general reference is Cloudbees `Top 10 Best Practices for Jenkins Pipeline Plugin`_.
For those unfamiliar with Groovy, Apache has a good `Groovy Style Guide`_ covering
some of the general differences between Groovy and other languages.
As a general note, the SDP uses *scripted* Jenkins pipeline syntax, as opposed
to *declarative* pipeline syntax.

How is the documentation formatted?
-----------------------------------

Our documentation, including this guide, is written in reStructuredText, which
is a markup language similar to Markdown. If you're unfamiliar with
reStructuredText, there are README.rst files in existing libraries for
reference, and `resources are available online`_.

We ask that the documentation include


* A general description of the library

* The pipeline steps the library contributes

* Information about any external configuration the library uses, including

  * Any fields in the library block

  * The datatype of those fields, and whether or not they're required

  * Any other template primitives used (e.g. app_env's)

* Any external dependencies your library needs to work

  * Jenkins credentials

  * Content in the source code repository

  * Web services and related setup

  * Any Jenkins plugins or other libraries/steps the library depends on

.. it looks silly spaced out, but that's how to make it look pretty on the webpage...

How do I test my changes?
-------------------------

Check out `our documentation on writing unit tests`_.


Where can I report a bug?
-------------------------

One way is to create a `GitHub issue`_. If you do, please

* include brief description
* list versions for Jenkins, the templating engine, and any plugins or other software that might be relevant
* describe how to recreate the issue
* give it the *bug* label, and any other relevant labels

You can also talk with us directly on `our Gitter channel`_, and other Booz
Allen Hamilton employees can reach us through the sdp-users slack channel.


Where can I make a feature suggestion?
--------------------------------------

While we ask that GitHub issues be limited to bugs and other defects, we
welcome feature suggestions in `our Gitter channel`_ and internal sdp-users
slack channel.

You can also submit a feature suggestion along with the actual feature in a pull
request, which we appreciate immensely!


Where can I ask other questions?
--------------------------------

If you have questions that `our documentation`_ can't answer, you can reach us
through the Gitter and Slack channels mentioned above.



.. _documentation on SDP libraries: https://jenkinsci.github.io/templating-engine-plugin/pages/Library_Development/getting_started.html
.. _opening a GitHub Pull Request: https://help.github.com/en/articles/creating-a-pull-request
.. _pipeline configuration: https://jenkinsci.github.io/templating-engine-plugin/pages/Library_Development/externalizing_config.html#externalizing-library-configuration
.. _stage: https://jenkins.io/doc/book/pipeline/#stage
.. _node: https://jenkins.io/doc/book/pipeline/#node
.. _stash: https://jenkins.io/doc/pipeline/steps/workflow-basic-steps/#stash-stash-some-files-to-be-used-later-in-the-build
.. _Top 10 Best Practices for Jenkins Pipeline Plugin: https://www.cloudbees.com/blog/top-10-best-practices-jenkins-pipeline-plugin
.. _Groovy Style Guide: https://groovy-lang.org/style-guide.html
.. _resources are available online: http://docutils.sourceforge.net/rst.html#user-documentation
.. _our documentation on writing unit tests: https://boozallen.github.io/sdp-libraries/.docs/pages/unit-testing/index.html
.. _GitHub issue: https://help.github.com/en/articles/creating-an-issue
.. _our Gitter channel: https://gitter.im/jenkinsci/templating-engine-plugin
.. _our documentation: https://boozallen.github.io/sdp-docs/html/index.html
