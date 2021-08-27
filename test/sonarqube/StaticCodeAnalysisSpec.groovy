package libraries.sonarqube

public class StaticCodeAnalysis extends JTEPipelineSpecification {

  def StaticCodeAnalysis = null
  def Sonarqube = null;

  def setup() {
    StaticCodeAnalysis = loadPipelineScriptForStep("sonarqube", "static_code_analysis")
    Sonarqube = loadPipelineScriptForTest("sonarqube/steps/sonarqube.groovy")
    Sonarqube = new SonarqubeHelper().setupMocks(Sonarqube)
    StaticCodeAnalysis.getBinding().setVariable("sonarqube", Sonarqube)

    explicitlyMockPipelineStep("inside_sdp_image")
  }

  def "Static Code Analysis runs commands with token" () {
    setup:
      def config = new LinkedHashMap();
      def env = [sq_token: "my-token" ];

      StaticCodeAnalysis.getBinding().setVariable("config", config)
      StaticCodeAnalysis.getBinding().setVariable("env", env)
      Sonarqube.getBinding().setVariable("config", config)
    when:
      StaticCodeAnalysis()
    then:
      /* .Sonar Scanner Commands */
      1 * getPipelineMock("sh")('mkdir -p empty')
      1 * getPipelineMock("sh")('sonar-scanner -X -Dsonar.login=\'my-token\'')
  }

    def "Static Code Analysis runs commands with username and pass" () {
    setup:
      def config = new LinkedHashMap();
      def env = [sq_user: "my-user", sq_token: "my-token" ];

      StaticCodeAnalysis.getBinding().setVariable("config", config)
      StaticCodeAnalysis.getBinding().setVariable("env", env)
      Sonarqube.getBinding().setVariable("config", config)
    when:
      StaticCodeAnalysis()
    then:
      /* .Sonar Scanner Commands */
      1 * getPipelineMock("sh")('mkdir -p empty')
      1 * getPipelineMock("sh")('sonar-scanner -X -Dsonar.login=\'my-user\' -Dsonar.password=\'my-token\'')
    }
}
