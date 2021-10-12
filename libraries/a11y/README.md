# The A11y Machine

[The A11y Machine](https://github.com/liip/TheA11yMachine) (or `a11ym` for short, spelled “alym”) is an **automated accessibility testing tool**
which ***crawls*** and ***tests*** pages of any Web application to produce detailed reports. 

It validates pages against the following specifications/laws:

* [W3C Web Content Accessibility Guidelines](http://www.w3.org/TR/WCAG20/) (WCAG) 2.0, including A, AA and AAA levels ([understanding levels of conformance](http://www.w3.org/TR/UNDERSTANDING-WCAG20/conformance.html#uc-levels-head))
* U.S. [Section 508 legislation](http://www.section508.gov/)
* [W3C HTML5 Recommendation](https://www.w3.org/TR/html)

## Steps
---

| Step | Description |
| ----------- | ----------- |
| ``accessibility_compliance_test()`` | crawls the provided URL and performs accessibility compliance scanning |

## Configuration
---

| Field | Description | Default Value |
| ----------- | ----------- | ----------- |
| url | The url a11y will crawl and scan | |

A target URL can be given. However `env.FRONTEND_URL` supersedes all configurations. If no `env.FRONTEND_URL` is found then the provided target URL is used. If no URL is provided an error is thrown.

```groovy
libraries{
  a11y{
    url = "https://example.com"
  }
}
```

## Results
---

The results of the scan are captured in an HTML report that gets archived by jenkins.

### Report Index

![HTML Report Landing Page](../../assets/images/a11y/index.png)
### Report of a specific URL

![HTML Report Drill Down](../../assets/images/a11y/report.png)
## Dependencies
---