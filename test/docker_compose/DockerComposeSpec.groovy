package libraries.docker_compose

import JTEPipelineSpecification

class DockerComposeSpec extends JTEPipelineSpecification {
    def dockerCompose = null
    def DOCKER_COMPOSE_COMMAND = "docker-compose "

    def setup() {
        dockerCompose = loadPipelineScriptForTest("docker_compose/compose.groovy")
        explicitlyMockPipelineStep("sh")
        explicitlyMockPipelineStep("sleep")
        dockerCompose.getBinding().setVariable("config", [:])
    }

    def "Run up with default values" () {
        when:
            dockerCompose.up()
        then:
            1 * getPipelineMock("stage")("Deploy", _)
        then:
            1 * getPipelineMock("sh")(DOCKER_COMPOSE_COMMAND + "up -d")
    }

    def "Run down with default values" () {
        when:
            dockerCompose.down()
        then:
            1 * getPipelineMock("stage")("Teardown", _)
        then:
            1 * getPipelineMock("sh")(DOCKER_COMPOSE_COMMAND + "down")
    }

    def "Run up with configurations" () {
        Map sleepConfig = [time: 1, unit: "MINUTES"]
        dockerCompose.getBinding().setVariable("config",
                [files: ["docker-compose.it.yml", "docker-compose.ci.yml"], env: ".env.ci", sleep: sleepConfig])
        when:
            dockerCompose.up()
        then:
            1 * getPipelineMock("stage")("Deploy", _)
        then:
            1 * getPipelineMock("sh")(DOCKER_COMPOSE_COMMAND + "-f docker-compose.it.yml -f docker-compose.ci.yml " +
                    "--env-file .env.ci up -d")
        then:
            1 * getPipelineMock("sleep")(sleepConfig)
    }

    def "Run down with configurations" () {
        Map sleepConfig = [time: 1, unit: "MINUTES"]
        dockerCompose.getBinding().setVariable("config",
                [files: ["docker-compose.it.yml", "docker-compose.ci.yml"], env: ".env.ci", sleep: sleepConfig])
        when:
            dockerCompose.down()
        then:
            1 * getPipelineMock("stage")("Teardown", _)
        then:
            1 * getPipelineMock("sh")(DOCKER_COMPOSE_COMMAND + "-f docker-compose.it.yml -f docker-compose.ci.yml " +
                "--env-file .env.ci down")
        then:
            0 * getPipelineMock("sleep")(sleepConfig)
    }
}
