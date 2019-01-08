/*
  Copyright © 2018 Booz Allen Hamilton. All Rights Reserved.
  This software package is licensed under the Booz Allen Public License. The license can be found in the License file or at http://boozallen.github.io/licenses/bapl
*/

import com.homeaway.devtools.jenkins.testing.JenkinsPipelineSpecification

public class EphemeralSpec extends JenkinsPipelineSpecification {

  def Ephemeral = null

  public static class DummyException extends RuntimeException {
		public DummyException(String _message) { super( _message ); }
	}

  def setup() {
    Ephemeral = loadPipelineScriptForTest("openshift/ephemeral.groovy")
    explicitlyMockPipelineVariable("out")
    explicitlyMockPipelineStep("withGit")
    explicitlyMockPipelineStep("inside_sdp_image")


    Ephemeral.getBinding().setVariable("env", [REPO_NAME: "unit-test", GIT_SHA: "abcd1234", JOB_NAME: "app/test/PR-test", BUILD_NUMBER: '7'])
    Ephemeral.getBinding().setVariable("token", "token")

    getPipelineMock("readYaml")(_ as Map) >> [
      image_shas: [
        unit_test: "efgh5678"
      ]
    ]
    getPipelineMock("sh")(_ as Map) >> "ENV:\nA:Alpha\nB:Bravo\nC:Charlie"
  }

  /*************************
   Variable Assignment Logic
  *************************/

  def "Throw error if helm_configuration_repository is not defined" () {
    setup:
      def app_env = [short_name: 'env', long_name: 'Environment']
      Ephemeral.getBinding().setVariable("config", [:])
      Ephemeral.getBinding().setVariable("pipelineConfig", [github_credential: null])
    when:
      Ephemeral(app_env, {})
    then:
      1 * getPipelineMock('error')("helm_configuration_repository not defined in library config or application environment config")
  }

  def "Use the library config's helm_configuration_repository if not set in app_env" () {
    setup:
      def app_env = [short_name: 'env', long_name: 'Environment', helm_configuration_repository: null]
      Ephemeral.getBinding().setVariable("config", [helm_configuration_repository: "config_hcr"])
      Ephemeral.getBinding().setVariable("pipelineConfig", [github_credential: null])
    when:
      Ephemeral(app_env, {})
    then:
      1 * getPipelineMock("withGit")( _ ) >> { _arguments ->
        assert _arguments[0][0].url == "config_hcr"
      }
  }

  def "Use the app_env's helm_configuration_repository if defined" () {
    setup:
      def app_env = [short_name: 'env', long_name: 'Environment', helm_configuration_repository: "app_env_hcr"]
      Ephemeral.getBinding().setVariable("config", [helm_configuration_repository: "config_hcr"])
      Ephemeral.getBinding().setVariable("pipelineConfig", [github_credential: null])
    when:
      Ephemeral(app_env, {})
    then:
      1 * getPipelineMock("withGit")( _ ) >> { _arguments ->
        assert _arguments[0][0].url == "app_env_hcr"
      }
  }

  def "Throw error if helm_configuration_repository_credential (HCRC) not defined" () {
    setup:
      def app_env = [short_name: 'env', long_name: 'Environment', helm_configuration_repository_credential: null]
      Ephemeral.getBinding().setVariable("config", [helm_configuration_repository_credential: null])
      Ephemeral.getBinding().setVariable("pipelineConfig", [github_credential: null])
    when:
      Ephemeral(app_env, {})
    then:
      1 * getPipelineMock("error")("GitHub Credential For Configuration Repository Not Defined")
  }

  def "Use the github_credential if HCRC not defined in the library config or app_env" () {
    setup:
      def app_env = [short_name: 'env', long_name: 'Environment', helm_configuration_repository_credential: null]
      Ephemeral.getBinding().setVariable("config", [helm_configuration_repository_credential: null])
      Ephemeral.getBinding().setVariable("pipelineConfig", [github_credential: "github_credential"])
    when:
      Ephemeral(app_env, {})
    then:
      1 * getPipelineMock("withGit")( _ ) >> { _arguments ->
        assert _arguments[0][0].cred == "github_credential"
      }
  }

  def "Use the HCRC defined by the library config if not defined by the app_env" () {
    setup:
      def app_env = [short_name: 'env', long_name: 'Environment', helm_configuration_repository_credential: null]
      Ephemeral.getBinding().setVariable("config", [helm_configuration_repository_credential: "config_hcrc"])
      Ephemeral.getBinding().setVariable("pipelineConfig", [github_credential: "github_credential"])
    when:
      Ephemeral(app_env, {})
    then:
      1 * getPipelineMock("withGit")( _ ) >> { _arguments ->
        assert _arguments[0][0].cred == "config_hcrc"
      }
  }

  def "Use the HCRC defined by the app_env if available" () {
    setup:
      def app_env = [short_name: 'env', long_name: 'Environment', helm_configuration_repository_credential: "app_env_hcrc"]
      Ephemeral.getBinding().setVariable("config", [helm_configuration_repository_credential: "config_hcrc"])
      Ephemeral.getBinding().setVariable("pipelineConfig", [github_credential: "github_credential"])
    when:
      Ephemeral(app_env, {})
    then:
      1 * getPipelineMock("withGit")( _ ) >> { _arguments ->
        assert _arguments[0][0].cred == "app_env_hcrc"
      }
  }

  def "Throw error if tiller_namespace is not defined" () {
    setup:
      def app_env = [short_name: 'env', long_name: 'Environment', tiller_namespace: null]
      Ephemeral.getBinding().setVariable("config", [tiller_namespace: null])
      Ephemeral.getBinding().setVariable("pipelineConfig", [github_credential: null])
    when:
      Ephemeral(app_env, {})
    then:
      1 * getPipelineMock("error")("Tiller Namespace Not Defined")
  }

  def "Use tiller_namespace defined by the library config if not defined by the app_env" () {
    setup:
      def app_env = [short_name: 'env', long_name: 'Environment', tiller_namespace: null]
      Ephemeral.getBinding().setVariable("config", [tiller_namespace: "config_tiller"])
      Ephemeral.getBinding().setVariable("pipelineConfig", [github_credential: null])
    when:
      Ephemeral(app_env, {})
    then:
      1 * getPipelineMock("withEnv")({it[0][0] ? it[0][0] == "TILLER_NAMESPACE=config_tiller" : false})
  }

  def "Use the tiller_namespace defined by the app_env if available" () {
    setup:
      def app_env = [short_name: 'env', long_name: 'Environment', tiller_namespace: "app_env_tiller"]
      Ephemeral.getBinding().setVariable("config", [tiller_namespace: "config_tiller"])
      Ephemeral.getBinding().setVariable("pipelineConfig", [github_credential: null])
    when:
      Ephemeral(app_env, {})
    then:
      1 * getPipelineMock("withEnv")({ it[0][0] ? it[0][0] == "TILLER_NAMESPACE=app_env_tiller" : false})
  }

  def "Throw error if tiller_credential is not defined" () {
    setup:
      def app_env = [short_name: 'env', long_name: 'Environment', tiller_credential: null]
      Ephemeral.getBinding().setVariable("config", [tiller_credential: null])
      Ephemeral.getBinding().setVariable("pipelineConfig", [github_credential: null])
    when:
      Ephemeral(app_env, {})
    then:
      1 * getPipelineMock("error")("Tiller Credential Not Defined")
  }

  def "Use tiller_credential defined by the library config if not defined by the app_env" () {
    setup:
      def app_env = [short_name: 'env', long_name: 'Environment', tiller_credential: null]
      Ephemeral.getBinding().setVariable("config", [tiller_credential: "config_tc"])
      Ephemeral.getBinding().setVariable("pipelineConfig", [github_credential: null])
    when:
      Ephemeral(app_env, {})
    then:
      1 * getPipelineMock("usernamePassword.call")( _ as Map ) >> { _arguments ->
        assert _arguments[0].credentialsId == "config_tc"
      }
  }

  def "Use the tiller_credential defined by the app_env if available" () {
    setup:
      def app_env = [short_name: 'env', long_name: 'Environment', tiller_credential: "app_env_tc"]
      Ephemeral.getBinding().setVariable("config", [tiller_credential: "config_tc"])
      Ephemeral.getBinding().setVariable("pipelineConfig", [github_credential: null])
    when:
      Ephemeral(app_env, {})
    then:
      1 * getPipelineMock("usernamePassword.call")( _ as Map ) >> { _arguments ->
        assert _arguments[0].credentialsId == "app_env_tc"
      }
  }

  def "Throw error if openshift_url is not defined" () {
    setup:
      def app_env = [short_name: 'env', long_name: 'Environment', openshift_url: null]
      Ephemeral.getBinding().setVariable("config", [url: null])
      Ephemeral.getBinding().setVariable("pipelineConfig", [github_credential: null])
    when:
      Ephemeral(app_env, {})
    then:
      1 * getPipelineMock("error")("OpenShift URL Not Defined")
  }

  def "Use openshift_url defined by the library config if not defined by the app_env" () {
    setup:
      def app_env = [short_name: 'env', long_name: 'Environment', openshift_url: null]
      Ephemeral.getBinding().setVariable("config", [url: "config_url"])
      Ephemeral.getBinding().setVariable("pipelineConfig", [github_credential: null])
    when:
      Ephemeral(app_env, {})
    then:
      (1.._) * getPipelineMock("sh")( {it =~ /oc login --insecure-skip-tls-verify config_url.+/} )
  }

  def "Use the openshift_url defined by the app_env if available" () {
    setup:
      def app_env = [short_name: 'env', long_name: 'Environment', openshift_url: "app_env_url"]
      Ephemeral.getBinding().setVariable("config", [url: "config_url"])
      Ephemeral.getBinding().setVariable("pipelineConfig", [github_credential: null])
    when:
      Ephemeral(app_env, {})
    then:
      (1.._) * getPipelineMock("sh")( {it =~ /oc login --insecure-skip-tls-verify app_env_url.+/} )
  }

  def "Throw error if no values file is defined" () {
    setup:
      def app_env = [short_name: null, long_name: 'Environment', chart_values_file: null]
      Ephemeral.getBinding().setVariable("config", [:])
      Ephemeral.getBinding().setVariable("pipelineConfig", [github_credential: null])
    when:
      Ephemeral(app_env, {})
    then:
      1 * getPipelineMock("error")( "Values File To Use For This Chart Not Defined" )
  }

  def "Use the short name to define the values file if app_env.chart_values_file is not set" () {
    setup:
      def app_env = [short_name: 'env', long_name: 'Environment', chart_values_file: null]
      Ephemeral.getBinding().setVariable("config", [:])
      Ephemeral.getBinding().setVariable("pipelineConfig", [github_credential: null])
    when:
      Ephemeral(app_env, {})
    then:
      1 * getPipelineMock("sh")( "rm values.env.yaml" )
  }

  def "Use app_env.chart_values_file for the values_file if available" () {
    setup:
      def app_env = [short_name: 'env', long_name: 'Environment', chart_values_file: "special_values_file.yaml"]
      Ephemeral.getBinding().setVariable("config", [:])
      Ephemeral.getBinding().setVariable("pipelineConfig", [github_credential: null])
    when:
      Ephemeral(app_env, {})
    then:
      1 * getPipelineMock("sh")( "rm special_values_file.yaml" )
  }

  def "Throw error if image_repository_project is not defined" () {

  }

  /**************************
   core functionality tests
  **************************/

  def "The body gets executed" () {
    setup:
      def app_env = [short_name: 'env', long_name: 'Environment']
      Ephemeral.getBinding().setVariable("config", [:])
      Ephemeral.getBinding().setVariable("pipelineConfig", [github_credential: null])
    when:
      Ephemeral(app_env, {echo "hello world"})
    then:
      1 * getPipelineMock("echo")("hello world")
  }

  //There's room for improvement for this test - KO
  def "withEnv is used to pass in the release's environment variables" () {
    setup:
      def app_env = [short_name: 'env', long_name: 'Environment']
      Ephemeral.getBinding().setVariable("config", [:])
      Ephemeral.getBinding().setVariable("pipelineConfig", [github_credential: null])
      // environment variables set by a stub in setup()
    when:
      Ephemeral(app_env, {})
    then:
      1 * getPipelineMock("withEnv")({ it[0] ? it[0] == ["A=Alpha", "B=Bravo", "C=Charlie"] : false})
  }

  /**************************
   update_values_file() tests
  **************************/

  def "Throw error if values_file is not in the config_repo" () {
    setup:
      def app_env = [short_name: 'env', long_name: 'Environment', chart_values_file: "The limit"]
      Ephemeral.getBinding().setVariable("config", [:])
      Ephemeral.getBinding().setVariable("pipelineConfig", [github_credential: null])
    when:
      Ephemeral(app_env, {})
    then:
      1 * getPipelineMock("fileExists")("The limit") >> false
      1 * getPipelineMock("error")("Values File The limit does not exist in the given Helm configuration repo")
  }

  def "Values file is updated with new Git SHA" () {
    setup:
      def app_env = [short_name: 'env', long_name: 'Environment']
      Ephemeral.getBinding().setVariable("config", [:])
      // env.REPO_NAME and env.GIT_SHA set above in setup()
      Ephemeral.getBinding().setVariable("pipelineConfig", [github_credential: null])
    when:
      Ephemeral(app_env, {})
    then:
      1 * getPipelineMock("readYaml")([file: "values.env.yaml"]) >>  [image_shas: [unit_test: "efgh5678"]]
      1 * getPipelineMock("echo")("writing new Git SHA abcd1234 to image_shas.unit_test in values.env.yaml")
      1 * getPipelineMock("sh")("rm values.env.yaml") // remove the old file to write a new one
      1 * getPipelineMock("writeYaml")([file: "values.env.yaml", data: [image_shas: [unit_test: "abcd1234"], is_ephemeral: true]])
  }

  def "Hyphens (-) in git repo name are translated to underscores (_)" () {
    setup:
      def app_env = [short_name: 'env', long_name: 'Environment']
      Ephemeral.getBinding().setVariable("config", [:])
      // env.REPO_NAME and env.GIT_SHA set above in setup()
      Ephemeral.getBinding().setVariable("pipelineConfig", [github_credential: null])
    when:
      Ephemeral(app_env, {})
    then:
      1 * getPipelineMock("echo")({ it =~ /(.+)(image_shas.unit_test)(.+)/})
  }

  /*********************
   prep_project() tests
  *********************/

  def "A random 10-character string of lowercase letters is created for the project name" () {
    setup:
      def app_env = [short_name: 'env', long_name: 'Environment']
      Ephemeral.getBinding().setVariable("config", [:])
      Ephemeral.getBinding().setVariable("pipelineConfig", [github_credential: null])
    when:
      Ephemeral(app_env, {})
    then:
      1 * getPipelineMock("echo")({ it =~ /Ephemeral Environment Name: [a-z]{10}/})
  }

  def "The project is given a human-readable display name" () {
    setup:
      def app_env = [short_name: 'env', long_name: 'Environment']
      Ephemeral.getBinding().setVariable("config", [:])
      Ephemeral.getBinding().setVariable("pipelineConfig", [github_credential: null])
      //certain variables set in setup()
    when:
      Ephemeral(app_env, {})
    then:
      1 * getPipelineMock("echo")({ it == "Project Display Name: unit-test: PR-test, Build: 7"})
  }

  def "A new Openshift project is created with the correct name and display name" () {
    setup:
      def app_env = [short_name: 'env', long_name: 'Environment']
      Ephemeral.getBinding().setVariable("config", [:])
      Ephemeral.getBinding().setVariable("pipelineConfig", [github_credential: null])
    when:
      Ephemeral(app_env, {})
    then:
      1 * getPipelineMock("sh")({ it =~ /oc new-project [a-z]{10} --display-name=\'unit-test: PR-test, Build: 7\'/})
  }

  //This step requires the OpenShift user to have the cluster-admin cluster role
  //Ephemeral.groovy should be modified to no longer require that level of administrative privilege
  def "The tiller server can deploy to the new project" () {
    setup:
      def app_env = [short_name: 'env', long_name: 'Environment', tiller_namespace: "tiller_test"]
      Ephemeral.getBinding().setVariable("config", [:])
      Ephemeral.getBinding().setVariable("pipelineConfig", [github_credential: null])
    when:
      Ephemeral(app_env, {})
    then:
      1 * getPipelineMock("sh")({ it =~ /oc process -p TILLER_NAMESPACE=tiller_test -p PROJECT=[a-z]{10} tiller-project-role -n openshift | oc apply -f -/})
  }

  def "The new project can use images from the image_repo_project" () {
    setup:
      def app_env = [short_name: 'env', long_name: 'Environment', tiller_namespace: "tiller_test"]
      Ephemeral.getBinding().setVariable("config", [image_repository_project: "image-proj"])
      Ephemeral.getBinding().setVariable("pipelineConfig", [github_credential: null])
    when:
      Ephemeral(app_env, {})
    then:
      1 * getPipelineMock("sh")({ it =~ /oc adm policy add-role-to-user system:image-puller system:serviceaccount:[a-z]{10}:default -n image-proj/})
  }

  def "If an error is thrown during the setup process, delete the new project" () {
    setup:
      def app_env = [short_name: 'env', long_name: 'Environment']
      Ephemeral.getBinding().setVariable("config", [:])
      Ephemeral.getBinding().setVariable("pipelineConfig", [github_credential: null])
    when:
      try{
        Ephemeral(app_env, {})
      } catch ( Exception e ){}
    then:
      1 * getPipelineMock("sh")({ it =~ /oc new-project [a-z]{10}.*/}) >> {throw Exception}
      (1.._) * getPipelineMock("sh")({ it =~ /oc delete project [a-z]{10}/ })
  }

  def "prep_project() returns the random string it generated" () {
    setup:
      def app_env = [short_name: 'env', long_name: 'Environment']
      Ephemeral.getBinding().setVariable("config", [:])
      Ephemeral.getBinding().setVariable("pipelineConfig", [github_credential: null])
      def retval
      def random_name
    when:
      retval = Ephemeral.prep_project("image_repo_project")
    then:
      1 * getPipelineMock("echo")({ it =~ /Ephemeral Environment Name: [a-z]{10}/ }) >> { _arguments ->
        random_name = _arguments[0]
      }
    expect:
      "Ephemeral Environment Name: ${retval}" == random_name
  }

  /*******************
   do_release() tests
  *******************/

  def "Use Helm to deploy to the new Openshift project" () {
    setup:
      def app_env = [short_name: 'env', long_name: 'Environment']
      Ephemeral.getBinding().setVariable("config", [:])
      Ephemeral.getBinding().setVariable("pipelineConfig", [github_credential: null])
    when:
      Ephemeral(app_env, {})
    then:
      1 * getPipelineMock("sh")({it instanceof Map ? it["script"] =~ /helm install \. -n [a-z]{10} -f values\.env\.yaml --wait/ : false}) >> "ENV:\nA:Alpha\nB:Bravo\nC:Charlie"
  }

  def "do_release() returns the list of variables relevant to the new release/environment" () {
    setup:
      def app_env = [short_name: 'env', long_name: 'Environment']
      Ephemeral.getBinding().setVariable("config", [:])
      Ephemeral.getBinding().setVariable("pipelineConfig", [github_credential: null])
    when:
      def retval = Ephemeral.do_release("abcdefghij", "values.env.yaml")
    then:
      retval?.A == "Alpha"
      retval?.B == "Bravo"
      retval?.C == "Charlie"

  }

  /*****************
   oc_login() tests
  *****************/

  def "The step logs into the specified Openshift cluster with the provided token" () {
    setup:
      def app_env = [short_name: 'env', long_name: 'Environment', openshift_url: "specified_url"]
      Ephemeral.getBinding().setVariable("config", [:])
      Ephemeral.getBinding().setVariable("token", "provided_token")
      Ephemeral.getBinding().setVariable("pipelineConfig", [github_credential: null])
    when:
      Ephemeral(app_env, {})
    then:
      (1.._) * getPipelineMock("sh")( {it =~ /oc login .* specified_url --token=provided_token.*/} )
  }

  def "If the credential is not for a token, check if it's a username/password" () {
    setup:
      def app_env = [short_name: 'env', long_name: 'Environment', openshift_url: "specified_url"]
      Ephemeral.getBinding().setVariable("config", [:])
      Ephemeral.getBinding().setVariable("user", "user")
      Ephemeral.getBinding().setVariable("pipelineConfig", [github_credential: null])
    when:
      Ephemeral(app_env, {})
    then:
      (1.._) * getPipelineMock("sh")( {it =~ /oc login .* specified_url --token=token.*/} ) >> {throw DummyException("Bad Token")}
      (1.._) * getPipelineMock("sh")( {it =~ /oc login .* specified_url -u user -p token.*/} )
  }

  /****************
   cleanup() tests
  ****************/

  def "The ephemeral environment's release is purged from the tiller server" () {
    setup:
      def app_env = [short_name: 'env', long_name: 'Environment']
      Ephemeral.getBinding().setVariable("config", [:])
      Ephemeral.getBinding().setVariable("pipelineConfig", [github_credential: null])
    when:
      Ephemeral(app_env, {})
    then:
      1 * getPipelineMock("sh")({it =~ /helm del --purge [a-z]{10}.*/})
  }

  def "The ephemeral environment's project is removed from OpenShift" () {
    setup:
      def app_env = [short_name: 'env', long_name: 'Environment']
      Ephemeral.getBinding().setVariable("config", [:])
      Ephemeral.getBinding().setVariable("pipelineConfig", [github_credential: null])
    when:
      Ephemeral(app_env, {})
    then:
      1 * getPipelineMock("sh")({it =~ /oc delete project [a-z]{10}.*/})
  }



}
