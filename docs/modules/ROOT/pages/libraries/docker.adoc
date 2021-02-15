= Docker

The Docker library will build docker images and push them into a docker repository.

== Steps Contributed

.Steps
|===
| *Step* | *Description* 

| ``build()``
| builds a container image, tagging it with the Git SHA, and pushes the image to the defined registry

| ``get_images_to_build()``
| inspects the source code repository based upon the configured ``build_strategy`` to determine which container images to build

| ``login_to_registry()``
| logs in to the configured container registry

| ``retag()``
| retags the container images determined by ``get_images_to_build()`` 

|===

== Example Configuration Snippet

[source,groovy]
----
libraries{
  docker {
    build_strategy = "dockerfile"
    registry = "docker-registry.default.svc:5000"
    cred = "openshift-docker-registry"
    repo_path_prefix = "proj-images"
    remove_local_image = true
    build_args{
      GITHUB_TOKEN{
        type = "credential"
        id = "github_token"
      }
      SOME_VALUE = "some-inline-value-here"
    }
  }
}
----
== Configurations

.Docker Configuration Options
|===
| Field | Description | Default Value | Required

| build_strategy
| Sets how the library will build the container image(s); Must be dockerfile, docker-compose, or modules
| dockerfile
| false

| registry
| Where the container images produced during the pipeline builds are going to be pushed to
| 
| true

| registry_protocol
| the protocol to prepend to the `registry` when authenticating to the container registry
| "https://"
| false

| cred
| Credentials used for the repository where different docker pipeline tools are stored.
| 
| true

| repo_path_prefix
| The part of the repository name between the registry name and the last forward-slash
| ""
| false

| remove_local_image
| Determines if the pipeline should remove the local image after building or retagging
| false
| false

| build_args
| A block of build arguments to pass to `docker build`. For more information, see below. 



|===

== Build Arguments

=== Static Inline Build Arguments

To pass static values as build arguments, set a field within the configuration block where the key is the build argument name and the value is the build argument value.

For example, 

[source,groovy]
----
libraries{
  docker{
    build_args{
      BUILD_ARG_NAME = "some-inline-argument" <1>
    }
  }
}
----
<1> This configuration would result in `--build-arg BUILD_ARG_NAME='some-inline-argument'` being passed to `docker build`

=== Secret Text Credentials 

To pass a secret value, ensure that a Secret Text credential type has been created and fetch the credential id from the Jenkins credential store. 

[source,groovy]
----
libraries{
  docker{
    build_args{
      GITHUB_TOKEN{ <1>
        type = "credential" <2>
        id = "theCredentialId" <3> 
      }
    }
  }
}
----
<1> This will result in the build argument `--build-arg GITHUB_TOKEN=<secret text>` being passed to `docker build`. The library will mask the value of the secret from the build log. 
<2> The type of "credential" must be set. This gives the library flexibilty in the future to support other build argument types
<3> This credential must exist and be a Secret Text credential in the Jenkins credential store. The library could be extended in the future to support other types of credentials, when necessary. 

== External Dependencies

* A Docker registry must be set up and configured. Credentials to the repository are also needed.
* Either the github or github_enterprise library needs to be loaded as a library inside your pipeline_config.groovy file.

== Troubleshooting

== FAQ