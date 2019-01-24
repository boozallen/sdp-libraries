def call() {
    def img_repo = config.registry ?:
            { error "Application Docker Image Registry not defined in pipeline config" }()
    def img_repo_cred = config.cred ?:
            { error "Application Docker Image Repository Credential not defined in pipeline config" }()

    return [img_repo, img_repo_cred]
}