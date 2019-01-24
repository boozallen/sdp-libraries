def call() {
    def ret = []
    with_errors {
    ret << (config.registry ?:
            { errors << "Application Docker Image Registry, libraries.docker.registry, not defined in pipeline config" }() )
    ret << ( config.cred ?:
            { errors << "Application Docker Image Repository Credential, libraries.docker.cred, not defined in pipeline config" }() )
    }

    return ret
}