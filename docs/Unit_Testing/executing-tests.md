# Test Execution

For our SDP Pipeline Libraries, tests are placed in a directory called `tests` within the library directory.

## Executing tests with Gradle
---

This repo has been set up to run Jenkins-Spock tests using Gradle. 

Currently, Gradle 6.3.0 running on JDK 8 is required. These can be downloaded on OSX via:

```bash
# gradle via sdkman: https://sdkman.io/
curl -s "https://get.sdkman.io" | bash
source "/Users/joshuaearnest/.sdkman/bin/sdkman-init.sh"
sdk install gradle 6.3

# java8
brew install adoptopenjdk8
export JAVA_8_HOME=$(/usr/libexec/java_home -v1.8)
alias java8='export JAVA_HOME=$JAVA_8_HOME'
java8
```

See the [contributing page](../../contribute/index.html#testing) for instructions on running unit tests.