package libraries.maven

import JTEPipelineSpecification


class MavenTestSpec extends JTEPipelineSpecification {

    def mvn = null
    def PHASES = ["clean", "install"]
    def INSTALL_COMMAND = "mvn clean install "

    static class DummyException extends RuntimeException {
        DummyException(String _message) { super( _message ) }
    }

    def setup() {
        mvn = loadPipelineScriptForTest("maven/maven.groovy")
        explicitlyMockPipelineStep("withMaven")
        explicitlyMockPipelineStep("sh")
        mvn.getBinding().setVariable("config", [mavenId: "installedMaven"])
    }

    def "Fail when no mavenId is supplied" () {
        mvn.getBinding().setVariable("config", [:])
        when:
            try{
                mvn.run(PHASES)
            } catch (DummyException e) {}
        then:
            1 * getPipelineMock("error")("Must supply the installed Maven version's ID") >> {throw new DummyException("No mavenId supplied")}
        then:
            0 * getPipelineMock("sh")(INSTALL_COMMAND)
    }

    def "Run with phase only" () {
        when:
            mvn.run(PHASES)
        then:
            1 * getPipelineMock("stage")("Maven", _)
        then:
            1 * getPipelineMock("withMaven")([maven:"installedMaven"], _)
        then:
            1 * getPipelineMock("sh")(INSTALL_COMMAND)
    }

    def "Fail to run when no phase is supplied" () {
        when:
            try {
                mvn.run(null)
            } catch (DummyException e) {}
        then:
            1 * getPipelineMock("stage")("Maven", _)
        then:
            1 * getPipelineMock("withMaven")([maven:"installedMaven"], _)
        then:
            1 * getPipelineMock("error")("Must supply phase for Maven") >> {throw new DummyException("No phase supplied")}
    }

    def "Using multiple goals, properties, and profiles" () {
        ArrayList<String> goals = ["dependency:copy-dependencies", "resources:resources"]
        Map<String, String> properties = ["null.value.property": null, "another.property": "X"]
        ArrayList<String> profiles = ["integration-test","ci"]

        String expected = INSTALL_COMMAND + goals.join(" ") + " -Dnull.value.property -Danother.property = X " + "-Pintegration-test,ci"
        when:
            mvn.run([goals: goals, properties: properties, profiles: profiles], PHASES)
        then:
            1 * getPipelineMock("stage")("Maven", _)
        then:
            1 * getPipelineMock("withMaven")([maven:"installedMaven"], _)
        then:
            1 * getPipelineMock("sh")(expected)

    }
}
