# Writing Tests

Now that you've laid the groundwork for your tests, it's time to write them. These are the "Feature Methods" because there should be one for each feature. Some of the things to write tests for are:

. Things are built correctly (objects, string variables, maps, etc.)
. Conditional Hierarchies function as expected
. Variables get passed correctly
. Things fail when they're supposed to

Once you know the feature you're testing, like "Pipeline Fails When Config Is Undefined", write a feature method for it:

```groovy
def "Pipeline Fails When Config Is Undefined" () {

}
```

Now create a setup "block" to define some do some pre-test preparation not covered by the `setup()` fixture method. In this example, the binding
variable "config" is set to null, and a mock for the `error` pipeline step is created.

```groovy
def "Pipeline Fails When Config Is Undefined" () {
  setup:
    explicitlyMockPipelineStep("error")
    MyPipelineStep.getBinding().setVariable("config", null)
}
```

Now I need to execute the pipeline step and test the response. This happens in the "when" and "then" blocks, respectively. In this example, the pipeline step
is called (with no parameters), and I state that I expect the `error` step to be called exactly once with the message `"ERROR: config is not defined"`

```groovy
def "Pipeline Fails When Config Is Undefined" () {
  setup:
    explicitlyMockPipelineStep("error")
    MyPipelineStep.getBinding().setVariable("config", null)
  when:
    MyPipelineStep() // Run the pipeline step we loaded, with no parameters
  then:
    1 * getPipelineMock("error")("ERROR: config is not defined")
}
```

And that's the gist of it. You can add as many feature methods as necessary in the spec file, testing a variety of things. Be sure to check out the Spock
Documentation, Jenkins-Spock Documentation, and already-created spec files in this repository for examples.