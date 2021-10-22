---
description: This library allows you to perform Maven commands. It requires installing Maven as a tool for Jenkins
---

# Maven

This library allows you to perform Maven commands. It requires installing Maven as a tool for Jenkins.

## Steps Provided
---

| Step | Description |
| ----------- | ----------- |
| ``run(Map params = [:], ArrayList<String> phases)`` | Runs the maven phases given along with the optional parameters passed through the params map. Named parameters include goals (List of Strings), properties (Map with String-String pairs), and profiles (List of Strings). | 

## Example Usage

``` groovy
maven.run(["clean", "install"], profiles: ["integration-test"])
```

## Library Configuration

The only configuration for the Maven library is the mavenId used to specify the installed version of Maven to use:
``` groovy
libraries{
  maven {
    mavenId = "maven"
  }
}
```

Where `"maven"` is the name of the installed Maven library in Jenkins.

## Library Dependencies

* Maven installed on Jenkins.
