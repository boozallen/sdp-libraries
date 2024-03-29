---
description: Leverages PyTest, a Python unit testing library, to perform unit tests
---

# PyTest

This library will execute Python unit tests leveraging the [PyTest](https://docs.pytest.org/en/latest/) framework.

## Steps

| Step | Description |
| ----------- | ----------- |
| `unit_test()` | executes unit tests via PyTest |

## Configuration

---

Configuration Options

| Field | Description | Required | Default Value |
| ----------- | ----------- | ----------- | ----------- |
| `enforce_success` | Set to false if failing tests shouldn't fail the build | No  | `true` |
| `requirements_file` | Relative path within the repository pointing to a Python requirements file | No | `requirements.txt` |

```groovy
libraries{
  pytest{
    requirements_file = "path/to/my/requirements.txt"
  }
}
```

## Results

---

View an example of the HTML output that's been saved as a PDF [here](../../assets/attachments/pytest/pytest.pdf).

## Dependencies

---
