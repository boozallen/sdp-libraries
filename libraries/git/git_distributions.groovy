/*
  validate library configuration 
*/
@Validate
void call(context){
    List options = [ "github", "github_enterprise", "gitlab" ]
    Map distributionConfig = config.subMap(options)

    // ensure only one distribution is configured
    List configured_distributions = distributionConfig.keySet().toList()
    if(configured_distributions.size() > 1){
        error "You can only specify one distrubtion among ${options}, currently: ${configured_distributions}"
    }

    String distribution = distributionConfig.keySet().first() 
    env.GIT_LIBRARY_DISTRUBITION = distribution
    def dist = this.fetch()
    dist.validate_configuration()
}

def fetch(){
    return getBinding().getStep(env.GIT_LIBRARY_DISTRUBITION)
}
