/*
  Copyright © 2018 Booz Allen Hamilton. All Rights Reserved.
  This software package is licensed under the Booz Allen Public License. The license can be found in the License file or at http://boozallen.github.io/licenses/bapl
*/

package libraries.openshift

public class DeployToSpec extends JTEPipelineSpecification {

  def DeployTo = null

  public static class DummyException extends RuntimeException {
		public DummyException(String _message) { super( _message ); }
	}

  def setup() {
    DeployTo = loadPipelineScriptForStep("openshift","deploy_to")
    explicitlyMockPipelineVariable("out")
    explicitlyMockPipelineVariable("push")
    explicitlyMockPipelineStep("withGit")
    explicitlyMockPipelineStep("inside_sdp_image")
    explicitlyMockPipelineStep("retag")

    DeployTo.getBinding().setVariable("env", [REPO_NAME: "unit-test", GIT_SHA: "abcd1234"])
    DeployTo.getBinding().setVariable("token", "token")

    getPipelineMock("readYaml")(_ as Map) >> [
      global: [
        repos: [
          [name: "unit-test", sha: "efgh5678"]
        ]
      ]
    ]
  }

  /*************************
   Variable Assignment Logic
  *************************/

  def "Throw error if helm_configuration_repository is not defined" () {
    setup:
      def app_env = [short_name: 'env', long_name: 'Environment']
      DeployTo.getBinding().setVariable("config", [:])
      DeployTo.getBinding().setVariable("pipelineConfig", [github_credential: null])
    when:
      DeployTo(app_env)
    then:
      1 * getPipelineMock('error')("helm_configuration_repository not defined in library config or application environment config")
  }

  def "Use the library config's helm_configuration_repository if not set in app_env" () {
    setup:
      def app_env = [short_name: 'env', long_name: 'Environment', helm_configuration_repository: null]
      DeployTo.getBinding().setVariable("config", [helm_configuration_repository: "config_hcr"])
      DeployTo.getBinding().setVariable("pipelineConfig", [github_credential: null])
    when:
      DeployTo(app_env)
    then:
      1 * getPipelineMock("withGit")( _ ) >> { _arguments ->
        assert _arguments[0][0].url == "config_hcr"
      }
  }

  def "Use the app_env's helm_configuration_repository if defined" () {
    setup:
      def app_env = [short_name: 'env', long_name: 'Environment', helm_configuration_repository: "app_env_hcr"]
      DeployTo.getBinding().setVariable("config", [helm_configuration_repository: "config_hcr"])
      DeployTo.getBinding().setVariable("pipelineConfig", [github_credential: null])
    when:
      DeployTo(app_env)
    then:
      1 * getPipelineMock("withGit")( _ ) >> { _arguments ->
        assert _arguments[0][0].url == "app_env_hcr"
      }
  }

  def "Throw error if helm_configuration_repository_credential (HCRC) not defined" () {
    setup:
      def app_env = [short_name: 'env', long_name: 'Environment', helm_configuration_repository_credential: null]
      DeployTo.getBinding().setVariable("config", [helm_configuration_repository_credential: null])
      DeployTo.getBinding().setVariable("pipelineConfig", [github_credential: null])
    when:
      DeployTo(app_env)
    then:
      1 * getPipelineMock("error")("GitHub Credential For Configuration Repository Not Defined")
  }

  def "Use the github_credential if HCRC not defined in the library config or app_env" () {
    setup:
      def app_env = [short_name: 'env', long_name: 'Environment', helm_configuration_repository_credential: null]
      DeployTo.getBinding().setVariable("config", [helm_configuration_repository_credential: null])
      DeployTo.getBinding().setVariable("pipelineConfig", [github_credential: "github_credential"])
    when:
      DeployTo(app_env)
    then:
      1 * getPipelineMock("withGit")( _ ) >> { _arguments ->
        assert _arguments[0][0].cred == "github_credential"
      }
  }

  def "Use the HCRC defined by the library config if not defined by the app_env" () {
    setup:
      def app_env = [short_name: 'env', long_name: 'Environment', helm_configuration_repository_credential: null]
      DeployTo.getBinding().setVariable("config", [helm_configuration_repository_credential: "config_hcrc"])
      DeployTo.getBinding().setVariable("pipelineConfig", [github_credential: "github_credential"])
    when:
      DeployTo(app_env)
    then:
      1 * getPipelineMock("withGit")( _ ) >> { _arguments ->
        assert _arguments[0][0].cred == "config_hcrc"
      }
  }

  def "Use the HCRC defined by the app_env if available" () {
    setup:
      def app_env = [short_name: 'env', long_name: 'Environment', helm_configuration_repository_credential: "app_env_hcrc"]
      DeployTo.getBinding().setVariable("config", [helm_configuration_repository_credential: "config_hcrc"])
      DeployTo.getBinding().setVariable("pipelineConfig", [github_credential: "github_credential"])
    when:
      DeployTo(app_env)
    then:
      1 * getPipelineMock("withGit")( _ ) >> { _arguments ->
        assert _arguments[0][0].cred == "app_env_hcrc"
      }
  }

  def "Throw error if tiller_namespace is not defined" () {
    setup:
      def app_env = [short_name: 'env', long_name: 'Environment', tiller_namespace: null]
      DeployTo.getBinding().setVariable("config", [tiller_namespace: null])
      DeployTo.getBinding().setVariable("pipelineConfig", [github_credential: null])
    when:
      DeployTo(app_env)
    then:
      1 * getPipelineMock("error")("Tiller Namespace Not Defined")
  }

  def "Use tiller_namespace defined by the library config if not defined by the app_env" () {
    setup:
      def app_env = [short_name: 'env', long_name: 'Environment', tiller_namespace: null]
      DeployTo.getBinding().setVariable("config", [tiller_namespace: "config_tiller"])
      DeployTo.getBinding().setVariable("pipelineConfig", [github_credential: null])
    when:
      DeployTo(app_env)
    then:
      1 * getPipelineMock("withEnv")( _ ) >> { _arguments ->
        assert _arguments[0][0] == ["TILLER_NAMESPACE=config_tiller"]
      }
  }

  def "Use the tiller_namespace defined by the app_env if available" () {
    setup:
      def app_env = [short_name: 'env', long_name: 'Environment', tiller_namespace: "app_env_tiller"]
      DeployTo.getBinding().setVariable("config", [tiller_namespace: "config_tiller"])
      DeployTo.getBinding().setVariable("pipelineConfig", [github_credential: null])
    when:
      DeployTo(app_env)
    then:
      1 * getPipelineMock("withEnv")( _ ) >> { _arguments ->
        assert _arguments[0][0] == ["TILLER_NAMESPACE=app_env_tiller"]
      }
  }

  def "Throw error if tiller_credential is not defined" () {
    setup:
      def app_env = [short_name: 'env', long_name: 'Environment', tiller_credential: null]
      DeployTo.getBinding().setVariable("config", [tiller_credential: null])
      DeployTo.getBinding().setVariable("pipelineConfig", [github_credential: null])
    when:
      DeployTo(app_env)
    then:
      1 * getPipelineMock("error")("Tiller Credential Not Defined")
  }

  def "Use tiller_credential defined by the library config if not defined by the app_env" () {
    setup:
      def app_env = [short_name: 'env', long_name: 'Environment', tiller_credential: null]
      DeployTo.getBinding().setVariable("config", [tiller_credential: "config_tc"])
      DeployTo.getBinding().setVariable("pipelineConfig", [github_credential: null])
    when:
      DeployTo(app_env)
    then:
      1 * getPipelineMock("usernamePassword.call")( _ as Map ) >> { _arguments ->
        assert _arguments[0].credentialsId == "config_tc"
      }
  }

  def "Use the tiller_credential defined by the app_env if available" () {
    setup:
      def app_env = [short_name: 'env', long_name: 'Environment', tiller_credential: "app_env_tc"]
      DeployTo.getBinding().setVariable("config", [tiller_credential: "config_tc"])
      DeployTo.getBinding().setVariable("pipelineConfig", [github_credential: null])
    when:
      DeployTo(app_env)
    then:
      1 * getPipelineMock("usernamePassword.call")( _ as Map ) >> { _arguments ->
        assert _arguments[0].credentialsId == "app_env_tc"
      }
  }

  def "Throw error if openshift_url is not defined" () {
    setup:
      def app_env = [short_name: 'env', long_name: 'Environment', openshift_url: null]
      DeployTo.getBinding().setVariable("config", [url: null])
      DeployTo.getBinding().setVariable("pipelineConfig", [github_credential: null])
    when:
      DeployTo(app_env)
    then:
      1 * getPipelineMock("error")("OpenShift URL Not Defined")
  }

  def "Use openshift_url defined by the library config if not defined by the app_env" () {
    setup:
      def app_env = [short_name: 'env', long_name: 'Environment', openshift_url: null]
      DeployTo.getBinding().setVariable("config", [url: "config_url"])
      DeployTo.getBinding().setVariable("pipelineConfig", [github_credential: null])
    when:
      DeployTo(app_env)
    then:
      1 * getPipelineMock("sh")( {it =~ /oc login --insecure-skip-tls-verify config_url.+/} )
  }

  def "Use the openshift_url defined by the app_env if available" () {
    setup:
      def app_env = [short_name: 'env', long_name: 'Environment', openshift_url: "app_env_url"]
      DeployTo.getBinding().setVariable("config", [url: "config_url"])
      DeployTo.getBinding().setVariable("pipelineConfig", [github_credential: null])
    when:
      DeployTo(app_env)
    then:
      1 * getPipelineMock("sh")( {it =~ /oc login --insecure-skip-tls-verify app_env_url.+/} )
  }

  def "Throw error if both tiller_release_name and short_name are undefined" () {
    setup:
      def app_env = [short_name: null, long_name: 'Environment', tiller_release_name: null]
      DeployTo.getBinding().setVariable("config", [:])
      DeployTo.getBinding().setVariable("pipelineConfig", [github_credential: null])
    when:
      DeployTo(app_env)
    then:
      1 * getPipelineMock("error")( "App Env Must Specify tiller_release_name or short_name" )
  }

  def "Use app_env.short_name for release name if app_env.tiller_release_name is undefined" () {
    setup:
      def app_env = [short_name: 'short_name', long_name: 'Environment', tiller_release_name: null]
      DeployTo.getBinding().setVariable("config", [:])
      DeployTo.getBinding().setVariable("pipelineConfig", [github_credential: null])
    when:
      DeployTo(app_env)
    then:
      1 * getPipelineMock("sh")( { (it instanceof Map) ? it?.script =~ "helm history --max 1 short_name" : false} )
  }

  def "Use app_env.tiller_release_name for release name if available" () {
    setup:
      def app_env = [short_name: 'short_name', long_name: 'Environment', tiller_release_name: "app_env_trn"]
      DeployTo.getBinding().setVariable("config", [:])
      DeployTo.getBinding().setVariable("pipelineConfig", [github_credential: null])
    when:
      DeployTo(app_env)
    then:
      1 * getPipelineMock("sh")( { (it instanceof Map) ? it?.script =~ "helm history --max 1 app_env_trn" : false} )
  }

  def "Throw error if no values file is defined" () {
    setup:
      def app_env = [short_name: null, long_name: 'Environment', chart_values_file: null]
      DeployTo.getBinding().setVariable("config", [:])
      DeployTo.getBinding().setVariable("pipelineConfig", [github_credential: null])
    when:
      DeployTo(app_env)
    then:
      1 * getPipelineMock("error")( "Values File To Use For This Chart Not Defined" )
  }

  def "Use the short name to define the values file if app_env.chart_values_file is not set" () {
    setup:
      def app_env = [short_name: 'env', long_name: 'Environment', chart_values_file: null]
      DeployTo.getBinding().setVariable("config", [:])
      DeployTo.getBinding().setVariable("pipelineConfig", [github_credential: null])
    when:
      DeployTo(app_env)
    then:
      1 * getPipelineMock("sh")( "rm values.env.yaml" )
  }

  def "Use app_env.chart_values_file for the values_file if available" () {
    setup:
      def app_env = [short_name: 'env', long_name: 'Environment', chart_values_file: "special_values_file.yaml"]
      DeployTo.getBinding().setVariable("config", [:])
      DeployTo.getBinding().setVariable("pipelineConfig", [github_credential: null])
    when:
      DeployTo(app_env)
    then:
      1 * getPipelineMock("sh")( "rm special_values_file.yaml" )
  }
  
  def "Checkout master branch of HCR if no helm_chart_branch from app_env" () {
    setup:
      def app_env = [short_name: 'env', long_name: 'Environment', helm_chart_branch: null]
      DeployTo.getBinding().setVariable("config", [:])
      DeployTo.getBinding().setVariable("pipelineConfig", [github_credential: null])
    when:
      DeployTo(app_env)
    then:
      1 * getPipelineMock("withGit")( { (it[0] instanceof Map) ? it[0]?.branch == "master" : false} )
  }
  
  def "Checkout app_env.helm_chart_branch of HCR if defined" () {
    setup:
      def app_env = [short_name: 'env', long_name: 'Environment', helm_chart_branch: "Mercator"]
      DeployTo.getBinding().setVariable("config", [:])
      DeployTo.getBinding().setVariable("pipelineConfig", [github_credential: null])
    when:
      DeployTo(app_env)
    then:
      1 * getPipelineMock("withGit")( { (it[0] instanceof Map) ? it[0]?.branch == "Mercator" : false} )
  }
  

  def "Don't retag the previous image if there is no Feature SHA" () {
    // and we can't expect such a corresponding image to exist
    setup:
      def app_env = [short_name: 'env', long_name: 'Enviornment', promote_previous_image: app_env_val]
      DeployTo.getBinding().setVariable("config", [promote_previous_image: config_val])
      DeployTo.getBinding().setVariable("env", [FEATURE_SHA: null, GIT_SHA: "git_sha", REPO_NAME: "unit-test"])
      DeployTo.getBinding().setVariable("pipelineConfig", [github_credential: null])
    when:
      DeployTo(app_env)
    then:
      0 * getPipelineMock("retag")(_, _)
    where:
      app_env_val | config_val
      true        | true
      false       | true
      true        | false
      false       | false
      null        | true
      null        | false
  }

  def "By default, Retag the previously built image for promotion if there is a Feature SHA" () {
    setup:
      def app_env = [short_name: 'env', long_name: 'Enviornment']
      DeployTo.getBinding().setVariable("env", [FEATURE_SHA: "feature_sha", GIT_SHA: "git_sha", REPO_NAME: "unit-test"])
      DeployTo.getBinding().setVariable("config", [:])
      DeployTo.getBinding().setVariable("pipelineConfig", [github_credential: null])
    when:
      DeployTo(app_env)
    then:
      1 * getPipelineMock("retag")("feature_sha", "git_sha")
  }

  def "Use config.promote_previous_image to determine whether or not to promote a previous image if not set in the app_env" () {
    setup:
      def app_env = [short_name: 'env', long_name: 'Enviornment']
      DeployTo.getBinding().setVariable('env', [FEATURE_SHA: "feature_sha", GIT_SHA: "git_sha", REPO_NAME: "unit-test"])
      DeployTo.getBinding().setVariable("config", [promote_previous_image: false])
      DeployTo.getBinding().setVariable("pipelineConfig", [github_credential: null])
      when:
        DeployTo(app_env)
      then:
        0 * getPipelineMock("retag")(_, _)
        1 * getPipelineMock("echo")("expecting image was already built")
  }

  def "Use app_env.promote_previous_image, if available, to determine whether or not to promote a previous image" () {
    setup:
      def app_env = [short_name: 'env', long_name: 'Enviornment', promote_previous_image: false]
      DeployTo.getBinding().setVariable('env', [FEATURE_SHA: "feature_sha", GIT_SHA: "git_sha", REPO_NAME: "unit-test"])
      DeployTo.getBinding().setVariable("config", [promote_previous_image: true])
      DeployTo.getBinding().setVariable("pipelineConfig", [github_credential: null])
      when:
        DeployTo(app_env)
      then:
        0 * getPipelineMock("retag")(_, _)
        1 * getPipelineMock("echo")("expecting image was already built")
  }

  def "Promote the previously built image if we choose to and there is a Feature SHA" () {
    setup:
      def app_env = [short_name: 'env', long_name: 'Enviornment', FEATURE_SHA: "feature_sha", GIT_SHA: "git_sha", promote_previous_image: app_env_val]
      DeployTo.getBinding().setVariable("env", [FEATURE_SHA: "feature_sha", GIT_SHA: "git_sha", REPO_NAME: "unit-test"])
      DeployTo.getBinding().setVariable("config", [promote_previous_image: config_val])
      DeployTo.getBinding().setVariable("pipelineConfig", [github_credential: null])
      when:
        DeployTo(app_env)
      then:
        1 * getPipelineMock("retag")("feature_sha", "git_sha")
      where:
        app_env_val | config_val
        true        | true
        true        | false
        true        | null
        null        | true
        null        | null
  }

  def "Don't retag the previous image if we choose not to, even if there is a feature SHA" () {
    setup:
      def app_env = [short_name: 'env', long_name: 'Enviornment', promote_previous_image: app_env_val]
      DeployTo.getBinding().setVariable("config", [promote_previous_image: config_val])
      DeployTo.getBinding().setVariable("env", [FEATURE_SHA: "feature_sha", GIT_SHA: "git_sha", REPO_NAME: "unit-test"])
      DeployTo.getBinding().setVariable("pipelineConfig", [github_credential: null])
      when:
        DeployTo(app_env)
      then:
        0 * getPipelineMock("retag")(_, _)
        1 * getPipelineMock("echo")("expecting image was already built")
      where:
        app_env_val | config_val
        false       | false
        false       | true
        false       | null
        null        | false
  }


  /**************************
   update_values_file() tests
  ***************************/

  def "Throw error if values_file is not in the config_repo" () {
    setup:
      def app_env = [short_name: 'env', long_name: 'Environment', chart_values_file: "The limit of f(x) as x approaches 0"]
      DeployTo.getBinding().setVariable("config", [helm_configuration_repository: "the equation f(x) = 1/x"])
      DeployTo.getBinding().setVariable("pipelineConfig", [github_credential: null])
    when:
      DeployTo(app_env)
    then:
      1 * getPipelineMock("fileExists")("The limit of f(x) as x approaches 0") >> false
      1 * getPipelineMock("error")("Values File The limit of f(x) as x approaches 0 does not exist in the equation f(x) = 1/x")
  }

  def "Values file is updated with new Git SHA" () {
    setup:
      def app_env = [short_name: 'env', long_name: 'Environment']
      DeployTo.getBinding().setVariable("config", [:])
      // env.REPO_NAME and env.GIT_SHA set above in setup()
      DeployTo.getBinding().setVariable("pipelineConfig", [github_credential: null])
    when:
      DeployTo(app_env)
    then:
      1 * getPipelineMock("readYaml")([file: "values.env.yaml"]) >>  [global: [repos: [[name: "unit-test", sha: "efgh5678"]]]]
      1 * getPipelineMock("echo")("writing new Git SHA abcd1234 for repo unit-test in values.env.yaml")
      1 * getPipelineMock("sh")("rm values.env.yaml") // remove the old file to write a new one
      1 * getPipelineMock("writeYaml")([file: "values.env.yaml", data: [global: [repos: [[name: "unit-test", sha: "abcd1234"]]]]])
  }

  /******************
   do_release() tests
  *******************/

  def "If the chart was never deployed, use helm install" () {
    setup:
      def app_env = [short_name: 'env', long_name: 'Environment']
      DeployTo.getBinding().setVariable("config", [:])
      DeployTo.getBinding().setVariable("pipelineConfig", [github_credential: null])
    when:
      DeployTo(app_env)
    then:
      //Check if chart exists by checking helm history; assert it doesn't exist
      //NOTE: the helm history command's return value's truthyness seems unintuitive
      1 * getPipelineMock("sh")( { (it instanceof Map) ? it?.script =~ "helm history --max 1 .+" : false} ) >> true
      1 * getPipelineMock("sh")( { it =~ /helm install \..*/} )
  }

  def "If the chart has already been deployed, use helm upgrade" () {
    setup:
      def app_env = [short_name: 'env', long_name: 'Environment']
      DeployTo.getBinding().setVariable("config", [:])
      DeployTo.getBinding().setVariable("pipelineConfig", [github_credential: null])
    when:
      DeployTo(app_env)
    then:
      1 * getPipelineMock("sh")( { (it instanceof Map) ? it?.script =~ "helm history --max 1 .+" : false} ) >> false
      1 * getPipelineMock("sh")( { it =~ /helm upgrade (\-\-install|) env \..*/} )
  }

  def "Chart deploys with the defined release and values_file" () {
    setup:
      def app_env = [short_name: 'env', long_name: 'Environment']
      DeployTo.getBinding().setVariable("config", [:])
      DeployTo.getBinding().setVariable("pipelineConfig", [github_credential: null])
    when:
      DeployTo(app_env)
    then:
      1 * getPipelineMock("sh")( { it =~ /helm (upgrade (\-\-install|) env \.|install \. env) -f values.env.yaml/} )
  }

  /*****************
   oc_login() tests
  *****************/

  def "The step logs into the specified Openshift cluster with the provided token" () {
    setup:
      def app_env = [short_name: 'env', long_name: 'Environment', openshift_url: "specified_url"]
      DeployTo.getBinding().setVariable("config", [:])
      DeployTo.getBinding().setVariable("token", "provided_token")
      DeployTo.getBinding().setVariable("pipelineConfig", [github_credential: null])
    when:
      DeployTo(app_env)
    then:
      1 * getPipelineMock("sh")( {it =~ /oc login .* specified_url --token=provided_token.*/} )
  }

  def "If the credential is not for a token, check if it's a username/password" () {
    setup:
      def app_env = [short_name: 'env', long_name: 'Environment', openshift_url: "specified_url"]
      DeployTo.getBinding().setVariable("config", [:])
      DeployTo.getBinding().setVariable("user", "user")
      DeployTo.getBinding().setVariable("pipelineConfig", [github_credential: null])
    when:
      DeployTo(app_env)
    then:
      1 * getPipelineMock("sh")( {it =~ /oc login .* specified_url --token=token.*/} ) >> {throw new DummyException("Bad Token")}
      1 * getPipelineMock("sh")( {it =~ /oc login .* specified_url -u user -p token.*/} )
  }

  /*************************
   push_config_update tests
  *************************/
  def "Changes to Values file are committed back to GitHub" () {
    setup:
      def app_env = [short_name: 'env', long_name: 'Environment']
      DeployTo.getBinding().setVariable("config", [:])
      DeployTo.getBinding().setVariable("pipelineConfig", [github_credential: null])
    when:
      DeployTo(app_env)
    then:
      1 * getPipelineMock("echo")("updating values file -> values.env.yaml")
      1 * getPipelineMock("git")( [add: "values.env.yaml"] )
      1 * getPipelineMock("git")( [commit: "Updating values.env.yaml for unit-test images"])
      1 * getPipelineMock("git")( getPipelineMock("push") )
  }
  
}
