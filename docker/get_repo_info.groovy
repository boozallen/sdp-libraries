def call() {
    def ret = [:]
    def errors = []
    ret.repo = config.registry ?:
            { errors << "Application Docker Image Registry, libraries.docker.registry, not defined in pipeline config" }()
    ret.cred = config.cred ?:
            { errors << "Application Docker Image Repository Credential, libraries.docker.cred, not defined in pipeline config" }()

    if(!errors.empty) {
        error errors.join("; ")
    }

    return ret
}