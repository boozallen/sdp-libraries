void call(){
  stage("build"){
    def nodeImage = config.nodeImage ?: "node:10.16.0-stretch-slim"
    docker.image(nodeImage).inside{
      unstash "workspace"
      sh "npm install && npm run build"
      stash name: "workspace",
        includes: config.stash?.includes ?: "**",
        excludes: config.stash?.excludes ?: "node_modules/**",
        useDefaultExcludes: false,
        allowEmpty: true
    }
  }
}
