.. _a11y Library: 
----------------
The A11y Machine
----------------

`The A11y Machine`_ (or `a11ym` for short, spelled “alym”) is an **automated accessibility testing tool**
which **crawls** and **tests** pages of any Web application to produce detailed
reports. It validates pages against the following specifications/laws:

  * `W3C Web Content Accessibility Guidelines`_
    (WCAG) 2.0, including A, AA and AAA levels (`understanding levels of
    conformance`_)
  * U.S. `Section 508`_ legislation
  * `W3C HTML5 Recommendation`_ 

.. _The A11y Machine: https://github.com/liip/TheA11yMachine
.. _W3C Web Content Accessibility Guidelines: http://www.w3.org/TR/WCAG20/
.. _understanding levels of conformance: http://www.w3.org/TR/UNDERSTANDING-WCAG20/conformance.html#uc-levels-head
.. _Section 508: http://www.section508.gov/
.. _W3C HTML5 Recommendation: https://www.w3.org/TR/html/

Steps Contributed
=================

* accessibility_compliance_test()

Library Configuration Options
=============================

.. csv-table:: a11y Library Configuration Options
   :header: "**Field**", "**Description**", "**Default Value**"

   "url", "The url a11y will crawl and scan"

A target URL can be given. However `env.FRONTEND_URL` supersedes all
configurations. If no `env.FRONTEND_URL` is found then the provided target
URL is used. If no URL is provided an error is thrown.

.. code:: groovy
    
    libraries{
      a11y{
        url = "https://example.com"
      }
    }

Results
=======

The results of the scan are captured in an HTML report that gets archived
by jenkins.

Report Index
############

.. image:: ../../../docs/images/a11y/index.png

Report of a specific URL
########################

.. image:: ../../../docs/images/a11y/report.png
