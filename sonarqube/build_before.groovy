
@BeforeStep
void call(Map context){
    if (context.step.equals("static_code_analysis")){
        if( config.require_build_source || getBinding().hasVariable("build_source")) {
            if( config.require_build_source || getBinding().getVariable("build_source") instanceof org.boozallen.plugins.jte.binding.StepWrapper) {
                println "build_source before static_code_analysis"
                build_source()
            }
        }
    }
}