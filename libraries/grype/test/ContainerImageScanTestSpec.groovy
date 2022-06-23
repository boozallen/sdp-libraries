/*
  Copyright © 2018 Booz Allen Hamilton. All Rights Reserved.
  This software package is licensed under the Booz Allen Public License. The license can be found in the License file or at http://boozallen.github.io/licenses/bapl
*/

package libraries.grype
import JTEPipelineSpecification


public class ContainerImageScanTestSpec extends JTEPipelineSpecification {

    def ContainerImageScan = null

    def setup() {
        ContainerImageScan = loadPipelineScriptForStep("grype", "container_image_scan")
        ContainerImageScan.getBinding().setVariable("config", [:])
        String grypeConfig = ""
        explicitlyMockPipelineStep("inside_sdp_image")
        explicitlyMockPipelineStep("login_to_registry")
        explicitlyMockPipelineStep("get_images_to_build")
        getPipelineMock("sh")([script: 'echo $HOME', returnStdout: true]) >> "/home"
        getPipelineMock("sh")([script: 'echo $XDG_CONFIG_HOME', returnStdout: true]) >> "/xdg"

        getPipelineMock("get_images_to_build")() >> {
            def images = []
            images << [registry: "test_registry", repo: "image1_repo", context: "image1", tag: "4321dcba"]
            images << [registry: "test_registry", repo: "image2_repo", context: "image2", tag: "4321dcbb"]
            images << [registry: "test_registry", repo: "image3_repo/qwerty", context: "image3", tag: "4321dcbc"]
            return images
        }
    
    }

    def "Unstash workspace before scanning images" () {

        when:
            ContainerImageScan()
        then:
            1 * getPipelineMock("unstash")("workspace")
        then:
            (1.._) * getPipelineMock("sh")({it =~ /^grype */})
    }

    def "Login to registry to scan images" () {
        when:
            ContainerImageScan()
        then:
            1 * getPipelineMock("login_to_registry")(_)
        then:
            (1.._) * getPipelineMock("sh")({it =~ /^grype */})
    }

    def "Grype config is given in pipeline_config.groovy" () {
        given:
            ContainerImageScan.getBinding().setVariable("config", [grype_config: "/testPath/grype.yaml"])
        when:
            ContainerImageScan()
        then:
            1 * getPipelineMock("echo")("Grype file explicitly specified in pipeline_config.groovy")
            (1.._) * getPipelineMock("sh")({it =~ /^grype .* --config \/testPath\/grype.yaml >> .*/})
    }

    def "Grype config is found at current dir .grype.yaml" () {
        when:
            ContainerImageScan()
        then:
            1 * getPipelineMock("fileExists")(".grype.yaml") >> true
            1 * getPipelineMock("echo")("Found .grype.yaml")
        then:
            (1.._) * getPipelineMock("sh")({it =~ /^grype .* --config .grype.yaml >> .*/})
    }

    def "Grype config is found at .grype/config.yaml" () {
        when:
            ContainerImageScan()
        then:
            1 * getPipelineMock("fileExists")(".grype/config.yaml") >> true
            1 * getPipelineMock("echo")("Found .grype/config.yaml")
        then:
            (1.._) * getPipelineMock("sh")({it =~ /^grype .* --config .grype\/config.yaml >> .*/})
    }

    def "Grype config is found at user Home path/.grype.yaml" () {
        when:
            ContainerImageScan()
        then:
            1 * getPipelineMock("fileExists")("/home/.grype.yaml") >> true
            1 * getPipelineMock("echo")("Found ~/.grype.yaml")
        then:
            (1.._) * getPipelineMock("sh")({it =~ /^grype .* --config \/home\/.grype.yaml >> .*/})
    }

    def "Grype config found at <XDG_CONFIG_HOME>/grype/config.yaml" () {
        when:
            ContainerImageScan()
        then:
            1 * getPipelineMock("fileExists")("/xdg/grype/config.yaml") >> true
            1 * getPipelineMock("echo")("Found <XDG_CONFIG_HOME>/grype/config.yaml")
        then:
            (1.._) * getPipelineMock("sh")({it =~ /^grype .* --config \/xdg\/grype\/config.yaml >> .*/})
    }

    def "Check each image is scanned as expected when no extra config is present" () {
        when:
            ContainerImageScan()
        then:
            1 * getPipelineMock("sh")("grype test_registry/image1_repo:4321dcba  >> image1_repo-grype-scan-results")
            1 * getPipelineMock("sh")("grype test_registry/image2_repo:4321dcbb  >> image2_repo-grype-scan-results")
            1 * getPipelineMock("sh")("grype test_registry/image3_repo/qwerty:4321dcbc  >> qwerty-grype-scan-results")
    }

    // test different inputs json, table, cyclonedx. low, high, critical, negligible, medium. grype no grype
    def "Test different inputs run as expected" () {
        given:
            ContainerImageScan.getBinding().setVariable("config", [report_format: "json", fail_on_severity: "low"])
        when:
            ContainerImageScan()
        then:
            (1.._) * getPipelineMock("sh")({it =~ /^grype .* -o json --fail-on low  >> .*/})
    }
    // test archive artifacts
    // test error handling
    // test stash workplace

    //def "Check images are scanned properly with pipeline_config.groovy vars set" () {
    //    expect:
    //        ContainerImageScan().getBinding().setVariable("config", [report_format: ${a}, fail_on_severity: ${b}, grype_config: ${c}])                       
    //        getPipelineMock("sh")({it =~ /^grype  \${a}\${b}\${c} >> .*/})
    //    where:
    //    //outputFormat|severityThreshold|grypeConfig
    //        a           | b             | c
    //        "json"      | "low"         | ".grype.yaml"
    //        "table"     | "medium"      | "config/.grype.yaml"
    //        "cyclonedx" | "high"        | "grype/config.yaml"
    //        "json"      | "negligible"  | null
    //        "table"     | "critical"    | null
    //}


}


