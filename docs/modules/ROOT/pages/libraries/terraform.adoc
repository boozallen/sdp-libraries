= Terraform

This library leverages https://www.terraform.io/intro/index.html[Terraform] to manage deployments of Infrastructure as Code to different environments. 

== Steps Contributed

.Steps
|===
| *Step* | *Description* 

| ``deploy_to(application_environment)``
| performs a terraform apply 

|===

== Library Configuration Options

=== Working Directory 

The working directory from which to run Terraform commands can be specified on the application environment pass to ``deploy_to`` or within the library configuration. 

.Pipeline Configuration 
[source, groovy]
----
application_environments{
  dev
  prod{
    terraform{
      working_directory = "terraform-prod" 
    }
  }
}

libraries{
  terraform{
    working_directory = "default-directory"
  }
}
----

.Pipeline Template
[source, groovy]
----
/*
  because dev.terraform.working_directory is not set
  the library will fallback to the library's configuration
  and execute terraform commands within the "default" directory
*/
deploy_to dev 
/*
  because prod.terraform.working_directory is set to "terraform-prod"
  the terraform commands will be executed within ./terraform-prod 
*/
deploy_to prod 
----

[NOTE]
====
If the working directory is not defined on either the library configuration or the application environment then the default value `"."` will be used. 
====

=== Secrets 

This library allows you to configure secrets as environment variables.  This can be done in both the library configuration or application environments.  There are two types of secrets currently supported:  secret text and username/password credentials. These credentials must be stored are in the Jenkins credential store. 

.Library Secrets Syntax
[source, groovy]
----
libraries{
  terraform{
    secrets{
      someTextCredential{
        type = "text"
        name = "VARIABLE_NAME"
        id = "some-credential-id"
      }
      someUsernamePasswordCredential{
        type = "usernamePassword"
        usernameVar = "USER"
        passwordVar = "PASS"
        id = "some-credential-id"
      }
    }
  }
}
----

The name of each credential block is not important, and only used when describing configuration errors found by the step. 

To pass secrets on a per application environment basis, define a `app_env.terraform.secrets` block: 

.Application Environments Secrets Syntax
[source, groovy]
----
application_environments{
  prod{
    terraform{
      secrets{
        someTextCredential{
          type = "text"
          name = "VARIABLE_NAME"
          id = "some-credential-id"
        }
        someUsernamePasswordCredential{
          type = "usernamePassword"
          usernameVar = "USER"
          passwordVar = "PASS"
          id = "some-credential-id"
        }
      }
    }
  }
}
----

[IMPORTANT]
====
If the same secret block is defined on both the application environment and the library configuration, the application environment secret definition will be used.
====

== Providers 

The https://github.com/boozallen/sdp-images/tree/master/terraform[SDP Terraform Pipeline Image] can bundle custom providers, if necessary. 

=== Sysdig Provider

The https://github.com/draios/terraform-provider-sysdig[Sysdig Terraform Provider] is bundled with the terraform image. To configure this provider, it is advisable to create secrets for `SYSDIG_SECURE_API_TOKEN` and `SYSDIG_MONITOR_API_TOKEN`.  These environment variables can be consumed by the provider to configure the required secrets. 

== External Dependencies 

== Troubleshooting
