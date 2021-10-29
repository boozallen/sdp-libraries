/*
  Copyright © 2018 Booz Allen Hamilton. All Rights Reserved.
  This software package is licensed under the Booz Allen Public License. The license can be found in the License file or at http://boozallen.github.io/licenses/bapl
*/

package libraries.git.steps

void call(Map args = [:], body){

  // do nothing if not pr
  if (!(env.GIT_BUILD_CAUSE in ["pr"]))
    return

  def source_branch = git_distributions.fetch().get_source_branch()
  def target_branch = env.CHANGE_TARGET

  // do nothing in source branch doesn't match
  if (args.from)
  if (!(source_branch ==~ (~args.from) ))// convert string to regex
    return

  // do nothing if target branch doesnt match
  if (args.to)
  if (!(target_branch ==~ (~args.to) ))// convert string to regex
    return

  println "running because of a PR from ${source_branch} to ${target_branch}"
  body()

}
