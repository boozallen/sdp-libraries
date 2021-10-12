# OWASP ZAP

[OWASP Zed Attack Proxy (ZAP)])https://www.owasp.org/index.php/OWASP_Zed_Attack_Proxy_Project) is a tool that can help you automatically find security vulnerabilities in your web applications while you are developing and testing your applications. Its also a great tool for experienced pentesters to use for manual security testing.

## Steps
---

| Step | Description |
| ----------- | ----------- |
| ``penetration_test()`` | Uses the OWASP ZAP cli to perform penetration testing against the configured URL |

## Library Configuration Options
---

OWASP ZAP Library Configuration Options

| Field | Description | Default Value | Options |
| target | The target url to pentest | none |  |
| vulnerability_threshold | Minimum alert level to include in report | High | one of "Ignore", "Low", "Medium", "High", or "Informational" |

`target` is set to `env.FRONTEND_URL` if available. If not then it uses the provided URL. If no URL is provided an error is thrown.

### Example Configuration Snippet

```groovy
libraries{
  owasp_zap{
    target = "https://example.com"
    vulnerability_threshold = "Low"
  }
}
```

## Results
---

![OWASP ZAP example](../../assets/images/owasp_zap/report.png)

## Troubleshooting
---

## FAQ
---