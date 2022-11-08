/*
  Copyright © 2018 Booz Allen Hamilton. All Rights Reserved.
  This software package is licensed under the Booz Allen Public License. The license can be found in the License file or at http://boozallen.github.io/licenses/bapl
*/

package libraries.grype
import JTEPipelineSpecification


public class ContainerImageScanSpec extends JTEPipelineSpecification {

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
            explicitlyMockPipelineStep('resource')
            getPipelineMock("sh")([script: "/bin/bash ./transform-results.sh image1_repo-grype-scan-results.json /testPath/grype.yaml", returnStdout:true]) >> "test.txt "
            getPipelineMock("sh")([script: "/bin/bash ./transform-results.sh image2_repo-grype-scan-results.json /testPath/grype.yaml", returnStdout: true ]) >> "test.txt "
            getPipelineMock("sh")([script: "/bin/bash ./transform-results.sh qwerty-grype-scan-results.json /testPath/grype.yaml", returnStdout: true ]) >> "test.txt "
        when:
            ContainerImageScan()
        then:
            1 * getPipelineMock("echo")("Grype file explicitly specified in pipeline_config.groovy")
            (1.._) * getPipelineMock("sh")({it =~ /^grype .* --config \/testPath\/grype.yaml >> .*/})
    }

    def "Grype config is found at current dir .grype.yaml" () {
        given:
            explicitlyMockPipelineStep('resource')
            getPipelineMock("sh")([script: "/bin/bash ./transform-results.sh image1_repo-grype-scan-results.json .grype.yaml", returnStdout:true]) >> "test.txt "
            getPipelineMock("sh")([script: "/bin/bash ./transform-results.sh image2_repo-grype-scan-results.json .grype.yaml", returnStdout: true ]) >> "test.txt "
            getPipelineMock("sh")([script: "/bin/bash ./transform-results.sh qwerty-grype-scan-results.json .grype.yaml", returnStdout: true ]) >> "test.txt "
        when:
            ContainerImageScan()
        then:
            1 * getPipelineMock("fileExists")(".grype.yaml") >> true
            1 * getPipelineMock("echo")("Found .grype.yaml")
        then:
            (1.._) * getPipelineMock("sh")({it =~ /^grype .* --config .grype.yaml >> .*/})
    }

    def "Grype config is found at .grype/config.yaml" () {
        given:
            explicitlyMockPipelineStep('resource')
            getPipelineMock("sh")([script: "/bin/bash ./transform-results.sh image1_repo-grype-scan-results.json .grype/config.yaml", returnStdout:true]) >> "test.txt "
            getPipelineMock("sh")([script: "/bin/bash ./transform-results.sh image2_repo-grype-scan-results.json .grype/config.yaml", returnStdout: true ]) >> "test.txt "
            getPipelineMock("sh")([script: "/bin/bash ./transform-results.sh qwerty-grype-scan-results.json .grype/config.yaml", returnStdout: true ]) >> "test.txt "
        when:
            ContainerImageScan()
        then:
            1 * getPipelineMock("fileExists")(".grype/config.yaml") >> true
            1 * getPipelineMock("echo")("Found .grype/config.yaml")
        then:
            (1.._) * getPipelineMock("sh")({it =~ /^grype .* --config .grype\/config.yaml >> .*/})
    }

    def "Grype config is found at user Home path/.grype.yaml" () {
        given:
            explicitlyMockPipelineStep('resource')
            getPipelineMock("sh")([script: "/bin/bash ./transform-results.sh image1_repo-grype-scan-results.json /home/.grype.yaml", returnStdout:true]) >> "test.txt "
            getPipelineMock("sh")([script: "/bin/bash ./transform-results.sh image2_repo-grype-scan-results.json /home/.grype.yaml", returnStdout: true ]) >> "test.txt "
            getPipelineMock("sh")([script: "/bin/bash ./transform-results.sh qwerty-grype-scan-results.json /home/.grype.yaml", returnStdout: true ]) >> "test.txt "
        when:
            ContainerImageScan()
        then:
            1 * getPipelineMock("fileExists")("/home/.grype.yaml") >> true
            1 * getPipelineMock("echo")("Found ~/.grype.yaml")
        then:
            (1.._) * getPipelineMock("sh")({it =~ /^grype .* --config \/home\/.grype.yaml >> .*/})
    }

    def "Grype config found at <XDG_CONFIG_HOME>/grype/config.yaml" () {
        given:
            explicitlyMockPipelineStep('resource')
            getPipelineMock("sh")([script: "/bin/bash ./transform-results.sh image1_repo-grype-scan-results.json /xdg/grype/config.yaml", returnStdout:true]) >> "test.txt "
            getPipelineMock("sh")([script: "/bin/bash ./transform-results.sh image2_repo-grype-scan-results.json /xdg/grype/config.yaml", returnStdout: true ]) >> "test.txt "
            getPipelineMock("sh")([script: "/bin/bash ./transform-results.sh qwerty-grype-scan-results.json /xdg/grype/config.yaml", returnStdout: true ]) >> "test.txt "
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
            1 * getPipelineMock("sh")("grype test_registry/image1_repo:4321dcba -o json --fail-on high  >> image1_repo-grype-scan-results.json")
            1 * getPipelineMock("sh")("grype test_registry/image2_repo:4321dcbb -o json --fail-on high  >> image2_repo-grype-scan-results.json")
            1 * getPipelineMock("sh")("grype test_registry/image3_repo/qwerty:4321dcbc -o json --fail-on high  >> qwerty-grype-scan-results.json")
    }

    def "Test json format and negligible severity" () {
        given:
            ContainerImageScan.getBinding().setVariable("config", [report_format: "json", fail_on_severity: "negligible"])
        when:
            ContainerImageScan()
        then:
            (1.._) * getPipelineMock("sh")({it =~ /^grype .* -o json --fail-on negligible  >> .*/})
    }

    def "Test table format and low severity" () {
        given:
            ContainerImageScan.getBinding().setVariable("config", [report_format: "table", fail_on_severity: "low"])
        when:
            ContainerImageScan()
        then:
            (1.._ ) * getPipelineMock("sh")({it =~ /^grype .* -o table --fail-on low  >> .*/})
    }

    def "Test cyclonedx format and medium severity" () {
        given:
            ContainerImageScan.getBinding().setVariable("config", [report_format: "cyclonedx", fail_on_severity: "medium"])
        when:
            ContainerImageScan()
        then:
            (1.._) * getPipelineMock("sh")({it =~ /^grype .* -o cyclonedx --fail-on medium  >> .*/})
    }

    def "Test table format and high severity" () {
        given:
            ContainerImageScan.getBinding().setVariable("config", [report_format: "table", fail_on_severity: "high"])
        when:
            ContainerImageScan()
        then:
            (1.._) * getPipelineMock("sh")({it =~ /^grype .* -o table --fail-on high  >> .*/})
    }

    def "Test cyclonedx format and critical severity" () {
        given:
            ContainerImageScan.getBinding().setVariable("config", [report_format: "cyclonedx", fail_on_severity: "critical"])
        when:
            ContainerImageScan()
        then:
            (1.._) * getPipelineMock("sh")({it =~ /^grype .* -o cyclonedx --fail-on critical  >> .*/})
    }

    def "Test Archive artifacts works as expected for json format and not null grype config" () {
        given: 
            ContainerImageScan.getBinding().setVariable("config", [report_format: "json", grype_config: ".grype.yaml"])
            explicitlyMockPipelineStep("resource")
            getPipelineMock("sh")([script:"/bin/bash ./transform-results.sh image1_repo-grype-scan-results.json .grype.yaml", returnStdout:true]) >> "test.txt "
            getPipelineMock("sh")([script: "/bin/bash ./transform-results.sh image2_repo-grype-scan-results.json .grype.yaml", returnStdout: true ]) >> "test.txt "
            getPipelineMock("sh")([script: "/bin/bash ./transform-results.sh qwerty-grype-scan-results.json .grype.yaml", returnStdout: true ]) >> "test.txt "
        when:
            ContainerImageScan()
        then:
            1 * getPipelineMock("archiveArtifacts.call")([artifacts: "image1_repo-grype-scan-results.json, image1_repo-grype-scan-results.txt", allowEmptyArchive: true ])
            1 * getPipelineMock("archiveArtifacts.call")([artifacts:"image2_repo-grype-scan-results.json, image2_repo-grype-scan-results.txt", allowEmptyArchive:true])
            1 * getPipelineMock("archiveArtifacts.call")([artifacts:"qwerty-grype-scan-results.json, qwerty-grype-scan-results.txt", allowEmptyArchive:true])

    }

    def "Test that error handling works as expected" () {
        given:
            explicitlyMockPipelineStep("Exception")//("Failed: java.lang.Exception: test")
            getPipelineMock("sh")("grype test_registry/image1_repo:4321dcba -o json --fail-on high  >> image1_repo-grype-scan-results.json") >> {throw new Exception("test")}
        when:
            ContainerImageScan()
        then:
            1 * getPipelineMock("echo")("Failed: java.lang.Exception: test")
            1 * getPipelineMock("echo")("Grype Quality Gate Failed. There are one or more CVE's that exceed the maximum allowed severity rating!")
            1 * getPipelineMock("stash")("workspace")
            1 * getPipelineMock("error")(_)          
    }
/*
repo: "image1_repo", context: "image1", tag: "4321dcba"]
            images << [registry: "test_registry", repo: "image2_repo", context: "image2", tag: "4321dcbb"]
            images << [registry: "test_registry", repo: "image3_repo/qwerty", context: "image3", tag: "4321dcbc"]
*/

    def "Test scanning syft JSON SBOM artifact" () {
        given:
            ContainerImageScan.getBinding().setVariable("config", [scan_sbom: true])
            getPipelineMock("findFiles")([glob:'image1_repo-4321dcba-*-json.json', excludes:'image1_repo-4321dcba-*-*dx-json.json']) >> ['image1_repo-4321dcba-test-json.json']
            getPipelineMock("findFiles")([glob:'image2_repo-4321dcbb-*-json.json', excludes:'image2_repo-4321dcbb-*-*dx-json.json']) >> ['image2_repo-4321dcbb-test-json.json']
            getPipelineMock("findFiles")([glob:'image3_repo-qwerty-4321dcbc-*-json.json', excludes:'image3_repo-qwerty-4321dcbc-*-*dx-json.json']) >> ['image3_repo-qwerty-4321dcbc-json.json']
            explicitlyMockPipelineVariable("syftSbom")
                
        when:
            ContainerImageScan()            

        then:
            (1.._) * getPipelineMock("sh")({it =~ /^grype sbom:.*/})
    }

        def "Test scanning syft Cyclonedx SBOM artifact" () {
        given:
            ContainerImageScan.getBinding().setVariable("config", [scan_sbom: true])
            getPipelineMock("findFiles")([glob:'image1_repo-4321dcba-*-json.json', excludes:'image1_repo-4321dcba-*-*dx-json.json']) >> []
            getPipelineMock("findFiles")([glob:'image2_repo-4321dcbb-*-json.json', excludes:'image2_repo-4321dcbb-*-*dx-json.json']) >> []
            getPipelineMock("findFiles")([glob:'image3_repo-qwerty-4321dcbc-*-json.json', excludes:'image3_repo-qwerty-4321dcbc-*-*dx-json.json']) >> []
            getPipelineMock("findFiles")([glob:'image1_repo-4321dcba-*-cyclonedx*']) >> ['image1_repo-4321dcba-test-cyclonedx-xml.xml']
            getPipelineMock("findFiles")([glob:'image2_repo-4321dcbb-*-cyclonedx*']) >> ['image2_repo-4321dcbb-test-cyclonedx-json.json']
            getPipelineMock("findFiles")([glob:'image3_repo-qwerty-4321dcbc-*-cyclonedx*']) >> ['image3_repo-qwerty-4321dcbc-cyclonedx-json.json']
            explicitlyMockPipelineVariable("syftSbom")
                
        when:
            ContainerImageScan()            

        then:
            (1.._) * getPipelineMock("sh")({it =~ /^grype sbom:.*/})
    }

    def "Test scanning syft Cyclonedx SBOM artifact" () {
        given:
            ContainerImageScan.getBinding().setVariable("config", [scan_sbom: true])
            getPipelineMock("findFiles")([glob:'image1_repo-4321dcba-*-json.json', excludes:'image1_repo-4321dcba-*-*dx-json.json']) >> []
            getPipelineMock("findFiles")([glob:'image2_repo-4321dcbb-*-json.json', excludes:'image2_repo-4321dcbb-*-*dx-json.json']) >> []
            getPipelineMock("findFiles")([glob:'image3_repo-qwerty-4321dcbc-*-json.json', excludes:'image3_repo-qwerty-4321dcbc-*-*dx-json.json']) >> []
            getPipelineMock("findFiles")([glob:'image1_repo-4321dcba-*-cyclonedx*']) >> []
            getPipelineMock("findFiles")([glob:'image2_repo-4321dcbb-*-cyclonedx*']) >> []
            getPipelineMock("findFiles")([glob:'image3_repo-qwerty-4321dcbc-*-cyclonedx*']) >> []
            getPipelineMock("findFiles")([glob:'image1_repo-4321dcba-*-spdx*']) >> ['image1_repo-4321dcba-test-spdx-json.json']
            getPipelineMock("findFiles")([glob:'image2_repo-4321dcbb-*-spdx*']) >> ['image2_repo-4321dcbb-test-spdx-tag-value.txt']
            getPipelineMock("findFiles")([glob:'image3_repo-qwerty-4321dcbc-*-spdx*']) >> ['image3_repo-qwerty-4321dcbc-spdx-json.json']
            explicitlyMockPipelineVariable("syftSbom")
                
        when:
            ContainerImageScan()            

        then:
            (1.._) * getPipelineMock("sh")({it =~ /^grype sbom:.*/})
    }
}
