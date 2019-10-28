import com.homeaway.devtools.jenkins.testing.JenkinsPipelineSpecification

public class GithubEnterpriseConstructorSpec extends JenkinsPipelineSpecification {

  def GithubEnterpriseConstructor = null
  def context = [:] // Required for methods w/ the @Init annotation, but not used in the step

  public static class DummyException extends RuntimeException {
    public DummyException(String _message) { super( _message ); }
  }

  def out
  def setup() {
    out = Mock(java.io.PrintStream)
    out.println(_) >> {return 1}
    GithubEnterpriseConstructor = loadPipelineScriptForTest("github/github_enterprise_constructor.groovy")
    GithubEnterpriseConstructor.getBinding().setVariable("env", [GIT_URL: "giturl", ORG_NAME: "orgname", REPO_NAME: "reponame", GIT_SHA: "gitsha", CHANGE_TARGET: false, GIT_BUILD_CAUSE: "gitbuildcause"])
    GithubEnterpriseConstructor.getBinding().setProperty("out", out)
    explicitlyMockPipelineVariable("ScmUserRemoteConfig")
    explicitlyMockPipelineVariable("scm")
    getPipelineMock("scm.getUserRemoteConfigs")() >> { [getPipelineMock("ScmUserRemoteConfig")] }
    getPipelineMock("ScmUserRemoteConfig.getUrl")() >> "https://github.com/example-org/example-repo.git"
    getPipelineMock("sh")(_ as Map) >> " 1 "
    //explicitlyMockPipelineVariable("out")

  }

  def "unstash workspace before calling git commands" () {
    when:
      GithubEnterpriseConstructor(context)
    then:
      1 * getPipelineMock("unstash")("workspace")
    then:
      _ * getPipelineMock("sh")({ try {it.script =~ /git/; } catch(any) {it =~ /git/} }) >> "1"
      //returning the arbitrary string "1" to prevent the script from failing
  }

  def "GIT_URL env var is retreived from the scm object" () {
    when:
      GithubEnterpriseConstructor(context)
    then:
      2 * getPipelineMock("scm.getUserRemoteConfigs")() >> { [getPipelineMock("ScmUserRemoteConfig")] }
      1 * getPipelineMock("ScmUserRemoteConfig.getUrl")() >> "https://github.com/boozallen/jenkins-templating-engine.git"
      GithubEnterpriseConstructor.getBinding().variables.env.GIT_URL == "https://github.com/boozallen/jenkins-templating-engine.git"
  }

  def "GIT_CREDENTIAL_ID env var is retreived from the scm object" () {
    when:
    GithubEnterpriseConstructor(context)
    then:
    2 * getPipelineMock("scm.getUserRemoteConfigs")() >> { [getPipelineMock("ScmUserRemoteConfig")] }
    1 * getPipelineMock("ScmUserRemoteConfig.getProperty").call('credentialsId') >> "credential"
    GithubEnterpriseConstructor.getBinding().variables.env.GIT_CREDENTIAL_ID == "credential"
  }

  def "ORG_NAME is set to the GitHub organization in GIT_URL" () {
    when:
      GithubEnterpriseConstructor(context)
    then:
      getPipelineMock("ScmUserRemoteConfig.getUrl")() >> "https://github.com/library-test/jenkins-templating-engine.git"
      GithubEnterpriseConstructor.getBinding().variables.env.ORG_NAME == "library-test"
  }

  def "REPO_NAME is set to the GitHub repository in GIT_URL" () {
    when:
      GithubEnterpriseConstructor(context)
    then:
      getPipelineMock("ScmUserRemoteConfig.getUrl")() >> "https://github.com/boozallen/library-test2.git"
      GithubEnterpriseConstructor.getBinding().variables.env.REPO_NAME == "library-test2"
  }

  def "GIT_SHA is set to HEAD" () {
    when:
      GithubEnterpriseConstructor(context)
    then:
      1 * getPipelineMock("sh")([script: "git rev-parse HEAD", returnStdout: true]) >> script_output
      GithubEnterpriseConstructor.getBinding().variables.env.GIT_SHA == git_sha
    where:
      script_output   | git_sha
      "abcd12345"     | "abcd12345"
      "\nefgh67890\n" | "efgh67890"
  }

  def "If CHANGE_TARGET is truthy set GIT_BUILD_CAUSE to pr" () {
    setup:
      GithubEnterpriseConstructor.getBinding().variables.env.put("CHANGE_TARGET", change_target)
    when:
      GithubEnterpriseConstructor(context)
    then:
      GithubEnterpriseConstructor.getBinding().variables.env.GIT_BUILD_CAUSE == build_cause
    where:
      change_target | build_cause
      true          | "pr"
      "true"        | "pr"
      false         | "commit"
      null          | "commit"
  }

  def "If the latest commit has more than 2 parents, set GIT_BUILD_CAUSE to merge, else set to commit" () {
    when:
      GithubEnterpriseConstructor(context)
    then:
      1 * getPipelineMock("sh")([script: 'git rev-list HEAD --parents -1 | wc -w', returnStdout: true]) >> script_output
      GithubEnterpriseConstructor.getBinding().variables.env.GIT_BUILD_CAUSE == build_cause
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
      GithubEnterpriseConstructor(context)
    then:
      1 * out.println(_) >> { _arguments ->
        def build_cause = GithubEnterpriseConstructor.getBinding().variables.env.GIT_BUILD_CAUSE
        assert _arguments[0] == "Found Git Build Cause: ${build_cause}"
      }

  }

}
