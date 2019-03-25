
@BeforeStep
void call(Map context){
    if (context.step.equals("static_code_analysis")){
        echo "build_source before static_code_analysis"
        build_source()
    }
}