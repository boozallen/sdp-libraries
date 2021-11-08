---
description: Uses docker to build and publish container images, tagging them with the Git SHA
---

# Docker

The Docker library will build docker images and push them into a docker repository.

## Steps

---

| Step | Description |
| ----------- | ----------- |
| ``build()``| builds a container image, tagging it with the Git SHA, and pushes the image to the defined registry |
| ``buildx()``| builds a multi-architecture image using buildx emulation, and pushes the image or images to the defined registry
| ``get_images_to_build()`` | inspects the source code repository based upon the configured ``build_strategy`` to determine which container images to build |
| ``login_to_registry()``| logs in to the configured container registry |
| ``retag()``| retags the container images determined by ``get_images_to_build()`` |

## Example Configuration Snippet

---

```groovy
libraries{
  docker {
    build_strategy = "dockerfile"
    registry = "docker-registry.default.svc:5000"
    cred = "openshift-docker-registry"
    repo_path_prefix = "proj-images"
    image_name = "my-container-image"
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
```

## Configuration

---

| Field | Description | Default Value | Required |
| ----------- | ----------- | ----------- | ----------- |
| build_strategy | Sets how the library will build the container image(s); Must be dockerfile, docker-compose, or modules | dockerfile | false |
| registry | Where the container images produced during the pipeline builds are going to be pushed to |  | true |
| registry_protocol | the protocol to prepend to the `registry` when authenticating to the container registry | "https://" | false |
| cred | Credentials used for the repository where different docker pipeline tools are stored. |  | true |
| repo_path_prefix | The part of the repository name between the registry name and the last forward-slash | "" | false |
| image_name | Name of the container image being built | `env.REPO_NAME` | false |
| remove_local_image | Determines if the pipeline should remove the local image after building or retagging | false | false |
| build_args | A block of build arguments to pass to `docker build`. For more information, see below. | | false |
| setExperimentalFlag | If the docker version only has buildx as an experimental feature then this allows that flag to be set | false | false
| same_repo_different_tags | When building multiple images don't change the repo name but append the key name to the tag | false | false
| buildx[].name { } | the key name to the map of the specific element of the buildx array |  | true
| buildx[].useLatestTag | Add an additional latest tag to the image being built on top of the other tag | false | false
| buildx[].tag | Override the tag with a string | git sha from commit | false
| buildx[].context | Dockerfile context for that image | "." | false
| buildx[].dockerfile_path | Dockerfile location and name for that image | "Dockerfile" | false
| buildx[].platforms | array of platforms to be built for that image | linux/amd64 | false
| buildx[].build_args | A block of build arguments to pass for that element to `docker buildx`. For more information, see below.

## Build Arguments

---

### Static Inline Build Arguments

To pass static values as build arguments, set a field within the configuration block where the key is the build argument name and the value is the build argument value.

For example,

```{ .groovy .annotate }
libraries{
  docker{
    build_args{
      BUILD_ARG_NAME = "some-inline-argument" // (1)
    }
  }
}
```

1. This configuration would result in `--build-arg BUILD_ARG_NAME='some-inline-argument'` being passed to `docker build`

### Secret Text Credentials

To pass a secret value, ensure that a Secret Text credential type has been created and fetch the credential id from the Jenkins credential store.

```{ .groovy .annotate }
libraries{
  docker{
    build_args{
      GITHUB_TOKEN{ // (1)
        type = "credential" // (2)
        id = "theCredentialId" // (3) 
      }
    }
  }
}
```

1. This will result in the build argument `--build-arg GITHUB_TOKEN=<secret text>` being passed to `docker build`. The library will mask the value of the secret from the build log.
2. The `type` of `credential` must be set. This gives the library flexibility in the future to support other build argument types.
3. This credential must exist and be a *Secret Text credential* in the Jenkins credential store. The library could be extended in the future to support other types of credentials, when necessary.

## Buildx Configuration

### Buildx Overview

Go to [Docker Buildx](https://docs.docker.com/buildx/working-with-buildx/) to learn more about buildx and the requirements for it.

The build strategy must be set to `'buildx'` to use the buildx step.

### Use Cases

This step provides covers three use cases for building multi-architecture.

#### 1. Single docker image name with one tag

Example: repo/example:1.0 that supports amd64, arm64, armv7

* Use this when the pipeline can build multiple architectures into a single docker image manifest.
* This method of building the image requires that the base image also supports all the architectures that the pipeline is building for.

Example Configuration Snippet for buildx Single docker image name with one tag:

``` groovy
libraries{
  docker {
        build_strategy = "buildx"
        registry = "docker-registry.default.svc:5000"
        cred = "docker_creds"
        repo_path_prefix = "java"
        buildx {
            name {
                build_args {
                    BASE_IMAGE = "alpine:3.12"
                }
                platforms = ["linux/amd64","linux/arm64","linux/arm/v7"]
                useLatestTag = true          
            }
        }
    }
}
```

Generated buildx command from above:

``` bash
docker buildx build . -t docker-registry.default.svc:5000/java/example:<insert git sha> -t docker-registry.default.svc:5000/java/example:latest --platform linux/amd64,linux/arm64,linux/arm/v7 --build-arg=BASE_IMAGE=alpine:3.12 --push
```

#### 2. Single docker image name with multiple tags

Example: repo/example:1.0-amd64 repo/example:1.0-arm64 where each image supports a different architecture

* Use this when there is no multi-architecture base image that can be used to build a single image manifest.
* Buildx is an array of maps that are separated by unique keys. This allows the pipeline to use the same Dockerfile with a parameterized base image or multiple Dockerfiles.
* This method requires that the `same_repo_different_tags` flag is set to `true` and for each element key in buildx to be unique.
* There can only be one element that can use the `useLatestTag` as it will throw an error due to the pipeline attempting to overwrite another image being built.

Example Configuration Snippet for buildx Single docker image name with one tag:

``` groovy
libraries{
  docker {
        build_strategy = "buildx"
        registry = "docker-registry.default.svc:5000"
        cred = "docker_creds"
        repo_path_prefix = "java"
        same_repo_different_tags = true
        buildx {
            amd64 {
                build_args {
                    BASE_IMAGE = "alpine:3.12"
                }
                platforms = ["linux/amd64"]
                useLatestTag = true
                tag = "1.0"         
            }
            arm64 {
                build_args {
                    BASE_IMAGE = "alpine:3.12"
                }
                platforms = ["linux/arm64"]
                tag = "1.0"
            }
        }
    }
}
```

Generated buildx command from above:

``` bash
docker buildx build . -t docker-registry.default.svc:5000/java/example:1.0-amd64 -t docker-registry.default.svc:5000/java/example:latest --platform=linux/amd64 --build-arg=BASE_IMAGE=alpine:3.12 --push
docker buildx build . -t docker-registry.default.svc:5000/java/example:1.0-arm64 --platform=linux/arm64 --build-arg=BASE_IMAGE=alpine:3.12 --push
```

#### 3. Multiple docker image names with multiple tags

Example: example-big:1.0 and example-small:1.0 where each image has its own list of architectures

* Use this when there is a single repo with multiple images that need to be built for multiple architectures.
* Each element's key must be unique for this to build or else it will override previous images and fail.

Example Configuration Snippet for buildx Single docker image name with one tag:

``` groovy
libraries{
  docker {
        build_strategy = "buildx"
        registry = "docker-registry.default.svc:5000"
        cred = "docker_creds"
        repo_path_prefix = "java"
        buildx {
            jre {
                build_args {
                    BASE_IMAGE = "alpine:3.12"
                }
                platforms = ["linux/amd64","linux/arm64","linux/arm/v7"]
                tag = "1.0"         
            }
            jdk {
                build_args {
                    BASE_IMAGE = "alpine:3.12"
                }
                platforms = ["linux/amd64","linux/arm64","linux/arm/v7"]
                tag = "1.0"
            }
        }
    }
}
```

Generated buildx commands from above:

``` bash
docker buildx build ./jdk -t docker-registry.default.svc:5000/java/example-jdk:1.0 --platform linux/amd64,linux/arm64,linux/arm/v7 --build-arg=BASE_IMAGE=alpine:3.12 --push
docker buildx build ./jre -t docker-registry.default.svc:5000/java/example-jre:1.0 --platform linux/amd64,linux/arm64,linux/arm/v7 --build-arg=BASE_IMAGE=alpine:3.12 --push
```

## External Dependencies

---

* A Docker registry must be set up and configured. Credentials to the repository are also needed.
* Either the `github` or `github_enterprise` library needs to be loaded as a library inside your `pipeline_config.groovy` file.
* Pipelines that use the **buildx** step need to be built on a node that has the correct Docker version with buildx support and the required emulator set up. See [Docker Buildx](https://docs.docker.com/buildx/working-with-buildx/) for how to set up a node with the right configurations.
* Buildx enabled nodes needs to be set up with buildkit builders that support the architectures required for the step to work.

## Troubleshooting

---

## FAQ

---
