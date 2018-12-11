/*
  Copyright © 2018 Booz Allen Hamilton. All Rights Reserved.
  This software package is licensed under the Booz Allen Public License. The license can be found in the License file or at http://boozallen.github.io/licenses/bapl
*/

def call() {
  node {
    unstash "workspace"
    stage ('Static Dependency Security Scan') {

      //The folder that is scanned; it should meet the requirements of the tool's scanners
      def scan_target = config.scan_target ? "$WORKSPACE/${config.scan_target}" : "$WORKSPACE"

      //The folders ()& their contents) that are ignored by the scan are passed in with the "--exclude" flag to the tool
      if ( ! config.exclude_dirs ) { //exclude_dirs is a comma-separated list of *directories* to ignore
        echo "No dir(s) excluded."
        exclude_opt = ""
      } else {
        echo "Excluding dir(s): ${config.exclude_dirs}"
        exclude_opt = config.exclude_dirs.replace(',','/**\' --exclude \'') //need an additional flag for each dir to ignore
        exclude_opt = "--exclude \'${exclude_opt}/**\'"
        echo "Exclude command: $exclude_opt"
      }

      // Vulnerabilities are scored 0-10, w/ 10 being most severe. This threshold needs to be >0
      // For any threshold greater than 10, the pipeline will not fail due to any detected vulnerability
      def cvss_threshold = (config.cvss_threshold == "pass") ? "11" :
                           (config.cvss_threshold ==~ /^\d+$/) ? config.cvss_threshold :
                           {error "CVSS Threshold is not properly defined in Pipeline Config"}()

      def image_version = config.image_version ?: "latest"

      def report_format = config.report_format ?: "ALL"

      //Check for "missing node_modules" corner case
      if( ( fileExists("${scan_target}/package.json") || fileExists("${scan_target}/package-lock.json") ) && ! fileExists("${scan_target}/node_modules") ){
        echo "package.json or package-lock.json detected, but not node_modules. Running \"npm install\" at least once"
        docker.image("node:latest").inside{
          sh "npm install"
        }
      }

      def data_dir = "owasp_logs/data"
      def report_dir = "owasp_logs/reports"

      sh """
      if [ ! -d "$data_dir" ]; then
        echo "Initially creating persistent directories"
        mkdir -p "$data_dir"
        chmod -R 777 "$data_dir"
        mkdir -p "$report_dir"
        chmod -R 777 "$report_dir"
      fi
      """

      inside_sdp_image "owasp-dep-check:$image_version", {
        try {
          sh """ /usr/share/dependency-check/bin/dependency-check.sh \
            --scan ${scan_target} \
            --format \"${report_format}\" \
            --project \"OWASP_dependency_check\" \
            --out ${report_dir} \
            --failOnCVSS ${cvss_threshold} \
            --cveUrl12Base     "https://nvd.nist.gov/feeds/xml/cve/1.2/nvdcve-%d.xml.gz" \
            --cveUrl20Base     "https://nvd.nist.gov/feeds/xml/cve/2.0/nvdcve-2.0-%d.xml.gz" \
            --cveUrl12Modified "https://nvd.nist.gov/feeds/xml/cve/1.2/nvdcve-modified.xml.gz" \
            --cveUrl20Modified "https://nvd.nist.gov/feeds/xml/cve/2.0/nvdcve-2.0-modified.xml.gz" \
            ${exclude_opt}
          """
        }
        catch (ex) {
          println "static dependency check failed with exception: " + ex
          if (cvss_threshold > 10) error "Error occured when running OWASP Dependency Check"
          else error 'Vulnerabilities found over threshold - stopping build'
          throw ex
        }
        finally {
          echo 'Publishing reports'
          sh "ls -l $WORKSPACE/owasp_logs/"
          //TODO: stop archiveArtifacts from failing if "owasp_logs/reports" DNE or is empty
          archiveArtifacts artifacts: "owasp_logs/reports/*.*"
        }
      }
    }
  }
}
