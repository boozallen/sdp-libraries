
@BeforeStep
void call(Map context){
    if (context.step.equals("static_code_analysis")){
        if( config.require_build_step || getBinding().hasStep(config.build_step) ) {
            def step = getBinding().getStep(config.build_step)
            String stepMethod = config.build_step_method ?: "call"
            step."${stepMethod}"()
        }
    }
}