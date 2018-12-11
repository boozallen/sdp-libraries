/*
  Copyright © 2018 Booz Allen Hamilton. All Rights Reserved.
  This software package is licensed under the Booz Allen Public License. The license can be found in the License file or at http://boozallen.github.io/licenses/bapl
*/

import com.homeaway.devtools.jenkins.testing.JenkinsPipelineSpecification

public class PenetrationTestSpec extends JenkinsPipelineSpecification {

  def PenetrationTest = null

  def setup() {
    PenetrationTest = loadPipelineScriptForTest("owasp_zap/penetration_test.groovy")
    explicitlyMockPipelineStep("inside_sdp_image")
  }

  def "Scan Fails Without Target URL" () {
    setup:
      PenetrationTest.getBinding().setVariable("config", [ target: null ])
    when:
      PenetrationTest()
    then:
      1 * getPipelineMock("error")(_)
  }

  def "If env.FRONTEND_URL is null, config.target is Used" () {
    setup:
      PenetrationTest.getBinding().setVariable("config", [target: "Kirk"])
    when:
      PenetrationTest()
    then:
      1 * getPipelineMock("env.getProperty").call('FRONTEND_URL') >> null
      1 * getPipelineMock("sh")({it =~ / (zap-cli open-url) Kirk (.+)/})
  }

  def "Target is set to env.FRONTEND_URL first" () {
    setup:
      PenetrationTest.getBinding().setVariable("config", [target: "Kirk"])
    when:
      PenetrationTest()
    then:
      1 * getPipelineMock("env.getProperty").call('FRONTEND_URL') >> "Bones"
      1 * getPipelineMock("sh")({it =~ / (zap-cli open-url) Bones (.+)/})
  }

  def "Default Vulnerability Treshold is High" () {
    setup:
      PenetrationTest.getBinding().setVariable("config", [ target: "https://example.com", vulnerability_threshold: null ])
    when:
      PenetrationTest()
    then:
      1 * getPipelineMock("sh")([script: 'zap-cli alerts -l High', returnStatus: true])
  }

  def "Error Thrown if vulnerability_threshold is invalid" () {
    setup:
      PenetrationTest.getBinding().setVariable("config", [target: "https://example.com", vulnerability_threshold: x])
    when:
      PenetrationTest()
    then:
      y * getPipelineMock("error")("OWASP Zap: Vulnerability Threshold ${x} not Ignore, Low, Medium, High, or Informational")
    where:
      x               | y
      "Ignore"        | 0
      "Low"           | 0
      "Medium"        | 0
      "Tiberius"      | 1
      "High"          | 0
      "Critical"      | 1
      "Informational" | 0
  }

  def "ZAP Scan is Run" () {
    setup:
      PenetrationTest.getBinding().setVariable("config", [ target: "https://example.com"])
    when:
      PenetrationTest()
    then:
      1 * getPipelineMock("inside_sdp_image")(_) >> { _arguments ->
        assert "zap" == _arguments[0][0]
      }
      1 * getPipelineMock("sh")({it =~ /zap.sh .+/}) >> { _arguments ->
        def tokens = _arguments[0].split(/\s\s+/) //double space
        assert tokens[0] == "zap.sh -daemon"
        assert tokens[1] == "-host 127.0.0.1"
        assert tokens[2] == "-port 8080"
        assert tokens[3] == "-config api.disablekey=true"
        assert tokens[4] == "-config scanner.attackOnStart=true"
        assert tokens[5] == "-config view.mode=attack"
        assert tokens[6] == "-config connection.dnsTtlSuccessfulQueries=-1"
        assert tokens[7] == "-config api.addrs.addr.name=.*"
        assert tokens[8] == "-config api.addrs.addr.regex=true &"
        assert tokens.length == 9
      }
      1 * getPipelineMock("sh")({it =~ / zap-cli .+/}) >> { _arguments ->
        def tokens = _arguments[0].split(/\s\s+/)
        assert tokens[0] == " zap-cli open-url https://example.com &&"
        assert tokens[1] == "zap-cli spider https://example.com &&"
        assert tokens[2] == "zap-cli active-scan -r https://example.com &&"
        assert tokens[3] == "zap-cli report -o zap.html -f html"
        assert tokens.length == 4
      }
  }

  def "Scan Results are Archived" () {
    setup:
      PenetrationTest.getBinding().setVariable("config", [ target: "https://example.com"])
    when:
      PenetrationTest()
    then:
      1 * getPipelineMock("archive")("zap.html")
  }

  def "Vulnerabilities Over Threshold Throw Error" () {
    setup:
      PenetrationTest.getBinding().setVariable("config", [ target: "https://example.com", vulnerability_threshold: "Low"])
    when:
      PenetrationTest()
    then:
      1 * getPipelineMock("sh")([script: "zap-cli alerts -l Low", returnStatus: true]) >> 3
      1 * getPipelineMock("error")("OWASP Zap found 3 Low vulnerabilities while performing a scan")
  }

  def "Test Passes If No Vulnerabilities Over Threshold Are Found" () {
    setup:
      PenetrationTest.getBinding().setVariable("config", [ target: "https://example.com"])
    when:
      PenetrationTest()
    then:
      1 * getPipelineMock("sh")([script: "zap-cli alerts -l High", returnStatus: true]) >> 0
      0 * getPipelineMock("error")(_)
  }

  def "If vulnerability_threshold == Ignore then Vulnerabilities Are Not Counted" () {
    setup:
      PenetrationTest.getBinding().setVariable("config", [ target: "https://example.com", vulnerability_threshold: "Ignore"])
    when:
      PenetrationTest()
    then:
      0 * getPipelineMock("sh")([script: "zap-cli alerts -l Ignore", returnStatus: true])
  }


}
