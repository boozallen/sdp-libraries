/*
  Copyright © 2018 Booz Allen Hamilton. All Rights Reserved.
  This software package is licensed under the Booz Allen Public License. The license can be found in the License file or at http://boozallen.github.io/licenses/bapl
*/
void call(){
    stage("Webhint: Lint") {
      def url = config.url ?: {
        error """
        Webhint.io Library needs the target url.
        libraries{
          webhint{
            url = "https://example.com"
          }
        }
        """
      } ()
      
      inside_sdp_image "webhint:1.9", {
        String resultsDir = "hint-report"
        String resultsText = "hint.results.log"
        
        def hintrc = [
          extends: config.extender ?: [ "accessibility" ],
          formatters: [ "html", "summary" ]
        ]
        
        sh "mkdir -p ${resultsDir}"
        writeJSON file: "${resultsDir}/.hintrc", json: hintrc
        sh "cp ${resultsDir}/.hintrc .; cat .hintrc;"
        sh script: "hint ${url} > ${resultsDir}/${resultsText}", returnStatus: true
        
        //def lines=new File("${resultsDir}/${resultsText}").readLines()
        //def lines = readFile 'hint-report/hint.report.log'
        //def lastline=lines.get(lines.size()-1)
        //File file = new File("/${resultsText}")
        //def lines = file.readLines()
        //sh "echo ${lastline}"
        archiveArtifacts allowEmptyArchive: true, artifacts: "${resultsDir}/"
        this.validateResults("${resultsDir}/${resultsText}")
        //File file = new File("${resultsDir}/${resultsText}")
        //def lines = file.readLines()
      }
    }
}

void validateResults(String resultsFile) {
    if(!fileExists(resultsFile)) {
        return
    }
  
    def file = readFile file: "${resultsFile}"
    def lines = file.readLines()
    def lastline=lines.get(lines.size()-1)
    def total = 0
  
    for (String item : lastline.split(' ')) {
      if (item.isNumber()) total += item.toInteger()
    }
  
    unstable(total.toString())

    //boolean shouldFail = results.size() >= config.failThreshold
    //boolean shouldWarn = results.size() < config.failThreshold
    
    //if(shouldFail) error("Webhint.io suggestions exceeded the fail threshold")
    //if(shouldWarn) unstable("Webhint.io suggested some changes")
}
