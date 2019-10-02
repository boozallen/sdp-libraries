void call(){
  stage("build"){
    def nodeImage = config.node_image ?: "node:latest"
    def buildScript = config.build_script ?: "build"
    def installScript = config.build_install ?: "install"

    if(fileExists("package.json")){
      def pkg = readJSON("package.json")
      if(!pkg.scripts[buildScript]){
        error "cannot execute 'npm run ${buildScript}'.  '${buildScript}' not found in scripts block of package.json file"
      }
    }else{
      error "no package.json file found"
    }

    docker.image(nodeImage).inside{
      unstash "workspace"
      sh "${installScript ? 'npm ' + installScript + ' && ' : ''}npm run ${buildScript}"
      stash name: "workspace",
        includes: config.stash?.includes ?: "**",
        excludes: config.stash?.excludes ?: "node_modules/**",
        useDefaultExcludes: false,
        allowEmpty: true
    }
  }
}
