---
description: Leverages Protractor, a front-end unit testing utility, to perform unit tests
---

# Protractor

Protractor is a test framework built for Angular and AngularJS applications that's used for end-to-end testing.
The framework simulates user activity using the web application by running a developer's tests on a real browser.
It adds a layer of tests to help ensure that newly added front-end code doesn't break already existing functionality or the build itself.

## Steps

---

| Step | Description |
| ----------- | ----------- |
| `functional_test()` | leverages Protractor CLI to perform configured Protractor tests |

## Configuration

---

Library Configuration Options

| Field | Description | Default Value |
| ----------- | ----------- | ----------- |
| `url` | Address of the website that will be tested | |
| `enforce` | Boolean value that determines if a build will fail if a Protractor test fails | |
| `config_file` | Name of the file where the Protractor configurations are set | |

### Example Configuration Snippet

```groovy
libraries{
  protractor {
     url = "http://frontend-website.com"
     enforce = true
     config_file = "protractor.conf.js"
  }
}
```
