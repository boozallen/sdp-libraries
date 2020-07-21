/*
  returns a string of build arguments that gets appended to 
  the `docker build` command in the build() function.

  There are two types or build arguments currently supported by this library,
  "credential" - will look in Jenkins for credentials of type "Secret Text" that are identified by "id"
  "inline" - value is not a secret and is defined directly in the configuration

  "Build Args" are passed to the library through the pipeline configuration like:

  docker{
    repo_path_prefix = env.PKG_REPO ?: "my-repo-path"
    build_args = [
      GITHUB_TOKEN : [type: "credential", id:"github_token"],
      SOME_VALUE : [type: "inline", value:"some-inline-value-here"]
    ]
  }
  
  get_build_arg_string()
  returns: "--build-arg GITHUB_TOKEN=some-credential-stored-in-jenkins --build-arg SOME_VALUE=some-inline-value-here"
*/
def call(){
  stage "Preparing Build Arguments", {
    def build_args = []
    // Check if build_args was passed into the config
    if (config.build_args) {
      // Ensure that the passed in build_args is a map
      if (!(config.build_args instanceof Map)){
        error "build_args must be a Map, received [${config.build_args.getClass()}]"
      }
      else { // Otherwise
        // Ensure the map has contents
        if (config.build_args.size() > 0){  
          build_args = config.build_args
        }  
      }
    }
    def arg_string = ""
    for (arg in build_args){
      def arg_key = arg.key
      def arg_value = ""
      switch(arg.value['type']) { 
       case 'credential': 
         def jenkinsCredentials = com.cloudbees.plugins.credentials.CredentialsProvider.lookupCredentials(
             com.cloudbees.plugins.credentials.Credentials.class,
             Jenkins.instance,
             null,
             null);
         def found = false;
         for (creds in jenkinsCredentials) {
           if(creds.id == arg.value['id']){
             arg_value = creds.secret
             found = true;
           }
         }
         if (!found) {
          error "The build arg ${arg.key} refers to a credential ${arg.value['id']} not found in Jenkins."
         }
         break;
       case 'inline':
         arg_value = arg.value['value']
         break;
      } 
      new_arg = "--build-arg ${arg_key}=${arg_value} "
      arg_string = arg_string + new_arg
    }
    return arg_string
  }
}
