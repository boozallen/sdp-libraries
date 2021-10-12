# Create A New Library

This page will outline the steps for adding a new SDP library that's ready for others to consume and contribute to. 

## Ask yourself some questions
---

. Does the library already exist? :) 
. Could this functionality be added to an existing library? 
. Is this library going to be usable by anyone else who wants to use this tool? 

If the library doesn't already exist, wouldn't make more sense as an addition to an existing library, and represents a use case that will be applicable outside of your current situation then it's likely a good candidate for contribution! 

## Fork the SDP Libraries repository
---

This project follows a standard [Fork Contribution Model](https://gist.github.com/Chaser324/ce0505fbed06b947d962), so if you haven't, go ahead and fork the [SDP Pipeline Libraries Repository](https://github.com/boozallen/sdp-libraries). 

## Determine a name for the library
---

* A library's name is determined by the name of the directory that's going to contain the implemented steps.  
* The name should be all lowercase, snake_case, and the same as the tool or process being integrated. 
* Of course, ensure this tool has 

```bash
# from the root of the repository
mkdir libraries/<name_of_new_library>
```

## Implement the library's steps
---

Go on over to JTE's [Library Development](https://boozallen.github.io/sdp-docs/jte/2.2.2/library-development/getting_started.html) documentation to learn how to create libraries. 

There are a few conventions the SDP Pipeline Libraries have adopted, outlined below:

### Check SDP library's helper methods

The [SDP](./libraries/sdp/) exists to implement common functionality required by the other libraries.  It's worthwhile to see if any of those steps are going to be useful to you during library development.

### Add an SDP Pipeline Image (if necessary)

The SDP Pipeline Libraries try to install as few plugins on the Jenkins instance and as few tools on the underlying infrastructure as possible.  We run portions of the pipeline inside container images, leveraging them as runtime pipeline environments. The existing container images used for this purpose can be found in the [SDP Pipeline Images](https://github.com/boozallen/sdp-images) repository.  

If your library requires runtime dependencies, like a CLI, capture them in a container image and open a PR to the SDP Pipeline Images repository.

In your step implementations, the image that is to be used should be overridable but default to the image hosted via GitHub Package Registry on the SDP Pipeline Images repository. 

**Note** If your library requires runtime dependencies, your new library will not be accepted until the required image has been merged and published to the SDP Pipeline Images repository. 

## Add documentation for the library
---

### Create the documentation page

Libraries are required to have a documentation page to be accepted. 

To keep the library documentation consistent, copy the resources/README.template.md is copied into a new library's `README.md` as a starting point to fill in. 

### Update the landing page libraries table

The landing page for the SDP Pipeline Libraries has a table that outlines each library and a high-level description of the library.  Update this table in docs/index.md.

### Preview your documentation

You can run ``just servce`` at the root of the repository to build the documentation as static HTML, and view it at `localhost:8000`.

## Add unit tests
---

It's highly encouraged that unit tests be written for the library.  

. Tests go under the `test` directory in your library.
. Read the [Unit Testing Documentation](./unit_testing/index.html)
. Write some tests for your steps

## Add a library configuration file
---

To help prevent configuration errors, you can also [validate the library parameters](https://boozallen.github.io/sdp-docs/jte/2.2.2/library-development/parameterizing_libraries.html#_validating_library_configurations).

## Open a Pull Request
---

The library is now done!  At this point you should have: 

. A new library with steps implemented using the SDP library's helpers if necessary
. A new SDP Pipeline Image corresponding to the new library for its runtime dependencies (if necessary)
. Documentation for the library 
. Unit tests for the library 
. A strategy for validating the library's configuration parameters

These will all be confirmed during PR review.
