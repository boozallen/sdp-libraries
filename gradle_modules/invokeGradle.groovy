void call(moduleName, String phase, moduleConfig){
  node{
    unstash "workspace"
    docker.image("gradle-jfx").inside{
      if(!fileExists(moduleName)){
        error "Module ${moduleName} does not exist." 
      }

      def block = { 
        String tasks = ""
        if(moduleConfig.tasks){
          tasks = moduleConfig.tasks.join(" ")
        }else if(config.get(phase)?.tasks){
          tasks = config.get(phase)?.tasks.join(" ")
        }

        String tasksExclude = ""
        if(moduleConfig.tasksExclude){
          tasksExclude = moduleConfig?.tasksExclude.collect{ "-x ${it} " }.join(" ")
        }else if(config.get(phase)?.tasksExclude){
          tasksExclude = config.get(phase)?.tasksExclude.collect{ "-x ${it} " }.join(" ")
        }

        try{
          dir(moduleName){
            sh "gradle ${tasks} ${tasksExclude}"
          }
        }catch(any){
          enforce = config[phase].subMap("enforce") ? config.enforce : true 
          if (phase.equals("test")){
            dir(moduleName){
              if(fileExists("sonar-project.properties")){
                String sonarConfig = readFile("sonar-project.properties")
                if (sonarConfig.contains("sonar.tests=")){
                  println "creating test reports directory for sonarqube"
                  sh "mkdir -p ${(sonarConfig.split("\n").find{ it.startsWith("sonar.tests=") } - "sonar.tests=").replaceAll(","," ")}"
                }
              }
            }
          }
          if (enforce){
            error "gradle ${tasks} ${tasksExclude} failed - ${any.getMessage()}"
          } else {
            unstable "gradle ${tasks} ${tasksExclude} failed - ${any.getMessage()}"
          }
        }finally{
          // show test results 
          moduleConfig.test_results.each{ 
            sh script: "touch ${it}", returnStatus: true // junit plugin ignores old files. problematic for large test suites
            junit it 
          }
          config.get(phase)?.test_results.each{ 
            sh script: "touch ${it}", returnStatus: true 
            junit it 
          }

          // archive artifacts
          moduleConfig.artifacts.each{ 
            try{ archiveArtifacts it }catch(any){
              println "error Archiving ${it}"
            }
          }
          config.get(phase)?.artifacts.each{ 
            try{ archiveArtifacts it }catch(any){
              println "error Archiving ${it}"
            }
          }

          stash "${moduleName}-${phase}"
        }
      }

      if(config.get(phase)?.credential){
        withCredentials([
          usernamePassword(credentialsId: config.get(phase)?.credential.id, 
                           passwordVariable: config.get(phase)?.credential.passwordVar, 
                           usernameVariable: config.get(phase)?.credential.usernameVar)
        ], block) 
      } else { 
        block()
      }

    }
  }
}