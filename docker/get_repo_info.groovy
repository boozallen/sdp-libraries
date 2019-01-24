def call() {
    def ret = [:]
    ret.repo = config.registry ?:
            { error "Application Docker Image Registry not defined in pipeline config" }()
    ret.cred = config.cred ?:
            { error "Application Docker Image Repository Credential not defined in pipeline config" }()

    return ret
}