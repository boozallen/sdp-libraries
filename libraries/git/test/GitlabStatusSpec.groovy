package libraries.git

public class GitlabStatusSpec extends JTEPipelineSpecification {

  def GitlabStatus = null
  def config = [distribution:"gitlab"]
  def env = [git_url_with_creds: "git_url_with_creds"]

  def setup() {
    GitlabStatus = loadPipelineScriptForStep("git", "gitlab_status")
    GitlabStatus.getBinding().setProperty("config", config)
    GitlabStatus.getBinding().setVariable("env", env)
  }

  def "If inputs are null, throw error" () {
    when:
      GitlabStatus()
    then:
      thrown(RuntimeException)
      1 * getPipelineMock("error")({ it =~ /gitlab connection must be a valid string/ }) >> { throw new RuntimeException("invalid action")}
  }

  def "If config.gitlab values are set, call properties and updateGitlabCommitStatus" () {
    setup:
      config.gitlab = [connection: 'https://gitlab.com/test',
            job_name : "j1",
            job_status : "pending"]
    when:
      GitlabStatus()
    then:
      1 * getPipelineMock("properties")([config.gitlab.connection]) >> { return }
      1 * explicitlyMockPipelineStep('gitLabConnection').call(config.gitlab.connection) >> { x -> return x[0] }
      1 * explicitlyMockPipelineStep("updateGitlabCommitStatus").call([name: config.gitlab.job_name, state: config.gitlab.job_status]) >> { return }
  }

  def "Argument should be used even with config.gitlab values are set, call properties and updateGitlabCommitStatus" () {
    setup:
      String job_name = 'j0'
      String job_status = "running"
      String connection = "https://gitlab.com/arg"
      config.gitlab = [connection: 'https://gitlab.com/test',
            job_name : "j1",
            job_status : "pending"]
    when:
      GitlabStatus(connection,job_name,job_status)
    then:
      1 * getPipelineMock("properties")([connection]) >> { return }
      0 * explicitlyMockPipelineStep('gitLabConnection').call(config.gitlab.connection)
      1 * explicitlyMockPipelineStep('gitLabConnection').call(connection) >> { x -> return x[0] }
      1 * explicitlyMockPipelineStep("updateGitlabCommitStatus").call([name: job_name, state: job_status]) >> { return }
  }


}
