
@BeforeStep
void call(Map context){
    if (context.step.equals("static_code_analysis")){
      boolean hasStep = getBinding().hasStep(config.build_step)
      if( config.require_build_step && !hasStep ){
         throw new Exception("require_build_step with no defined step named: ${config.build_step}")
      }
      else if( hasStep ) {
            def step = getBinding().getStep(config.build_step)
            String stepMethod = config.build_step_method ?: "call"
            step."${stepMethod}"()
        }
    }
}