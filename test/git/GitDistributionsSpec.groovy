package libraries.git

public class GitDistributionsSpec extends JTEPipelineSpecification {

  def GitDistributions = null

  public static class DummyException extends RuntimeException {
    public DummyException(String _message) { super( _message ); }
  }

  def out
  def env = [GIT_URL: "giturl", ORG_NAME: "orgname", REPO_NAME: "reponame", GIT_SHA: "gitsha", CHANGE_TARGET: false, GIT_BUILD_CAUSE: "gitbuildcause"]
  def config = [github:[:]]
  def setup() {
    env = [GIT_URL: "giturl", ORG_NAME: "orgname", REPO_NAME: "reponame", GIT_SHA: "gitsha", CHANGE_TARGET: false, GIT_BUILD_CAUSE: "gitbuildcause"]

    out = Mock(java.io.PrintStream)
    out.println(_) >> {return 1}
    GitDistributions = loadPipelineScriptForStep("git", "git_distributions")
   
    // should be set for each implementation github, 
    GitDistributions.getBinding().setVariable("env", env)
    GitDistributions.getBinding().setProperty("out", out)
    GitDistributions.getBinding().setProperty("config", config)

    def GitHub = loadPipelineScriptForTest("git/steps/github.groovy")
    GitDistributions.getBinding().metaClass.getStep = { String s -> 
    if(s == 'github'){ return GitHub}
    else { return null }
    }
    
    GitHub.getBinding().setVariable("env", env)
    GitHub.getBinding().setProperty("out", out)

    explicitlyMockPipelineVariable("ScmUserRemoteConfig")
    explicitlyMockPipelineVariable("scm")
    getPipelineMock("scm.getUserRemoteConfigs")() >> { [getPipelineMock("ScmUserRemoteConfig")] }
    getPipelineMock("ScmUserRemoteConfig.getUrl")() >> "https://github.com/example-org/example-repo.git"
    getPipelineMock("sh")(_ as Map) >> " 1 "

  }
  def "print map for config" () {
    when:
      GitDistributions()
    then:
      1 * out.println("github config is ${config.github}".toString())
  }

  def "empty config generates message" () {
    setup:
      GitDistributions.getBinding().setProperty("config", [:])
    when:
      GitDistributions()
    then:
      1 * getPipelineMock("error")("you must configure one distribution option, currently: []") >> { throw new RuntimeException("empty config") }
      thrown(RuntimeException)
  }

  def "too many distributions config generates message" () {
    setup:
      GitDistributions.getBinding().setProperty("config", [github:[:], gitlab:[:]])
    when:
      GitDistributions()
    then:
      1 * getPipelineMock("error")({ it =~ /you must configure one distribution option, currently: /}) >> { throw new RuntimeException("empty config") }
      thrown(RuntimeException)
  }

  def "unstash workspace before calling git commands" () {
    when:
      GitDistributions()
    then:
      1 * getPipelineMock("unstash")("workspace")
    then:
      0 * out.println(" 'workspace' stash not present. Skipping git library environment variable initialization. To change this behavior, ensure the 'sdp' library is loaded")
      _ * getPipelineMock("sh")({ try {it.script =~ /git/; } catch(any) {it =~ /git/} }) >> "1"
      //returning the arbitrary string "1" to prevent the script from failing
  }

  def "unstash workspace with exception prints message" () {
    when:
      GitDistributions()
    then:
      1 * getPipelineMock("unstash")("workspace") >> { throw new RuntimeException("invalid action")}
    then:
      1 * out.println("'workspace' stash not present. Skipping git library environment variable initialization. To change this behavior, ensure the 'sdp' library is loaded")
  }

  def "GIT_URL env var is retrieved from the scm object" () {
    when:
      GitDistributions()
    then:
      2 * getPipelineMock("scm.getUserRemoteConfigs")() >> { [getPipelineMock("ScmUserRemoteConfig")] }
      1 * getPipelineMock("ScmUserRemoteConfig.getUrl")() >> "https://github.com/boozallen/jenkins-templating-engine.git"
      GitDistributions.getBinding().variables.env.GIT_URL == "https://github.com/boozallen/jenkins-templating-engine.git"
  }

  def "GIT_CREDENTIAL_ID env var is retreived from the scm object" () {
    when:
    GitDistributions()
    then:
    2 * getPipelineMock("scm.getUserRemoteConfigs")() >> { [getPipelineMock("ScmUserRemoteConfig")] }
    1 * getPipelineMock("ScmUserRemoteConfig.getProperty").call('credentialsId') >> "credential"
    GitDistributions.getBinding().variables.env.GIT_CREDENTIAL_ID == "credential"
  }

  def "ORG_NAME is set to the GitHub organization in GIT_URL" () {
    when:
      GitDistributions()
    then:
      getPipelineMock("ScmUserRemoteConfig.getUrl")() >> "https://github.com/library-test/jenkins-templating-engine.git"
      GitDistributions.getBinding().variables.env.ORG_NAME == "library-test"
  }

  def "REPO_NAME is set to the GitHub repository in GIT_URL" () {
    when:
      GitDistributions()
    then:
      getPipelineMock("ScmUserRemoteConfig.getUrl")() >> "https://github.com/boozallen/library-test2.git"
      GitDistributions.getBinding().variables.env.REPO_NAME == "library-test2"
  }

  def "GIT_SHA is set to HEAD" () {
    when:
      GitDistributions()
    then:
      1 * getPipelineMock("sh")([script: "git rev-parse HEAD", returnStdout: true]) >> script_output
      GitDistributions.getBinding().variables.env.GIT_SHA == git_sha
    where:
      script_output   | git_sha
      "abcd12345"     | "abcd12345"
      "\nefgh67890\n" | "efgh67890"
  }

  def "If CHANGE_TARGET is truthy set GIT_BUILD_CAUSE to pr" () {
    setup:
      GitDistributions.getBinding().variables.env.put("CHANGE_TARGET", change_target)
    when:
      GitDistributions()
    then:
      GitDistributions.getBinding().variables.env.GIT_BUILD_CAUSE == build_cause
    where:
      change_target | build_cause
      true          | "pr"
      "true"        | "pr"
      false         | "commit"
      null          | "commit"
  }

  def "If the latest commit has more than 2 parents, set GIT_BUILD_CAUSE to merge, else set to commit" () {
    when:
      GitDistributions()
    then:
      1 * getPipelineMock("sh")([script: 'git rev-list HEAD --parents -1 | wc -w', returnStdout: true]) >> script_output
      GitDistributions.getBinding().variables.env.GIT_BUILD_CAUSE == build_cause
    where:
      script_output | build_cause
      "1"           | "commit" // I don't anticipate script_output being less than 2
      "2"           | "commit"
      "3"           | "merge"
      "4"           | "merge"
      "  1    \n"   | "commit"
      "\n5\n"       | "merge"
  }


  def "The Git Build Cause is printed to the log" () {
    when:
      GitDistributions()
    then:
      1 * out.println({it =~ /Found Git Build Cause/})
  }

}
