/*
  Copyright © 2018 Booz Allen Hamilton. All Rights Reserved.
  This software package is licensed under the Booz Allen Public License. The license can be found in the License file or at http://boozallen.github.io/licenses/bapl
*/

import com.homeaway.devtools.jenkins.testing.JenkinsPipelineSpecification

public class ScanContainerImageSpec extends JenkinsPipelineSpecification {

  def ScanContainerImage = null

  public static class DummyException extends RuntimeException {
    public DummyException(String _message) { super( _message ); }
  }

  def setup() {
    ScanContainerImage = loadPipelineScriptForTest("twistlock/scan_container_image.groovy")
    explicitlyMockPipelineVariable("out")
    explicitlyMockPipelineVariable("login_to_registry")
    explicitlyMockPipelineVariable("get_images_to_build")

    getPipelineMock("sh")(_ as Map) >> { _arguments ->
      if (_arguments[0]["script"] =~ /twistcli.+/) {
        return "Results at: foobarbaz"
      } else return "foobar"
    }

    getPipelineMock("get_images_to_build.call")() >> {
      def images = []
      images << [registry: "reg1", repo: "repo1", context: "context1", tag: "tag1"]
      images << [registry: "reg2", repo: "repo2", context: "context2", tag: "tag2"]
      return images
    }

    getPipelineMock("readJSON")([file: "twistlock_results.json"]) >> {
      def results = [:]
      results.images = [[info: [
                    tags: [
                      registry: "reg1",
                      repo: "repo1",
                      tag: "tag1"
                    ],
                    cveVulnerabilityDistribution: [
                      low: "vd-low1",
                      medium: "vd-medium1",
                      high: "vd-high1",
                      critical: "vd-critical1"
                    ],
                    complianceDistribution: [
                      low: "cd-low1",
                      medium: "cd-medium1",
                      high: "cd-high1",
                      critical: "cd-critical1"
                    ]
                  ]],
                  [info: [
                    tags: [
                      registry: "reg2",
                      repo: "repo2",
                      tag: "tag2"
                    ],
                    cveVulnerabilityDistribution: [
                      low: "vd-low2",
                      medium: "vd-medium2",
                      high: "vd-high2",
                      critical: "vd-critical2"
                    ],
                    complianceDistribution: [
                      low: "cd-low2",
                      medium: "cd-medium2",
                      high: "cd-high2",
                      critical: "cd-critical2"
                      ]
                    ]]]
      return results
    }

    ScanContainerImage.getBinding().setVariable("user", "user")
    ScanContainerImage.getBinding().setVariable("pass", "pass")
  }

  def "Pipeline Fails If URL Not Set" () {
    setup:
      ScanContainerImage.getBinding().setVariable("config", [url: null])
    when:
      try {
        ScanContainerImage()
      } catch( DummyException e ) {}
    then:
      1 * getPipelineMock("error")("Twistlock url not defined in library spec") >> {
        throw new DummyException("Dummy test failure")
      }
  }

  def "Pipeline Fails if Credential Name Not Set" () {
    setup:
      ScanContainerImage.getBinding().setVariable("config", [url: "foo", credential: null])
    when:
      try {
        ScanContainerImage()
      } catch( DummyException e ) {}
    then:
      1 * getPipelineMock("error")("Twistlock credential not defined in library spec") >> {
        throw new DummyException("Dummy test failure")
      }
  }

  def "Credentials Are Passed to get_twistcli Method" () {
    setup:
      ScanContainerImage.getBinding().setVariable("config", [url: "foo", credential: "bar"])
    when:
      ScanContainerImage()
    then:
      1 * getPipelineMock("usernamePassword.call")([credentialsId: "bar", passwordVariable: 'pass', usernameVariable: 'user'])
      1 * getPipelineMock("sh")("curl -k -u 'user':'pass' -H 'Content-Type: application/json' -X GET -o /usr/local/bin/twistcli foo/api/v1/util/twistcli")
      // Not sure of a valid way to test that ${user} and ${pass} are the correct values
      // inside the withCredentials block; I'm not sure how those variables are set (in
      // the binding, in a special binding, as locally defined variables, etc.).
  }

  def "Images From images_to_build Output Are Pulled" () {
    setup:
      ScanContainerImage.getBinding().setVariable("config", [url: "foo", credential: "bar"])
    when:
      ScanContainerImage()
    then:
      1 * getPipelineMock("sh")("docker pull reg1/repo1:tag1 ")
      1 * getPipelineMock("sh")("docker pull reg2/repo2:tag2 ")
  }

  def "Images From images_to_build Output Are Added To images List and Scanned" () {
    setup:
      ScanContainerImage.getBinding().setVariable("config", [url: "foo", credential: "bar"])
    when:
      ScanContainerImage()
    then:
      2 * getPipelineMock("sh")(_ as Map) >> { _arguments ->
        if (_arguments[0]["script"] =~ /twistcli.+/) {
          assert _arguments[0]["script"] == "twistcli images scan --details --upload --address foo -u user -p 'pass' reg1/repo1:tag1 reg2/repo2:tag2 "
          return "Results at: foobarbaz"
        } else return "foobar"
      }

  }

  def "Results Are Archived" () {
    setup:
      ScanContainerImage.getBinding().setVariable("config", [url: "foo", credential: "bar"])
    when:
      ScanContainerImage()
    then:
      1 * getPipelineMock("archiveArtifacts.call")("twistlock_results.json")
  }

}
