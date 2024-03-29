# Create A New Library

This page will outline the steps for adding a new SDP library that's ready for others to consume and contribute to.

## Ask yourself some questions

---

* Does the library already exist? :)
* Could this functionality be added to an existing library?
* Is this library going to be usable by anyone else who wants to use this tool?

If the library doesn't already exist, wouldn't make more sense as an addition to an existing library,
and represents a use case that will be applicable outside of your current situation then it's likely a good candidate for contribution!

## Fork the repository

---

This project follows a standard [Fork Contribution Model](https://gist.github.com/Chaser324/ce0505fbed06b947d962),
so if you haven't, go ahead and fork the [SDP Pipeline Libraries Repository](https://github.com/boozallen/sdp-libraries).

## Determine a name for the library

---

* A library's name is determined by the name of the directory that's going to contain the implemented steps
* The name should be all lowercase, snake_case, and the same as the tool or process being integrated

```bash
# from the root of the repository
just create <name_of_new_library>
```

## Implement the library's steps

---

Go on over to JTE's [Library Development](https://jenkinsci.github.io/templating-engine-plugin/2.3/concepts/library-development/overview/) documentation to learn how to create libraries.

There are a few conventions the SDP Pipeline Libraries have adopted, outlined below:

### Review the `sdp` library's helper methods

The [SDP library](./libraries/sdp/) exists to implement common functionality required by other libraries.
It's worthwhile to see if any of those steps are going to be useful to you during library development.

### Add a container image (if necessary)

The SDP Pipeline Libraries try to install as few plugins on the Jenkins instance and as few tools on the underlying infrastructure as possible.
Part of the pipeline runs inside container images, leveraging them as runtime pipeline environments.
The existing container images used for this purpose can be found in the [SDP Pipeline Images](https://github.com/boozallen/sdp-images) repository.

If your library requires runtime dependencies, like a CLI, capture them in a container image and open a PR to the SDP Pipeline Images repository.

In your step implementations, the image that's used should be overrideable but default to the image hosted via GitHub Package Registry on the SDP Pipeline Images repository.

**Note** If your library requires runtime dependencies, your new library won't be accepted until the required image has been merged and published to the SDP Pipeline Images repository.

## Add documentation for the library

---

### Create the documentation page

Libraries are required to have a documentation page to be accepted.

To keep the library documentation consistent, copy the [resources/README.template.md](https://github.com/boozallen/sdp-libraries/blob/main/resources/docs/README.template.md) is copied into a new library's `README.md` as a starting point to fill in.

### Update the landing page libraries table

The landing page for the [SDP Pipeline Libraries](../libraries/README.md) contains a table that outlines each library and a high-level description of the library.
To make sure new library descriptions are added to the list, be sure to fill out the Frontmatter description block at the top of the new library's `README.md` file:

```markdown
---
description:
---
```

### Preview your documentation

You can run `just serve` at the root of the repository to build the documentation as static HTML, and view it at <localhost:8000>.

## Add unit tests

---

It's highly encouraged that unit tests be written for the library.  

* Tests should be placed in the `test` directory within the library directory
* Read the [Unit Testing Documentation](../concepts/unit-testing/index.md)
* Write some tests for your steps

## Add a library configuration file

---

To help prevent configuration errors, you can also [validate the library parameters](https://jenkinsci.github.io/templating-engine-plugin/2.3/concepts/library-development/library-configuration-file/).

## Open a Pull Request

---

The library is now ready for review! At this point you should have:

* A new library with steps implemented using the SDP library's helpers if necessary
* A new SDP Pipeline Image corresponding to the new library for its runtime dependencies (if necessary)
* Documentation for the library
* Unit tests for the library
* A strategy for validating the library's configuration parameters

These will all be confirmed during PR review.
