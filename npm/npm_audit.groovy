void call(){
  node{
    stage('NPM Package Analysis') {
      unstash "workspace"
        // npm_base: path from the root of your repository to the NPM project directory
        def npm_base = config.npm_base ?: ""

        // use_npm_default_registry:  if the audit will use the default npm registry (https://registry.npmjs.org) or the project's registry,
        def use_default_registry = config.use_npm_default_registry ?: false

        def project_base = "${WORKSPACE}/${npm_base}"
        def nodeImage = config.node_image ?: "node:latest"

        echo "Creating file for output."
        def timestamp = new Date().format("yyyy-MM-dd_HH-mm-ss", TimeZone.getTimeZone('UTC'))
        def audit_results_file = "npmAudit_${timestamp}.json"
        sh "touch ${audit_results_file}"

        echo "Running npm audit"

        docker.image(nodeImage).inside{
            //create package-lock if not already present
            if( !(fileExists("${project_base}/package-lock.json"))) {
                sh "npm i --prefix ${project_base} --package-lock-only"
              echo "creating ${project_base}/package-lock.json"
            }
            //run npm audit, using the set registry
            if(use_default_registry) {
                sh ( returnStatus:true, script: "npm audit --registry=https://registry.npmjs.org --json --prefix ${project_base} > ${audit_results_file}" )
            }
            else {
                sh ( returnStatus:true, script: "npm audit --json --prefix ${project_base} > ${audit_results_file}" )
            }
        }

        archiveArtifacts allowEmptyArchive: true, artifacts: "${audit_results_file}"
    }
  }
}