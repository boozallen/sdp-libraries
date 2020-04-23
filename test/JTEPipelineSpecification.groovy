import com.homeaway.devtools.jenkins.testing.JenkinsPipelineSpecification
import com.homeaway.devtools.jenkins.testing.InvalidlyNamedScriptWrapper
import javax.lang.model.SourceVersion
import org.codehaus.groovy.control.CompilerConfiguration
import org.codehaus.groovy.control.customizers.ImportCustomizer

public class JTEPipelineSpecification extends JenkinsPipelineSpecification {
    @Override
    Script loadPipelineScriptForTest(String _path) {
		
		String[] path_parts = _path.split( "/" )
		
		String filename = path_parts[path_parts.length-1]
		
		String resource_path = "/"
		if( path_parts.length >= 2 ) {
			resource_path = String.join( "/", path_parts[0..path_parts.length-2] )
			resource_path = "/${resource_path}/"
		}

		GroovyScriptEngine script_engine = new GroovyScriptEngine(generateScriptClasspath(resource_path))
        CompilerConfiguration cc = script_engine.getConfig() 
        // define auto importing of JTE hook annotations
        ImportCustomizer ic = new ImportCustomizer()
        ic.addStarImports("org.boozallen.plugins.jte.hooks")
        cc.addCompilationCustomizers(ic) 
        script_engine.setConfig(cc)

		Class<Script> script_class = script_engine.loadScriptByName( "${filename}" )

		Script script = script_class.newInstance()

		addPipelineMocksToObjects( script )
		
		if( SourceVersion.isName( script_class.getSimpleName() ) ) {
			return script
		} else {
			return new InvalidlyNamedScriptWrapper( script )
		}
	}
}