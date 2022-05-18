/*
  Copyright © 2022 Booz Allen Hamilton. All Rights Reserved.
  This software package is licensed under the Booz Allen Public License. The license can be found in the License file or at http://boozallen.github.io/licenses/bapl
*/

package libraries.maven

class MavenInvokeSpec extends JTEPipelineSpecification {

    def MavenInvoke = null

    LinkedHashMap minimalUnitTestingConfig = [
        unit_test: [
            stageName: "Maven Unit Tests",
            buildContainer: "maven:3.8.5-openjdk-11",
            phases: ["test"]
        ]
    ]

    static class DummyException extends RuntimeException {
        DummyException(String _message) { super( _message ) }
    }

    def setup() {
        LinkedHashMap config = [:]
        LinkedHashMap stepContext = [ name: "unit_test" ]

        MavenInvoke = loadPipelineScriptForStep("maven", "maven_invoke")

        explicitlyMockPipelineStep("inside_sdp_image")

        MavenInvoke.getBinding().setVariable("config", config)
        MavenInvoke.getBinding().setVariable("stepContext", stepContext)
    }

    def "Completes a mvn test successfully" () {
        setup:
            getPipelineMock("sh")("mvn archetype:generate -DgroupId=com.mycompany.app -DartifactId=my-app -DarchetypeArtifactId=maven-archetype-quickstart -DarchetypeVersion=1.4 -DinteractiveMode=false && cd my-app")
            MavenInvoke.getBinding().setVariable("config", minimalUnitTestingConfig)
        when:
            MavenInvoke()
        then:
            1 * getPipelineMock("sh")("mvn test")
    }

    def "Application environment settings take precendence over library config" () {
        setup:
            MavenInvoke.getBinding().setVariable("config", minimalUnitTestingConfig)
        when:
            MavenInvoke([
                maven: [
                    unit_test: [
                        stageName: "AppEnv Defined Maven Stage",
                        options: ["-v"],
                        phases: []
                    ]
                ]
            ])
        then:
            MavenInvoke.getBinding().variables.stageName == "AppEnv Defined Maven Stage"
            MavenInvoke.getBinding().variables.buildContainer == "maven:3.8.5-openjdk-11"
            MavenInvoke.getBinding().variables.options == ["-v"]
            MavenInvoke.getBinding().variables.phases == []
    }

    def "Artifacts get archived as expected" () {
        setup:
            getPipelineMock("sh")("mvn archetype:generate -DgroupId=com.mycompany.app -DartifactId=my-app -DarchetypeArtifactId=maven-archetype-quickstart -DarchetypeVersion=1.4 -DinteractiveMode=false && cd my-app")
            MavenInvoke.getBinding().setVariable("config", [
                build: [
                    stageName: "Maven Build",
                    buildContainer: "maven:3.8.5-openjdk-11",
                    phases: ["clean", "install"],
                    artifacts: ["target/*.jar"]
                ]
            ])
        when:
            MavenInvoke()
        then:
            1 * getPipelineMock("archiveArtifacts.call")(_ as Map)
    }
}