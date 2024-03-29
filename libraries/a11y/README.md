---
description: Leverages The A11y Machine to perform accessibility compliance scanning
---

# a11y

[The A11y Machine](https://github.com/liip/TheA11yMachine) is an automated accessibility testing tool which crawls and tests pages of a web application to produce detailed reports.

It validates pages against the following specifications/laws:

* [W3C Web Content Accessibility Guidelines](http://www.w3.org/TR/WCAG20/) (WCAG) 2.0, including A, AA and AAA levels ([understanding levels of conformance](http://www.w3.org/TR/UNDERSTANDING-WCAG20/conformance.html#uc-levels-head))
* U.S. [Section 508 legislation](http://www.section508.gov/)
* [W3C HTML5 Recommendation](https://www.w3.org/TR/html)

!!! warning "Deprecated"
    This library is no longer maintained because the A11y Machine is no longer maintained.
    Consider using [webhint](./webhint.md) instead.

## Steps

| Step                              | Description                                                                |
|-----------------------------------|----------------------------------------------------------------------------|
| `accessibility_compliance_test()` | crawls the provided website and performs accessibility compliance scanning |

## Configuration

| Field | Description                          | Default Value |
|-------|--------------------------------------|---------------|
| `URL` | The address a11y will crawl and scan |               |

A target `URL` can be given. However `env.FRONTEND_URL` supersedes all configurations.
If no `env.FRONTEND_URL` is found then the provided target `URL` is used. If no `URL` is provided an error is thrown.

```groovy
libraries{
  a11y{
    url = "https://example.com"
  }
}
```

## Results

The results of the scan are captured in an HTML report that gets archived by Jenkins.

### Report Index

![HTML Report Landing Page](../../assets/images/a11y/index.png)

### Report from a specific address

![HTML Report Drill Down](../../assets/images/a11y/report.png)

## Dependencies
