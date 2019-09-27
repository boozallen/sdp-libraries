/*
  libraries{
    gradle{
      modules
      tasks = []
      taskExcldues = []
      
    }
  }
*/

void call(){
  
  def modules = [:] 
  
  config.build.modules.each{ moduleName, moduleConfig -> 
    modules[moduleName] = { invokeGradle(moduleName.toString(), "build", moduleConfig) }
  }

  if(config.build.order){
    config.build.order.each{ 
      stage("Build: ${it.join(", ")}"){
        parallel modules.subMap(it)
      }
      // join files from parallel build threads 
      node{
        unstash "workspace"
        it.each{ module -> 
          unstash "${module}-build" 
        }
        stash "workspace"
      }
    }
  } else {
      stage("Build"){
        parallel modules 
      }
      // join files from parallel build threads 
      node{
        unstash "workspace"
        modules.each{ moduleName, moduleConfig -> 
          unstash "${moduleName}-build"
        }
        stash "workspace" 
      }
  } 

}