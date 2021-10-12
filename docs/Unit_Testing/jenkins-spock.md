# Jenkins Spock

We test pipeline libraries using [Jenkins-Spock](https://github.com/homeaway/jenkins-spock), a variation of the [Spock](http://spockframework.org/spock/docs) testing framework that has been designed around testing Jenkins pipelines. 

## Writing a Specification File
---

A "specification" is a list of features derived from business requirements. A specification file contains that list of features as unit tests, and those tests validate that the features work as expected. There should be a separate file for each pipeline step in your library.

Below is an outline of a specification file. It shows what you need to include in order to run tests, as well as some conventions for what to name methods and variables.  Create a groovy file with the same name as the class (such as ``MyPipelineStepSpec.groovy``) and use this outline to get you started, making sure to swap names with ones for your library.

## Sample Specification
---

1. Import the framework
2. Create a class extending `JTEPipelineSpecification`
3. Create a field to house the loaded step
4. Define a setup method where you will load the step
5. Write a test

```groovy

// Create a new class for the Spec
// The naming convention is the pipeline step's name, followed by Spec,
// all camel-cased starting w/ a capital.
public class MyPipelineStepSpec extends JTEPipelineSpecification {

  // Define the variable that will store the step's groovy code. This variable
  // follows the same naming variable as the class name, with Spec omitted.
  def MyPipelineStep = null

  // setup() is a fixture method that gets run before every test.
  // http://spockframework.org/spock/docs/1.2/spock_primer.html#_fixture_methods
  def setup() {
    // It's required to load the pipeline script as part of setup()
    // With the library monorepo, pipeline step groovy files can be found in "sdp/libraries"
    MyPipelineStep = loadPipelineScriptForTest("sdp/libraries/my_library/my_pipeline_step.groovy")
  }

  // Write a test (i.e. Feature Method) for each feature you wish to validate
  // http://spockframework.org/spock/docs/1.2/spock_primer.html#_feature_methods
  def "Successful Build Sends Success Result" () {
    setup:
      // unlike in the pipeline, the config object is not loaded during
      // unit tests. Use this to set it manually
      MyPipelineStep.getBinding().setVariable("config", [ field: "String" ])
    when:
      // This is the "stimulus". It does things so we can test what happens
      // Typically, you execute the step's groovy code like this
      MyPipelineStep()
    then:
      // Here's where you describe the expected response
      // If everything in here is valid, the test passes
      1 * getPipelineMock("sh")("echo \"field = String\"")
  }

}
```
