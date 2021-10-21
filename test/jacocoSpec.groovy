/*
  Copyright © 2018 Booz Allen Hamilton. All Rights Reserved.
  This software package is licensed under the Booz Allen Public License. The license can be found in the License file or at http://boozallen.github.io/licenses/bapl
*/

package jacoco

import groovy.io.FileType

public class JacocoSpec extends JTEPipelineSpecification {

    def "JaCoCo"(){
        when:
        new File("libraries").eachFileRecurse (FileType.FILES) { file ->
            String filePath = file.getPath()
            if(filePath.endsWith(".groovy") && !filePath.endsWith("library_config.groovy")){
                String path = filePath - "libraries/"
                loadPipelineScriptForTest(path)
            }
        }
        then: 
        assert true 
    }

}