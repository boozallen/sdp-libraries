def call() {
    def ret = []
    errors = []
    ret << (config.registry ?:
            { errors << "Application Docker Image Registry, libraries.docker.registry, not defined in pipeline config" }() )
    ret << ( config.cred ?:
            { errors << "Application Docker Image Registry Credential, libraries.docker.cred, not defined in pipeline config" }() )

    if(!errors.empty) {
        error errors.join("; ")
    }

    return ret
}
