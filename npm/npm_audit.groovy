void call(){
  node{
    stage('NPM Package Analysis') {
      unstash "workspace"
      try {
        // npm_base: path from the root of your repository to the NPM project directory
        def npm_base = (config.npm_base instanceof String) ? config.npm_base : ""

        // use_npm_default_registry:  if the audit will use the default npm registry (https://registry.npmjs.org) or the project's registry,
        def use_default_registry = (config.use_npm_default_registry instanceof Boolean) ? config.use_npm_default_registry : false

        def project_base = "${WORKSPACE}/${npm_base}"

        echo "Creating file to output to."
        def timestamp = new Date().format("yyyy-MM-dd_HH-mm-ss", TimeZone.getTimeZone('UTC'))
        def audit_results_file = "npmAudit_${timestamp}.json"
        sh "touch ${audit_results_file}"

        echo "Running npm audit"
        def nodeImage = config.nodeImage ?: "node:10.16.0-stretch-slim" //node:latest
        docker.image(nodeImage).inside(){
            //create package-lock if not already present
            if( !(fileExists("${project_base}/package-lock.json"))) {
                sh "npm i --prefix ${project_base} --package-lock-only"
            }
            //run npm audit, using the set registry
            if(use_default_registry) {
                sh ( returnStatus:true, script: "npm audit --registry=https://registry.npmjs.org --json --prefix ${project_base} > ${audit_results_file}" )
            }
            else {
                sh ( returnStatus:true, script: "npm audit --json --prefix ${project_base} > ${audit_results_file}" )
            }
        }

        archiveArtifacts artifacts: "${audit_results_file}"
      }catch(any){
        println "issue with npm audit"
        unstable("issue with npm audit")
      }

    }
  }
}