
def call(){
    List options = ['github_standard', 'github_enterprise']
    String impl = config.source_type

    return options.contains(impl) ? getBinding().getStep(impl) :
            { error "github.config.source_type: ${impl} is not a valid option" } ()
}
