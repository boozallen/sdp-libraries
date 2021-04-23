= Docker Compose

This library allows you to perform docker compose commands.

== Steps Provided

.Steps
|===
| Step | Description

| ``up()``
| Runs docker-compose up with values taken from the configuration.
| ``down()``
| Runs docker-compose down with values taken from the configuration.

|===

== Example Usage
[source,groovy]
----
compose.up()
compose.down()
----


== Library Configurations

The library configurations for docker_compose are as follows

.Steps
|===
| Parameter | Description

| ``files``
| Optional list of ordered docker compose files to run. Omitting this parameter causes the command
 ``docker-compose up`` to run on a file named "docker-compose.yml"
| ``env``
| Optional environment file to pass to the docker-compose command.
|``sleep``
| Optional configuration that controls how long to wait after running the up() command before continuing the
pipeline execution. This is helpful when the docker containers need to be started before other steps may run
like integration tests.

|===

== Example Library Configuration
[source,groovy]
----
libraries{
  docker_compose {
    files = ["docker-compose.it.yml"]
    env = ".env.ci"
    sleep {
     time: 1
     unit: "MINUTES"
    }
  }
}
----

== Library Dependencies
* Docker and docker-compose installed on Jenkins.
