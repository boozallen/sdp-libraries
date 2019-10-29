import org.boozallen.plugins.jte.binding.injectors.ApplicationEnvironment
import java.lang.reflect.Field

void call(ApplicationEnvironment a, Map c){
    Field configF = ApplicationEnvironment.class.getDeclaredField("config")
    configF.setAccessible(true)
    println "setting ${a.short_name} config: ${c}"
    configF.set(a, c)
}
