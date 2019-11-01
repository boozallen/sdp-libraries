void call(){
  stage("build"){
    def image = config.image ?: "maven:3.6-jdk-8"
    def install_script = config.install_script ?: "install"
    def ls_output = config.ls_output ?: false
    def build_cmd = config.build_cmd ?: "clean package -Dmaven.test.skip=true"
    inside_sdp_image(image){
      unstash "workspace"
      sh "mvn ${build_cmd}"

      if( ls_output ){
        sh "ls ."
      }

      stash name: config.stash?.name ?: "workspace",
        includes: config.stash?.includes ?: "**",
        excludes: config.stash?.excludes ?: "**/*Test",
        useDefaultExcludes: false,
        allowEmpty: true
    }
  }
}
