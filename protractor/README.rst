Protractor
----------

Protractor is a test framework built for Angular and AngularJS applications that is used for end-to-end testing. The framework simulates user activity using the web application by running a developer's tests on a real browser. It adds a layer of tests to help ensure that newly added front-end code does not break already existing functionality or the build itself.

Steps Provided
##############

* functional_test()

Library Configurations
######################

.. csv-table:: Protractor Library Configuration Options
   :header: "Field", "Description", "Default Value"

   "url", "Url for the website that will be tested", "none"
   "enforce", "Boolean value that determines if a build will fail if a Protractor test fails", "none"
   "config_file", "Name of the file where the Protractor configurations are set", "none"


Example Configuration Snippet
*****************************

.. code::

   libraries{
     protractor {
        url = "http://frontend-website.com"
        enforce = true
        config_file = "protractor.conf.js"
     }
   }