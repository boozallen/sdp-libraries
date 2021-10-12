# Google Lighthouse 

This library integrates [Google Lighthouse](https://developers.google.com/web/tools/lighthouse) to scan a frontend application for performance, accessibility compliance, search engine optimization, and best practice violations. 

The great part about this library is that developers can also use Google Lighthouse when developing locally in Chrome and these practices can be enforced via the pipeline. 

## Steps
---

| Step | Description |
| ----------- | ----------- |
| ``accessibility_compliance_scan()`` | performs a lighthouse analysis against the configured URL |

## Library Configuration Options
---

| Field | Type | Description | Default Value |
| ----------- | ----------- | ----------- | ----------- |
| url | String | The url to scan  |  |
| thresholds.performance.fail | Double | Performance scores less than or equal to this will fail the build  | 49.0 |
| thresholds.performance.warn | Double | Performance above the failure threshold but less than this will mark the build unstable | 89.0 |
| thresholds.accessibility.fail | Double | Accessibility scores less than or equal to this will fail the build  | 49.0 |
| thresholds.accessibility.warn | Double | Accessibility above the failure threshold but less than this will mark the build unstable | 89.0 |
| thresholds.best_practices.fail | Double | Best Practice scores less than or equal to this will fail the build  | 49.0 |
| thresholds.best_practices.warn | Double | Best Practice above the failure threshold but less than this will mark the build unstable | 89.0 |
| thresholds.search_engine_optimization.fail | Double | Search Engine Optimization scores less than or equal to this will fail the build  | 49.0 |
| thresholds.search_engine_optimization.warn | Double | Search Engine Optimization above the failure threshold but less than this will mark the build unstable | 89.0 |

```groovy
libraries{
  google_lighthouse{
    url = "https://google.com"
    thresholds{
      performance{
        fail = 75
      }
    }
  }
}
```

## Results
---

An example html report has been saved to PDF.

[click here to download.](../../assets/attachments/google_lighthouse/google_lighthouse.pdf)

## External Dependencies
---

* network connectivity from the Jenkins build agent running the scan to the provided url 

## Troubleshooting
---
