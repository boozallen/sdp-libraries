# Contributing Guide

## Repository Structure
---

| Repository Component | Description                                                                               |
|----------------------|-------------------------------------------------------------------------------------------|
| `README.md`          | Repository overview, displays on Github.                                                  |
| `docs/index.md`      | Repository overview, gets compiled as the landing page for the documentation              |
| `docs`               | Documentation not specific to a particular library, and assets for library docs           |
| `CONTRIBUTING.md`    | Explains how to develop against this repository                                           |
| `libraries`          | The base directory where the libraries are stored                                         |

### Library Structure

Within the `libraries` directory, there are several components to be aware of:

| Component | Description                                         |
|-----------|-----------------------------------------------------|
| README.md | The library's documentation page                    |
| steps     | The steps contributed by the library                |
| resources | Any reusable content for consumption by the library |
| src       | The classes contributed by the library              |
| test      | The unit tests for the library                      |

For example, the current repository's `a11y` library: 

``` text
libraries/a11y
├── README.md
├── resources
├── src
├── steps
│   └── accessibility_compliance_test.groovy
└── test
    └── AccessibilityComplianceTestSpec.groovy
```

## Required Tools
---

| Tool | Purpose |
| ----------- | ----------- |
| [Gradle](https://gradle.org) | Used to run unit tests |
| [Just](https://github.com/casey/just) | A task runner. Used here to automate common commands used during development. |
| [Docker](https://www.docker.com/get-started) | Used to build the documentation for local preview |

## Create a Library
---

Create a new library by running:

```bash
just create <library name>
```

This will produce a folder of the library name with the library structure described above stubbed out. Information for developing a new library can be found in [Create a New Library](./create-new-library/) page.

## Documentation
--- 

This repository uses [Material for MkDocs](https://squidfunk.github.io/mkdocs-material/) to build the documentation.

### Building the Docs

To build the documentation, run: 

``` bash
just build
```

This will build the documentation and produce static html in the `site` directory.

### Live Reloading

To see changes live as you make them, run: 

``` bash
just serve
```

The documentation will be accessible at http://localhost:8000.

## Testing
---

Unit tests can be written using [Jenkins Spock](https://github.com/ExpediaGroup/jenkins-spock).

These tests should go in the `test` directory for each library.

To run all the tests, run: 

``` bash
just test
```

The gradle test report is published to `target/reports/tests/test/index.html`. 

### For a specific library

To run tests for a specific library, `docker` for example, run:

``` bash
just test '*docker*'
```

### For a specific Specification file

To run tests for a specific Specification file, `test/docker/BuildSpec.groovy` for example, run:

``` bash
just test "*.BuildSpec"
```

## Linting
---

This repository uses [npm-groovy-lint](https://github.com/nvuillam/npm-groovy-lint) with the recommended codenarc profile for Jenkins. 

The `.groovylintrc.json` can be used to tune the rule profile. 

To lint the libraries, run: 

``` bash
just lint
```

The output will go to standard out.

## Release Management
---

This repository automates the create of release branches and tags as well as publishing the documentation for each version.

### Release Automation

To cut a new release, run: 

``` bash
just release $version
```

Which will:

1. create a `release/$version` branch
2. create a `$version` tag
3. publish the documentation for the version and upate the `latest` documentation alias


### Automated Changelogs

[Release Drafter](https://github.com/release-drafter/release-drafter) is used to automate release note updates as Pull Requests are opened to `main`. 

The configuration for Release Drafter exists in the `.github/release-drafter.yml` file and uses GitHub Actions. 