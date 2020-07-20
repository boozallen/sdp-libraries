void validate_configuration(){
    println "validating the gitlab configuraiton"
}

/*
    returns the name of the source branch in a Merge Request

    for example, in a MR from feature to development, the source branch 
    would be "feature"
*/
def get_source_branch(){
  node{
    def gitlabUrl = "${env.GIT_URL.split("/")[0..-3].join("/")}"
    withCredentials([
        usernamePassword(
            credentialsId: env.GIT_CREDENTIAL_ID, 
            passwordVariable: 'PAT', 
            usernameVariable: 'USER'
        )
    ]) {
        if ((env.ORG_NAME).isEmpty()){
            projectName = "${env.REPO_NAME}"
        } else {
            projectName = "${env.ORG_NAME}/${env.REPO_NAME}"
        }
        GitLabApi gl = new GitLabApi(gitlabUrl, PAT)
        sourceBranch =  gl.getMergeRequestApi().getMergeRequest(projectName.toString(), env.CHANGE_ID.toInteger()).getSourceBranch() 
        return sourceBranch
    }
  }
}
