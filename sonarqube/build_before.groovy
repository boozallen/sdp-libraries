
@BeforeStep
void call(Map context){
    if (context.step.equals("static_code_analysis")){
        if( config.require_build_step || (config.build_step && getBinding().hasVariable(config.build_step)) ) {
            if( config.require_build_step || getBinding().getVariable(config.build_step) instanceof org.boozallen.plugins.jte.binding.StepWrapper) {
                println "executing before static_code_analysis: step: ${config.build_step ?: 'not defined'}"
                "${config.build_step}"()
            }
        }
    }
}