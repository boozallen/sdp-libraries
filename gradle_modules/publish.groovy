void call(){
  
  def modules = [:] 
  
  config.publish.modules.each{ moduleName, moduleConfig -> 
    modules[moduleName] = { invokeGradle(moduleName.toString(), "publish", moduleConfig) }
  }

  if(config.publish.order){
    config.publish.order.each{ 
      stage("Publish: ${it.join(", ")}"){
        parallel modules.subMap(it)
      }
      // join files from parallel publish threads 
      node{
        unstash "workspace"
        it.each{ module -> 
          unstash "${module}-publish" 
        }
        stash "workspace"
      }
    }
  } else {
      stage("Publish"){
        parallel modules 
      }
      // join files from parallel publish threads 
      node{
        unstash "workspace"
        modules.each{ moduleName, moduleConfig -> 
          unstash "${moduleName}-publish"
        }
        stash "workspace" 
      }
  } 

}