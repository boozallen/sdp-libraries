# SDP Pipeline Libraries

This repository contains [Booz Allen's](https://boozallen.com) pipeline libraries that integrate with the [Jenkins Templating Engine](https://plugins.jenkins.io/templating-engine/).

If you want to learn more, the best place to get started is the [documentation](https://boozallen.github.io/sdp-docs/sdp-libraries/). 

## Usage

In order to use the different libraries in this repository, you can configure this repository as a library source, for a detailed example of how to do this you may refer to [this lab](https://boozallen.github.io/sdp-docs/learning-labs/1/jte-the-basics/3-first-libraries.html#_configure_the_library_source). 

It is recommended that rather than using the master branch you pin your library source to a particular github release such as: https://github.com/boozallen/sdp-libraries/tree/release/2.0/libraries [like 2.0].  This helps to ensure that you have greater control in version management. 

Also ensure that in addition to whichever library you wish to use you include the `sdp` library. This helps to resolve a number of dependency errors you may otherwise face.

### Configuring the sdp library

As a dependency for every other library, it is important that the sdp library not only be included but also configured properly. For instructions on how to configure this library, please reference the [sdp guide](https://boozallen.github.io/sdp-docs/sdp-libraries/libraries/sdp.html)

## Repository Structure

A detailed description is available in the [contributing guide](./CONTRIBUTING.md)

## Contributing

We accept contributions via a fork-based development workflow. See the [contributing guide](./CONTRIBUTING.md).