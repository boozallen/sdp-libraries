void call(){
  
  def modules = [:] 
  
  config.test.modules.each{ moduleName, moduleConfig -> 
    modules[moduleName] = { invokeGradle(moduleName.toString(), "test", moduleConfig) }
  }

  if(config.test.order){
    config.test.order.each{ 
      stage("Unit Test: ${it.join(", ")}"){
        parallel modules.subMap(it)
      }
      // join files from parallel build threads 
      node{
        unstash "workspace"
        it.each{ module -> 
          unstash "${module}-test" 
        }
        stash "workspace"
      }
    }
  } else {
      stage("Unit Test"){
        parallel modules 
      }
      // join files from parallel build threads 
      node{
        unstash "workspace"
        modules.each{ moduleName, moduleConfig -> 
          unstash "${moduleName}-test"
        }
        stash "workspace" 
      }
  } 

}