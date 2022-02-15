package libraries.grype

import JTEPipelineSpecification


class GrypeTestSpec extends JTEPipelineSpecification {

    def GrypeTest = null

    static class DummyException extends RuntimeException {
        DummyException(String _message) { super( _message ) }
}

def setup() {
  GrypeTest = loadPipelineScriptForTest("grype/steps/grype_scan.groovy")
  explicitlyMockPipelineVariable("get_images_to_build")

  getPipelineMock("get_images_to_build.call")() >> {
      def images = []
      images << [registry: "reg1", repo: "repo1", context: "context1", tag: "tag1"]
      images << [registry: "reg2", repo: "repo2", context: "context2", tag: "tag2"]
      return images
    }
  GrypeTest.getBinding().setVariable("registry-creds", ["user", "pass"])
}

def "If grype.yaml does not exist, throw error" () {
  when:
    try {
      GrypeTest()
    } catch(DummyException e) {}
  then:
    1 * getPipelineMock("error")("no grype config found") >> {throw new DummyException("config file error")}
}

def "Images are saved from get_images_to_build with docker" () {  
  setup: 
    
  when:
    GrypeTest()
  then:
  1 * getPipelineMock("sh")("docker save reg1/repo1:tag1 > tag1.tar ")  
//getPipelineMock("sh")("grype ${img.tag}.tar -o json >> ${RAW_RESULTS_FILE}")
}


/*def "Image is scanned with Grype" () {
}
*/

/*def "JSON results are transformed to a human readable form" () {
}
*/

/*def "Grype results are archived" () {
}
*/

}