import org.boozallen.plugins.jte.console.TemplateLogger
import org.boozallen.plugins.jte.binding.injectors.ApplicationEnvironment
import java.lang.reflect.Field
import com.cloudbees.groovy.cps.NonCPS

/*
    three places to get data from in structure:
    1: global env vars for the account
    /infrastructure/global/* 
    
    2. app env specific infrastructure
    /infrastructure/<env name>/*
    
    3. application data env specific
    /apps/<app name>/<env name>/*
*/

@Init
void call(context){

    def identifier = config.identifier 
    def region = env.AWS_REGION
    def projectName = config.projectName ?: env.PROJECT_NAME

    inside_sdp_image("aws"){
        assumeRole()

        pipelineConfig.application_environments.each{ envName, _ -> 
            def app_env = getBinding().getVariable(envName)
            def appEnvConfig = this.getParameters("/${projectName}/apps/${identifier}/${app_env.short_name}", region)
            def infraEnvConfig = this.getParameters("/${projectName}/infrastructure/${app_env.short_name}", region)
            def resultantConfig = infraEnvConfig + appEnvConfig + app_env.config
            setAppEnvConfig(app_env, resultantConfig)
        }

        this.getParameters("/${projectName}/infrastructure/globals", region).each{ key, value -> 
            TemplateLogger.print "setting env: ${key}=${value}"
            env[key] = value 
        }

    }

}

def keepParam(param, region){
    def response = sh(script: "aws ssm list-tags-for-resource --resource-type Parameter --resource-id ${param.Name} --region ${region}", returnStdout: true).trim()
    def tagList = readJSON(text: response).TagList
    return tagList.find{ it.Key.equals("sdp") }?.Value 
}

def getParameters(path, region){
    def additionalConfig = [:]
    def response = sh(script: "aws ssm get-parameters-by-path --recursive --path ${path} --region ${region}", returnStdout: true).trim()
    def params = readJSON(text: response).Parameters
    
    params.findAll{ param ->
        this.keepParam(param, region)
    }.each{ param -> 
        additionalConfig[param.Name.split("/").last()] = param.Value 
    }
    return additionalConfig
}
