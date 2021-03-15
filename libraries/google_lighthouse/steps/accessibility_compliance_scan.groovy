/*
  Copyright © 2018-present Booz Allen Hamilton. All Rights Reserved.
  This software package is licensed under the Booz Allen Public License. The license can be found in the License file or at http://boozallen.github.io/licenses/bapl
*/

package libraries.google_lighthouse.steps

void call(){
    stage("Accessibility Compliance: Google Lighthouse"){
        inside_sdp_image "google-lighthouse", {
            String url = config.url 
            String resultsDir = "google-lighthouse"
            sh """
            mkdir -p ${resultsDir}; 
            lighthouse ${url} \
            --chrome-flags='--headless --no-sandbox' \
            --output json --output html \
            --output-path=${resultsDir}/lighthouse
            """ 
            archiveArtifacts allowEmptyArchive: true, artifacts: "${resultsDir}/"
            this.validateResults(resultsDir)
        }
    }
}

/*
    validate results via generated JSON file

    libraries{
        google_lighthouse{
            thresholds{
                performance{
                    fail = 49
                    warn = 89
                }
                accessibility {
                    fail = 49
                    warn = 89
                }
                best_practices{
                    fail = 49
                    warn = 89
                }
                search_engine_optimization{
                    fail = 49
                    warn = 89 
                }
            }
        }
    } 
*/
void validateResults(String resultsDir){
    if(!fileExists("${resultsDir}/lighthouse.report.json")){
        return
    }

    def results = readJSON file: "${resultsDir}/lighthouse.report.json"

    boolean shouldFail = false 
    boolean shouldWarn = false 
    ArrayList output = [ """
    ------------------------
    Google Lighthouse Scores
    ------------------------""".stripIndent()]
    
    [
        [
            configKey: "performance",
            jsonKey: "performance",
            title: "Performance" 
        ],
        [
            configKey: "accessibility",
            jsonKey: "accessibility",
            title: "Accessibility"
        ],
        [
            configKey: "best_practices",
            jsonKey: "best-practices",
            title: "Best Practices" 
        ],
        [
            configKey: "search_engine_optimization",
            jsonKey: "seo",
            title: "Search Engine Optimization"
        ]
    ].each{ category -> 
        def failThreshold = config.thresholds?."${category.configKey}"?.fail
        if(!(failThreshold instanceof Number)){
            failThreshold = 49
        }

        def warnThreshold = config.thresholds?."${category.configKey}"?.warn
        if(!(warnThreshold instanceof Number)){
            warnThreshold = 89
        }
        
        def score = results.categories[category.jsonKey]?.score * 100 

        if( score <= failThreshold ){
            shouldFail = true 
            output << "${category.title}: ${score} <-- failing"
        } else if ( score <= warnThreshold ){
            shouldWarn = true 
            output << "${category.title}: ${score} <-- warning"
        } else {
            output << "${category.title}: ${score}"
        }
    }

    println output.join("\n")
    String oopsMessage = "Google Lighthouse results did not meet thresholds"
    if(shouldFail) error(oopsMessage)
    if(shouldWarn) unstable(oopsMessage)

}
