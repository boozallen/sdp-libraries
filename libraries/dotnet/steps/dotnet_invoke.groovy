/*
  Copyright © 2018 Booz Allen Hamilton. All Rights Reserved.
  This software package is licensed under the Booz Allen Public License. The license can be found in the License file or at http://boozallen.github.io/licenses/bapl
*/

package libraries.dotnet.steps

@StepAlias(["source_build", "unit_test"])
void call() {
    String stepName = ""
    String outDir = ""
    String resultDir = ""

    String sdkImage = config?.sdk_image ?: "dotnet-sdk:latest"

    switch(stepContext.name) {
        case "source_build":
            stepName = "DotNet Build"
            outDir = config?.source_build?.outDir ?: "bin"
            break
        case "unit_test":
            stepName = "DotNet Unit Test"
            resultDir = config?.unit_test?.resultDir ?: "coverage"
            break
        default:
            error("step name must be \"source_build\" or \"unit_test\" got \"${stepContext.name}\"")
    }

    stage(stepName) {
        inside_sdp_image "${sdkImage}", {
            unstash "workspace"

            if (stepName == "DotNet Build") {
                try {
                    sh "dotnet publish -c release -o ${outDir}"
                }
                catch (any) {
                    throw any
                }
                finally {
                    archiveArtifacts artifacts: "${outDir}/*.*, allowEmptyArchive: true"
                }
            }
            else {
                //Execute dotnet tests and output to coverage directory
                try {
                    sh "rm -drf ${resultDir}"
                    sh "dotnet test --collect:'XPlat Code Coverage' --results-directory ${resultDir} --logger trx"
                }
                catch (any) {
                    throw any
                }
                finally {
                    archiveArtifacts artifacts: "${resultDir}/**/*, allowEmptyArchive: true"
                }
            }

            stash "workspace"
        }
    }
}
