void validate_configuration(){
    println "validating the github_enterprise configuration"
}

/*
    fetches the name of the source branch in a Pull Request. 
*/
def get_source_branch(){
    String ghUrl = "${env.GIT_URL.split("/")[0..-3].join("/")}/api/v3"
    def repo, org
    withCredentials([
        usernamePassword(credentialsId: env.GIT_CREDENTIAL_ID, passwordVariable: 'PAT', usernameVariable: 'USER')
    ]) {
        return GitHub.connectToEnterprise(ghUrl, PAT).getRepository("${env.ORG_NAME}/${env.REPO_NAME}")
            .getPullRequest(env.CHANGE_ID.toInteger())
            .getHead()
            .getRef()
    }
}