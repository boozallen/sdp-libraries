.. _Owasp Zap Library: 
---------
OWASP ZAP
---------

`OWASP Zed Attack Proxy (ZAP)`_ is a tool that can help
you automatically find security vulnerabilities in your web applications while
you are developing and testing your applications. Its also a great tool for
experienced pentesters to use for manual security testing.

Steps Contributed
#################
* penetration_test

Library Configuration Options
#############################

.. csv-table:: OWASP ZAP Library Configuration Options
   :header: "Field", "Description", "Default Value", "Options"

   "target", "The target url to pentest", "none", 
   "vulnerability_threshold", "Minimum alert level to include in report", "High", "Ignore | Low | Medium | High | Informational"

:code:`target` is set to :code:`env.FRONTEND_URL` if available. If not then it uses the provided
URL. If no URL is provided an error is thrown.

Example Configuration Snippet
*****************************

.. code::

   libraries{
     owasp_zap{
       target = "https://example.com"
       vulnerability_threshold = "Low"
     }
   }

Results
#######
.. image:: ../../images/owasp_zap/report.png
   :alt: OWASP ZAP example

.. _OWASP Zed Attack Proxy (ZAP): https://www.owasp.org/index.php/OWASP_Zed_Attack_Proxy_Project