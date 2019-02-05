void call(){
  // get directory of where the collections are stored
  collections_directory = config.collections_directory ?:
            "collections"
  
  stage "API Testing", {
    inside_sdp_image "newman", {
        try{
            unstash "workspace"
            def files = findFiles(glob: "${collections_directory}/*.json")
            files.each{
                echo "Running ${it.name} collection"
                sh "newman run ${it.path}"
            }
        }
        catch(ex){
            error "Newman tests failed with: ${ex}"
        }
    }
  }
}
