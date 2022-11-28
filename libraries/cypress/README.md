---
description: This library allows you to run end-to-end tests with Cypress
---

# Cypress

This library allows you to run end-to-end tests with [Cypress](https://www.cypress.io/).

## Steps

| Step                | Description                                                                           |
| ------------------- | ------------------------------------------------------------------------------------- |
| `end_to_end_test()` | Runs tests defined in the configured `npm_script` in your project `package.json` file |

## Configuration

| Field                      | Description                                                                                                               | Default Value                                |
| -------------------------- | ------------------------------------------------------------------------------------------------------------------------- | -------------------------------------------- |
| `npm_script`               | Cypress NPM script to run (defined in your `package.json` file)                                                           | N/A (Required)                               |
| `report_path`              | Path where Cypress reports can be found after tests are run (will be archived in Jenkins and accepts Ant-style wildcards) | N/A (Required)                               |
| `test_repo`                | Repository containing Cypress test code (leave as default if test code is in the same repository as your project)         | `.`                                          |
| `test_repo_creds`          | Test code repository credentials                                                                                          | `''`                                         |
| `branch`                   | If using a separate `test_repo` for Cypress test code, allows you to define the repository branch to use                  | `main`                                       |
| `container_image`          | Cypress test runner container image to use                                                                                | `cypress/browsers:node14.17.0-chrome91-ff89` |
| `container_registry`       | Container registry to use (if not hub.docker.com)                                                                         | `https://index.docker.io/v1/`                |
| `container_registry_creds` | Container registry credentials to use                                                                                     | `''`                                         |

## Example Configuration Snippet

``` groovy title='pipeline_config.groovy'
libraries {
  cypress {
    npm_script = 'npm run cy:run:myTestSuite'
    report_path = 'cypress/reports/**'
    test_repo = 'https://github.com/username/my-cypress-test-repository'
    test_repo_creds = 'my-github-creds'
    branch = 'main'
    container_image = 'cypress/browsers:node14.17.0-chrome91-ff89'
    container_registry = 'https://index.docker.io/v1/'
    container_registry_creds = 'my-registry-creds'
  }
}
```

## Dependencies

* None
