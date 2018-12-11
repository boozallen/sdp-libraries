/*
  Copyright © 2018 Booz Allen Hamilton. All Rights Reserved.
  This software package is licensed under the Booz Allen Public License. The license can be found in the License file or at http://boozallen.github.io/licenses/bapl
*/

void call(Map args = [:], body){

  // do nothing if not merge
  if (!env.GIT_BUILD_CAUSE.equals("merge"))
    return

  env.FEATURE_SHA = get_feature_branch_sha()

  def source_branch = get_merged_from()
  def target_branch = env.BRANCH_NAME

  // do nothing if source branch doesn't match
  if (args.from)
  if (!source_branch.collect{ it ==~ args.from}.contains(true))
    return

  // do nothing if target branch doesnt match
  if (args.to)
  if (!(target_branch ==~ args.to))
    return

  def mergedFrom = source_branch.join(", ")
  // grammar essentially, remove oxford comma to follow git syntax
  if(mergedFrom.contains(", ")) {
      def oxford = mergedFrom.lastIndexOf(", ")
      mergedFrom = mergedFrom.substring(0, oxford) + " and" + mergedFrom.substring(oxford + 1)
  }

  println "running because of a merge from ${mergedFrom} to ${target_branch}"
  body()
}

String get_merged_from(){
  node{
    unstash "git-info"
    // update remote for git name-rev to properly work
    def remote = sh(
        script: "git remote -v",
        returnStdout: true
    ).split()[1]
    withCredentials([usernamePassword(credentialsId: 'github', passwordVariable: 'PASS', usernameVariable: 'USER')]){
        remote = remote.replaceFirst("://", "://${USER}:${PASS}@")
        sh "git remote rm origin"
        sh "git remote add origin ${remote}"
        sh "git fetch --all > /dev/null 2>&1"
    }
    // list all shas, but trim the first two shas
    // the first sha is the current commit
    // the second sha is the current commit's parent
    def sourceShas = sh(
      script: "git rev-list HEAD --parents -1",
      returnStdout: true
    ).trim().split(" ")[2..-1]
    def branchNames = []
    // loop through all shas and attempt to turn them into branch names
    for(sha in sourceShas) {
      def branch = sh(
        script: "git name-rev --name-only " + sha,
        returnStdout: true
      ).replaceFirst("remotes/origin/", "").trim()
      // trim the ~<number> off branch names which means commits back
      // e.g. master~4 means 4 commits ago on master
      if(branch.contains("~"))
        branch = branch.substring(0, branch.lastIndexOf("~"))
      if(!branch.contains("^"))
        branchNames.add(branch)
    }
    return branchNames
  }
}

String get_feature_branch_sha(){
  node{
    unstash "git-info"
    sh(
      script: "git rev-parse \$(git --no-pager log -n1 | grep Merge: | awk '{print \$3}')",
      returnStdout: true
     ).trim()
  }
}