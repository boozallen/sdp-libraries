---
description: Leverages OWASP Dependency Check for scanning third party application dependencies
---

# OWASP Dependency Check

The OWASP Dependency Check library will use the namesake tool to scan a project's source code to identify components with known vulnerabilities.

* [Official Website](https://www.owasp.org/index.php/OWASP_Dependency_Check)
* [Documentation](https://jeremylong.github.io/DependencyCheck/)

## Steps

---

| Step                            | Description                                                                   |
| ------------------------------- | ----------------------------------------------------------------------------- |
| `application_dependency_scan()` | Uses the OWASP Dependency Check CLI to perform an application dependency scan |

## Configuration

---

OWASP Dependency Check Library Configuration Options

| Field                    | Description                                                                                                                                             | Default Value                      |
| ------------------------ | ------------------------------------------------------------------------------------------------------------------------------------------------------- | ---------------------------------- |
| `scan`                   | ArrayList of Ant style paths to scan                                                                                                                    | `[ '.' ]`                          |
| `exclude`                | ArrayList of Ant style paths to exclude                                                                                                                 | `[ ]`                              |
| `cvss_threshold`         | A number between 0 and 10, inclusive, representing the failure threshold for vulnerabilities (**note:** will never fail unless a threshold is provided) |                                    |
| `allow_suppression_file` | Allows whitelisting vulnerabilities using a suppression XML file                                                                                        | `true`                             |
| `suppression_file`       | Path to the suppression file (see [here](https://jeremylong.github.io/DependencyCheck/general/suppression.html) for how to create a suppression file)   | `dependency-check-suppression.xml` |
| `image_tag`              | The tag for the scanner Docker image used                                                                                                               | `7.3.0-8.6-2`                      |

## Example Configuration Snippet

---

```groovy
libraries {
  owasp_dep_check {
    scan = [ "src" ]
    cvss_threshold = 9 
  }
}
```

## Viewing The Reports

---

The `application_dependency_scan` step archives artifacts in multiple formats: HTML, JSON, JUnit XML, and CSV.

## CVSS Threshold & Scores

---

From the [Wikipedia article](https://en.wikipedia.org/wiki/Common_Vulnerability_Scoring_System),
> The Common Vulnerability Scoring System (CVSS) is a free and open industry standard for assessing the severity of computer system security vulnerabilities. Scores range from 0 to 10, with 10 being the most severe.

The pipeline can fail if a vulnerability is detected at or above a given threshold.
This threshold is set with the `cvss_threshold` configuration option.
For example, if `cvss_threshold` is set to 7, and a vulnerability with a CVSS score of 7.5 is detected, the pipeline will fail.
If the vulnerability remains, but the `cvss_threshold` is set to 9, the pipeline will pass the OWASP Dependency Check scan.

If you wish for the scan to pass regardless of the CVSS scores of detected vulnerabilities, don't set the `cvss_threshold` option.
