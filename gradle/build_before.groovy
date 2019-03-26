
@BeforeStep
void call(Map context){
    if (context.step.equals("static_code_analysis")){
        println "build_source before static_code_analysis"
        build_source2()
    } else {
        println "not static_code_analysis"
    }
}