# FAQ

This section covers some of the questions not easily answered in the Spock or Jenkins-Spock documentation.

**Q: What's the difference between explicitlyMockPipelineStep and explicitlyMockPipelineVariable?**

**A: Practically speaking, the difference is you can omit ".call" for explicitlyMockPipelineStep() when you use getPipelineMock()**

In the example above, I used ``explicitlyMockPipelineStep()`` to mock `error`. Because of that, if I want to see if the `error` pipeline step is run, I use
`1 * getPipelineMock("error")`. If I were to create the mock using `explicitlyMockPipelineVariable()` I would instead use `1 * getPipelineMock("error.call")`

There may be some additional differences as well, so try to use what makes the most sense

**Q: What if I don't know exactly what the parameters are going to be?**

**A: There are ways to match parameters to regex expressions, as well as test parameters individually**

The standard format for interaction-based tests are

```groovy
<count> * getPipelineMock(<method>)(<parameter(s)>)
```

While you can put the exact parameter value in the second parentheses, you can also run arbitrary groovy code inside curly brackets. Whether or not it's a "match" depends on if that code returns `true` or `false`. A good example is in PenetrationTestSpec.groovy. Use `it` to get the value of the parameter. ``1 * getPipelineMock("sh")({it =~ / (zap-cli open-url) Kirk (.+)/})``

**Q: Do I have to do interaction-based testing?**

**A: No, but you can't get variables the same way as traditional Spock tests**

This is because the script gets run in that `loadPipelineScriptForTest` object. You can only access variables stored in the binding, which are few. It makes more
sense to see how variables are being used in pipeline steps, and make sure those pipeline steps use the correct value for those variables.

Similarly, if you need to control how a variable is set, you need to stub whatever method or pipeline step that sets the initial value for that variable

As an example, in PenetrationTestSpec.groovy, the `target` variable in penetration_test.groovy is tested by checking the parameters to an `sh` step.

**Q: I keep getting "can't run method foo() on null; what do I do?"**

**A: You need to find a way to stub the method that sets the value for the object that calls foo()**

There should be an example in GetImagesToBuildSpec.groovy (in the docker-unit-tests branch at the time of writing)