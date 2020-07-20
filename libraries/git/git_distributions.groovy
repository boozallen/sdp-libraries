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
    def dist = getBinding().getStep(distribution)
    dist.validate_configurations()
}
