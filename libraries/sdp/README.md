# SDP

The SDP library provides helper steps used by multiple libraries within sdp-libraries.

## Steps Provided
---

| Step | Description |
| ----------- | ----------- |
| ``inside_sdp_images(String image, Closure body)`` | helper function that wraps ``docker.image(<image>).inside{}`` to execute a portion of the pipeline inside the specified container image runtime environment |

| ``jteVersion`` | a multi-method step that provides utilities for determining the current JTE version. more docs below. |

Lifecycle Hooks

| Step | Hook | Purpose |
| ----------- | ----------- | ----------- |
| archive_pipeline_config() | `@Init` | Writes the aggregated pipeline configuration to a file and saves it as a build artifact |
| create_workspace_stash() | `@Validate` | If the pipeline job is a Multibranch Project, checkout the source code.  In either case, save a stash called "workspace" for other libraries to consume. |

## Library Configuration Options
---

SDP Library Configuration Options

| Field | Description | Default Value |
| ----------- | ----------- | ----------- |
| images.registry | This sets the registry the sdp library expects to find its Docker images | |
| images.repository | The first https://forums.docker.com/t/docker-registry-v2-spec-and-repository-naming-rule/5466[path component] in the repository name, e.g. if your images follow the format ``my-registry.com/sdp/*``, this would be *sdp* | |
| sdp.images.cred | Credentials used for the repository where different docker pipeline tools are stored | |
| sdp.images.docker_args | Arguments to use when starting the container. Uses the same flags as `docker run` | |

**Important** Unlike the Docker Library, the value in "registry" _does_ include the protocol (http/https)

## Example Configuration Snippet
---

```{ .groovy .annotate }
libraries{
  sdp{
    images{
      registry = "https://docker.pkg.github.com" // (1)
      repository = "boozallen/sdp-images" // (2)
      cred = "github" // (3)
    }
  }
}
```

1. the container registry that holds the SDP container images
2. the container image repository that holds the SDP container images
3. A jenkins credential ID to authenticate to the container registry

## `jteVersion`
---

`jteVersion` is a multi-method step that provides utilities for determining the current JTE version. This is particularly useful when making changes to support backwards compatibility. 

Methods

| Method | Description |
| `jteVersion.get()` | returns the current JTE version |
| `jteVersion.lessThan(String version)` | returns true if the current JTE version is less than the parameter |
| `jteVersion.lessThanOrEqualTo(String version)` | returns true if the current JTE version is less than or equal to the parameter |
| `jteVersion.greatherThan(String version)` | returns true if the current JTE version is greater than the parameter |
| `jteVersion.greaterThanOrEqualTo(String version)` | returns true if the current JTE version is greater than or equal to the parameter |
| `jteVersion.equalTo(String version)` | returns true if the current JTE version is equal to the parameter |

For example, 

```groovy
if (jteVersion.lessThan("2.1")){
  // code to run if current installed version is < 2.1
} else { 
  // code to run if current installed version is >= 2.1
}
```

## External Dependencies
---

* A Docker registry must be setup and configured. Credentials to the registry are also needed.
* A repository for the image being used by the given library is expected to be in the given registry.
* The repository name for the pipeline tools' images should be in the format  _"${images.registry}/${images.repository}/tool-name"_
* The Pipeline Utility Steps plugin is required 

## Troubleshooting
---

## FAQ
---
