def call() {
    def ret = [:]
    def errors = []
    ret.repo = config.registry ?:
            { errors << "Application Docker Image Registry, library.docker.registry, not defined in pipeline config" }()
    ret.cred = config.cred ?:
            { errors << "Application Docker Image Repository Credential, library.docker.cred, not defined in pipeline config" }()

    if(!errors.empty) {
        error errors.join("; ")
    }

    return ret
}