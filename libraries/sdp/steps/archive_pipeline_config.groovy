/*
  Copyright © 2018 Booz Allen Hamilton. All Rights Reserved.
  This software package is licensed under the Booz Allen Public License. The license can be found in the License file or at http://boozallen.github.io/licenses/bapl
*/

package libraries.sdp

import org.boozallen.plugins.jte.config.TemplateConfigObject
import org.boozallen.plugins.jte.config.TemplateConfigDsl

@Init
void call(context){
    TemplateConfigObject aggregated = new TemplateConfigObject(
        config: pipelineConfig, // variable provided in binding by JTE
        merge: [],
        override: []
    )
    node{
        writeFile text: TemplateConfigDsl.serialize(aggregated), file: "pipeline_config.groovy"
        archiveArtifacts "pipeline_config.groovy"
    }
}