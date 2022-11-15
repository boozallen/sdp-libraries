---
description: This library allows you to 
---

# Cypress

This library allows you to

## Steps

| Step | Description |
| ----------- | ----------- |
| `test_ui()` |             |

## Configuration

``` groovy title='pipeline_config.groovy'
libraries {
  cypress {
    npm_script = 'npm run cy:run:myTestSuite'
    report_path = './test/cypress/my_reports/**'
    test_repo = '.'
    branch = 'main'
    container_image = 'cypress/browsers:node14.17.0-chrome91-ff89'
    container_registry_creds = 'my-registry-creds'
  }
}
```

## Dependencies

* None
