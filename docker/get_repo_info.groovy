def call() {
    def ret = [:]
    errors = []
    ret.repo = config.registry ?:
            { errors << "Application Docker Image Registry not defined in pipeline config" }()
    ret.cred = config.cred ?:
            { errors << "Application Docker Image Repository Credential not defined in pipeline config" }()

    if(!errors.empty) {
        error errors.join("; ")
    }

    return ret
}