package libraries.docker_compose.steps

void up() {
    stage("Deploy: Docker Compose") {
        String command = "docker-compose "
        command = addFiles(command)
        command = addEnvFile(command)
        command += "up -d"

        sh command

        if (config.sleep) {
            sleep time: config.sleep.time, unit: config.sleep.unit
        }
    }
}

void down() {
    stage("Teardown: Docker Compose") {
        String command = "docker-compose "
        command = addFiles(command)
        command = addEnvFile(command)
        command += "down"

        sh command
    }
}

String addEnvFile(String command) {
    if (config.env) {
        command += "--env-file ${config.env} "
    }
    return command
}

String addFiles(String command) {
    if (config.files) {
        config.files.each { file -> command += "-f ${file} " }
    }
    return command
}