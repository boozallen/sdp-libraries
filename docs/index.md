# SDP Pipeline Libraries

The Solution Delivery Platform's open source pipeline libraries plug in to the [Jenkins Templating Engine](https://boozallen.github.io/sdp-docs/jte/2.2.2/index.html) to accelerate the development of a DevSecOps pipeline. 

**Important** For any relevant upgrade notes about the SDP Pipeline Libraries, checkout the [GitHub Releases](https://github.com/boozallen/sdp-libraries/releases).

## Motivation
---

The Jenkins pipeline-as-code that is developed to perform various tool integrations is largely undifferentiated.  That is to say, it doesn't really matter what project you're working on - the pipeline code that's written can be reused anywhere if the configuration is appropriately externalized.  

These libraries serve as an open source, reusable portfolio of tool integrations that can help us speak a common language and leverage a common framework when implementing CI/CD pipelines. 

## Approach
---

### Container Images as Pipeline Run Time Environments

Maintaining tool installations on a Jenkins instance can be a configuration management nightmare.  Trying to keep straight 3 different versions of Java, Maven, Ant, Gradle, and so on within your Jenkins instance quickly leads to a bloated and difficult to maintain instance. 

Furthermore, when tools are installed directly on Jenkins build agents it can be difficult to rapidly introduce new features to the pipeline. 

We use container images to decouple the Jenkins infrastructure from the tools that the pipeline needs for building, testing, and deploying applications. 

Each library, rather than direclty invoke a tool, will leverage helpers from the ``sdp`` library to execute portions of the pipeline inside of container images. 

These images can be found in the [Booz Allen SDP Images GitHub Repository](https://github.com/boozallen/sdp-images) and are hosted through the GitHub Package Registry. 

### The ``sdp`` Library

If using the SDP Pipeline Libraries as a [Library Source](https://boozallen.github.io/sdp-docs/jte/2.2.2/library-development/library_sources/library_sources.html) for your pipeline, then you **must** include the ``sdp`` library.  This library containers helper functions such as ``inside_sdp_image()`` to facilitate the use of the SDP Pipeline Container Images as run time environments. 

## Requirements
---

Your Jenkins build agents must have Docker installed due to the above-mentioned use of container images in the SDP Pipeline Libraries. 
