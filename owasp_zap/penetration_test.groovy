/*
  Copyright © 2018 Booz Allen Hamilton. All Rights Reserved.
  This software package is licensed under the Booz Allen Public License. The license can be found in the License file or at http://boozallen.github.io/licenses/bapl
*/

void call() {
  stage "Penetration Test", {

    def target = env.FRONTEND_URL ?: config.target ?: {
      error """
      OWASP Zap target url undefined.  Set:
      libraries{
        owasp_zap{
            target = <url_to_attack>
        }
      }
      """.stripIndent(6)
    }()

    def vuln_threshold
    if (config.vulnerability_threshold){
      if ( !(config.vulnerability_threshold in ["Ignore", "Low", "Medium", "High", "Informational"])){
        error "OWASP Zap: Vulnerability Threshold ${config.vulnerability_threshold} not Ignore, Low, Medium, High, or Informational"
      }
      vuln_threshold = config.vulnerability_threshold
    } else {
       vuln_threshold = "High"
    }

    inside_sdp_image "zap", {
      // start zap daemon
      sh """zap.sh -daemon \
                    -host 127.0.0.1 \
                    -port 8080 \
                    -config api.disablekey=true \
                    -config scanner.attackOnStart=true \
                    -config view.mode=attack \
                    -config connection.dnsTtlSuccessfulQueries=-1 \
                    -config api.addrs.addr.name=.* \
                    -config api.addrs.addr.regex=true &
        """
      // validate daemon is running
      sh "zap-cli status -t 60"
      // prep-url, do scan, generate report
      sh """ zap-cli open-url ${target} && \
             zap-cli spider ${target} && \
             zap-cli active-scan -r ${target} && \
             zap-cli report -o zap.html -f html
         """
      archive "zap.html"
      // fail if vuln_threshold met
      if(!vuln_threshold.equals("Ignore")){
        def n_vulns = sh script: "zap-cli alerts -l ${vuln_threshold}", returnStatus: true
        if (n_vulns){
            error "OWASP Zap found ${n_vulns} ${vuln_threshold} vulnerabilities while performing a scan"
        }
      }
    }
  }
}
