import org.boozallen.plugins.jte.hooks.BeforeStep

@BeforeStep
void call(Map context){
    if (context.step.equals("static_code_analysis")){
      String buildStep = config.build_step
      boolean hasStep = getBinding().hasStep(buildStep)
      if( !hasStep && config.require_build_step ){
         throw new Exception("require_build_step with no defined step named: ${buildStep}")
      }
      else if( hasStep ) {
            def step = getBinding().getStep(buildStep)
            String stepMethod = config.build_step_method ?: "call"
            step."${stepMethod}"()
      }
    }
}