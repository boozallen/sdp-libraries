---
description: A customizable linting tool that helps you improve your site's accessibility, speed, cross-browser compatibility, and more by checking your code for best practices and common errors
---

# Webhint

[Webhint](https://webhint.io) is a customizable linting tool that helps you improve your site's accessibility,
speed, cross-browser compatibility, and more by checking your code for best practices and common errors.

## Steps

---

| Step | Description |
| ----------- | ----------- |
| `accessibility_compliance_scan()` | generates website developer hints from the given URL |

## Library Configuration Options

---

Configuration Options

| Field | Description | Default Value |
| ----------- | ----------- |  ----------- |
| `url` | web address to analyze | |
| `extender` | Array - Optional - Hint types. See [the documentation](https://webhint.io/docs/user-guide/configurations/configuration-development/) for more information. | `["accessibility"]` |
| `failThreshold` | Optional - Hint limit at which the jenkins build will fail | 25 |
| `warnThreshold` | Optional - Hint limit at which the jenkins build will issue a warning | 10 |

```groovy
libraries{
  url = "your_url_here"
  extender = ["progressive-web-apps"]
  failThreshold = 35
  warnThreshold = 25
}
```

## Results

---

[View an example HTML report, saved to PDF](../assets/attachments/webhint/webhint_mockaroo.pdf).

## External Dependencies

---

* none

## Troubleshooting

---
