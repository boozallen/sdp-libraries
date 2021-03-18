/*
  Copyright © 2018 Booz Allen Hamilton. All Rights Reserved.
  This software package is licensed under the Booz Allen Public License. The license can be found in the License file or at http://boozallen.github.io/licenses/bapl
*/

package libraries.webhint.steps

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

    inside_sdp_image "webhint", {
      String resultsDir = "hint-report"
      String resultsText = "hint.results.log"

      this.createAndAddHintrcFile("${resultsDir}")
      this.processUrl(url, resultsDir, resultsText)
      archiveArtifacts allowEmptyArchive: true, artifacts: "${resultsDir}/"
      this.validateResults("${resultsDir}/${resultsText}")
    }
  }
}

void createAndAddHintrcFile(String path) {
  def hintrc = [
    extends: config.extender ?: [ "accessibility" ],
    formatters: [ "html", "summary" ]
  ]
        
  sh "mkdir -p ${path}"
  writeJSON file: "${path}/.hintrc", json: hintrc
  sh "cp ${path}/.hintrc .;"
}

void processUrl(String url, String resultsDir, String resultsText) {
  sh "cat ${resultsDir}/.hintrc"
  sh script: "hint ${url} > ${resultsDir}/${resultsText}", returnStatus: true
}

void validateResults(String filePath) {
  if(!fileExists(filePath)) {
    return
  }
  
  def file = readFile file: "${filePath}"
  def lines = file.readLines()
  def lastline=lines.get(lines.size()-1)
  def total = 0
  
  for (String item : lastline.split(' ')) {
    if (item.isNumber()) total += item.toInteger()
  }
  
  int fail = config.failThreshold ?: 25
  int warn = config.warnThreshold ?: 10

  boolean shouldFail = total >= fail
  boolean shouldWarn = total >= warn

  echo "[total hints:${total}] [fail threshold:${fail}] [warn threshold:${warn}]"
    
  if(shouldFail) error("Webhint.io found ${total} suggestion(s) meeting or exceeding the fail threshold of ${fail}.")
  else if(shouldWarn) unstable("Webhint.io found ${total} suggestion(s). Consider fixing a few of them.")
}
