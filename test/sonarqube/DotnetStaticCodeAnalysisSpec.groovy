package libraries.sonarqube

public class DotnetStaticCodeAnalysisSpec extends JTEPipelineSpecification {

  def DotnetStaticCodeAnalysis = null
  def Sonarqube = null
  
  def setup() {
    DotnetStaticCodeAnalysis = loadPipelineScriptForStep("sonarqube", "dotnet_static_code_analysis")
    Sonarqube = loadPipelineScriptForTest("sonarqube/steps/sonarqube.groovy")
    Sonarqube = new SonarqubeHelper().setupMocks(Sonarqube)
    DotnetStaticCodeAnalysis.getBinding().setVariable("sonarqube", Sonarqube)

    explicitlyMockPipelineStep("inside_sdp_image")
  }

  def "Dotnet Static Code Analysis runs commands with token" () {
    setup:
      LinkedHashMap config = [ project_key : "my-project" ]
      LinkedHashMap env = [ REPO_NAME: "my-repo", WORKSPACE: "my-workspace", sq_token: "my-token" ]

      DotnetStaticCodeAnalysis.getBinding().setVariable("config", config)
      DotnetStaticCodeAnalysis.getBinding().setVariable("env", env)
      Sonarqube.getBinding().setVariable("config", config)
    when:
      DotnetStaticCodeAnalysis()
    then:
      /* .NET Sonar Scanner Commands */
      1 * getPipelineMock("sh")('dotnet sonarscanner begin /k:my-project /n:my-repo /d:sonar.verbose=true /d:sonar.cs.opencover.reportsPaths=\'TestResults/**/coverage.opencover.xml\' /d:sonar.cs.vstest.reportsPaths=\'TestResults/*.trx\' /d:sonar.login=\'my-token\'')
      1 * getPipelineMock("sh")("rm -drf my-workspace/TestResults")
      1 * getPipelineMock("sh")("dotnet build")
      1 * getPipelineMock("sh")("dotnet test --settings coverlet.runsettings --results-directory my-workspace/TestResults --logger trx")
      1 * getPipelineMock("sh")('dotnet sonarscanner end /d:sonar.login=\'my-token\'')
  }

  def "Dotnet Static Code Analysis runs commands with username and pass" () {
    setup:
      LinkedHashMap config = [project_key : "my-project" ]
      LinkedHashMap env = [ REPO_NAME: "my-repo", WORKSPACE: "my-workspace", sq_user: "my-user", sq_token: "my-token" ]

      DotnetStaticCodeAnalysis.getBinding().setVariable("config", config)
      DotnetStaticCodeAnalysis.getBinding().setVariable("env", env)
      Sonarqube.getBinding().setVariable("config", config)
    when:
      DotnetStaticCodeAnalysis()
    then:
      /* .NET Sonar Scanner Commands */
      1 * getPipelineMock("sh")('dotnet sonarscanner begin /k:my-project /n:my-repo /d:sonar.verbose=true /d:sonar.cs.opencover.reportsPaths=\'TestResults/**/coverage.opencover.xml\' /d:sonar.cs.vstest.reportsPaths=\'TestResults/*.trx\' /d:sonar.login=\'my-user\' /d:sonar.password=\'my-token\'')
      1 * getPipelineMock("sh")("rm -drf my-workspace/TestResults")
      1 * getPipelineMock("sh")("dotnet build")
      1 * getPipelineMock("sh")("dotnet test --settings coverlet.runsettings --results-directory my-workspace/TestResults --logger trx")
      1 * getPipelineMock("sh")('dotnet sonarscanner end /d:sonar.login=\'my-user\' /d:sonar.password=\'my-token\'')
  }

  def "Dotnet Static Code Analysis runs commands project key from env repo" () {
    setup:
      LinkedHashMap config = []
      LinkedHashMap env = [ REPO_NAME: "my-repo", WORKSPACE: "my-workspace", sq_token: "my-token" ]

      DotnetStaticCodeAnalysis.getBinding().setVariable("config", config)
      DotnetStaticCodeAnalysis.getBinding().setVariable("env", env)
      Sonarqube.getBinding().setVariable("config", config)
    when:
      DotnetStaticCodeAnalysis()
    then:
      /* .NET Sonar Scanner Commands */
      1 * getPipelineMock("sh")('dotnet sonarscanner begin /k:my-repo /n:my-repo /d:sonar.verbose=true /d:sonar.cs.opencover.reportsPaths=\'TestResults/**/coverage.opencover.xml\' /d:sonar.cs.vstest.reportsPaths=\'TestResults/*.trx\' /d:sonar.login=\'my-token\'')
      1 * getPipelineMock("sh")("rm -drf my-workspace/TestResults")
      1 * getPipelineMock("sh")("dotnet build")
      1 * getPipelineMock("sh")("dotnet test --settings coverlet.runsettings --results-directory my-workspace/TestResults --logger trx")
      1 * getPipelineMock("sh")('dotnet sonarscanner end /d:sonar.login=\'my-token\'')
  }

  def "Dotnet Static Code Analysis runs commands project key from env org and repo" () {
    setup:
      LinkedHashMap config = []
      LinkedHashMap env = [ ORG_NAME: 'my-org', REPO_NAME: "my-repo", WORKSPACE: "my-workspace", sq_token: "my-token" ]

      DotnetStaticCodeAnalysis.getBinding().setVariable("config", config)
      DotnetStaticCodeAnalysis.getBinding().setVariable("env", env)
      Sonarqube.getBinding().setVariable("config", config)
    when:
      DotnetStaticCodeAnalysis()
    then:
      /* .NET Sonar Scanner Commands */
      1 * getPipelineMock("sh")('dotnet sonarscanner begin /k:my-org:my-repo /n:my-repo /d:sonar.verbose=true /d:sonar.cs.opencover.reportsPaths=\'TestResults/**/coverage.opencover.xml\' /d:sonar.cs.vstest.reportsPaths=\'TestResults/*.trx\' /d:sonar.login=\'my-token\'')
      1 * getPipelineMock("sh")("rm -drf my-workspace/TestResults")
      1 * getPipelineMock("sh")("dotnet build")
      1 * getPipelineMock("sh")("dotnet test --settings coverlet.runsettings --results-directory my-workspace/TestResults --logger trx")
      1 * getPipelineMock("sh")('dotnet sonarscanner end /d:sonar.login=\'my-token\'')
  }
}
