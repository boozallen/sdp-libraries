def call() {
    def img_repo = config.images.registry ?:
            { error "Application Docker Image Registry not defined in pipeline config" }()
    def img_repo_cred = config.images.cred ?:
            { error "Application Docker Image Repository Credential not defined in pipeline config" }()

    return new Tuple(img_repo, img_repo_cred)
}