.. _OpenShift Library: 
---------
OpenShift
---------

OpenShift is an enterprise grade distribution of Kubernetes built by Red Hat. For information on 
deploying OpenShift inside Booz Allen's CSN AWS environment, refer to our 
:ref:`guide <deploy openshift on aws csn>`

This library allows you to perform deployments to static or ephemeral application environments with  
Helm_ 


Steps Provided
##############

* deploy_to
* ephemeral 

Library Configurations
######################

The configurations for the OpenShift library can be specified at multiple levels. Given this 
additional configurability the typical table of configuration options would be less clear than
examples, so we'll be breaking it down per configuration portion. 

OpenShift URL
*************
The OpenShift URL can be defined in the library spec or on a per application environment basis.

For example, it's common to have a cluster for lower environments with a separate cluster for
production.  You would specify this as follows: 

.. code:: groovy

    application_environments{
      dev{
        short_name = "dev"
        long_name = "Development"
      }
      test{
        short_name = "test"
        long_name = "Test"
      }
      prod{
        short_name = "prod"
        long_name = "Production" 
        openshift_url = "https://openshift.prod.example.com:8443" 
      }
    }
    libraries{
      openshift{
        url = "https://openshift.dev.example.com:8443" 
      }
    }

With this configuration, ``https://openshift.dev.example.com:8443`` would be used when deploying
to ``dev`` and ``test`` while ``https://openshift.prod.example.com:8443`` would be used when 
deploying to ``prod`` 

Helm Configuration
******************

We use Helm_ for a deployment mechanism to OpenShift.  Helm is a package manager and templating
engine for Kubernetes manifests.  Using Helm, the typical YAML manifests used to deploy to 
Kubernetes distributions can be templatized for reuse.  In our case, a different values file is
used for each static application environment. 

**Deploy the Tiller Server** 

You can configure Helm for multitenancy by using our 
:ref:`environment provisioning script <helm environment provisioning script>`. 

Instead of using Helm as a package manager by bundling the charts and deploying them to a chart
repository, we instead use a configuration repository as our infrastructure as code mechanism.

**Create Helm Configuration Repository** 

You'll need to create a GitHub repository to store the helm chart for your application(s). See the
`helm docs on provisioning a new chart`_ to get intialize the repository with the skeleton for your chart. 

How you choose to build your helm chart is up to you, you can put every api object in the ``templates``
directory or have subcharts for each individual microservice.  SDP doesn't care, as all it does is clone
the github repository and deploy the chart using the specified values file. 

**Values File Conventions** 

Given that we tag container images using the git SHA, SDP will clone your helm configuration repository and
update a key corresponding to the current version of each container image for each application environment. 

As such, a certain syntax is required in your values file.  You must have an ``image_shas`` key. SDP will
automatically add subkeys for each repositories under this ``image_shas`` with a value that is the git SHA.

`Given that YAML keys can't have hypens, hyphens in repository names will be replaced with underscores.`

.. code:: yaml 

    image_shas: 
      my_sample_application: abcdefgh
      another_repo: abcdef
  
you can add whatever other keys are necessary to appropriately parameterize your helm chart. 

**Helm Configurations for the Library** 

The helm configuration repository, github credential, tiller namespace, and tiller credential  can be configured
globally in the library spec and overriden for specific application environments. 

The values file to will default to ``values.${app_env.short_name}.yaml``, or can be overridden via ``app_env.chart_values_file``. 

The name of the release will default to ``app_env.short_name``, or can be overridden via ``app_env.tiller_release_name``

An example of helm configurations: 

.. code:: groovy

    application_environments{
      dev{
        short_name = "dev"
        long_name = "Development" 
        chart_values_file = "dev_values.yaml" 
      }
      test{
        short_name = "test" 
        long_name = "Test" 
        tiller_release_name = "banana" 

      }
      prod{
        short_name = "prod"
        long_name = "Production" 
        tiller_namespace = "rhs-tiller-prod" 
        tiller_credential = "rhs-tiller-prod" 
      }
    }
    libraries{
      openshift{
        helm_configuration_repository = "https://github.boozallencsn.com/Red-Hat-Summit/helm-configuration.git"
        helm_configuration_repository_credential = "github"
        tiller_namespace = "rhs-tiller"
        tiller_credential = "rhs-tiller"
      }
    }

Putting It All Together
***********************


.. csv-table:: OpenShift Library Configuration Options
   :header: "Field", "Description", "Default Value", "Defined On"

   "openshift_url", "The OpenShift Console URL when specified per app env", , "app_env" 
   "url", "The OpenShift Console URL when specified globally", , "library spec" 
   "helm_configuration_repository", "The GitHub Repository containing the helm chart(s) for this application", ,"both"
   "helm_configuration_repository_credential", "The Jenkins credential ID to access the helm configuration GitHub repository", , "both"
   "tiller_namespace", "The tiller namespace for this application", , "both"
   "tiller_credential", "The Jenkins credential ID referencing an OpenShift credential", , "both"
   "tiller_release_name", "The name of the release to deploy", , "app env"   
   "chart_values_file", "The values file to use for the release", , "app_env" 


.. code:: groovy

    application_environments{
      dev{
        short_name = "dev"
        long_name = "Development" 
        chart_values_file = "dev_values.yaml" 
      }
      test{
        short_name = "test" 
        long_name = "Test" 
        tiller_release_name = "banana" 

      }
      prod{
        short_name = "prod"
        long_name = "Production" 
        tiller_namespace = "rhs-tiller-prod" 
        tiller_credential = "rhs-tiller-prod" 
        openshift_url = "https://openshift.prod.example.com:8443" 
      }
    }
    libraries{
      openshift{
        url = "https://openshift.dev.example.com:8443" 
        helm_configuration_repository = "https://github.boozallencsn.com/Red-Hat-Summit/helm-configuration.git"
        helm_configuration_repository_credential = "github"
        tiller_namespace = "rhs-tiller"
        tiller_credential = "rhs-tiller"
      }
    }

External Dependencies
#####################

* Openshift is deployed and accessible from Jenkins
* Helm configuration repository creates
* Values files contain the ``image_shas`` key convention 
* A Jenkins credential exists to access helm configuration repository
* A Jenkins credential exists to login with OpenShift CLI 


.. _Helm: https://helm.sh/
.. _helm docs on provisioning a new chart: https://docs.helm.sh/helm/#helm-create