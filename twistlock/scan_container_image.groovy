/*
  Copyright © 2018 Booz Allen Hamilton. All Rights Reserved.
  This software package is licensed under the Booz Allen Public License. The license can be found in the License file or at http://boozallen.github.io/licenses/bapl
*/

def call() {

    if (!config.url)
        error "Twistlock url not defined in library spec"

    if (!config.credential)
        error "Twistlock credential not defined in library spec"

    stage "Scanning Container Images", {
        node {
            withCredentials([usernamePassword(credentialsId: config.credential, passwordVariable: 'pass', usernameVariable: 'user')]) {
                unstash "workspace"
                this.get_twistcli()
                login_to_registry() // from container image building library
                // comes from whatever library builds container images
                // for now .. just docker
                def images = ""
                get_images_to_build().each { img ->
                  image = "${img.repo}/${img.path}:${img.tag} " //The trailing space is intentional
                  sh "docker pull ${image}"
                  images += image
                }
                def scan_url = this.do_scan(images)
                def results = this.parse_results(scan_url)
                results.images.each {
                  echo """
                      Twistlock Scan Results: ${it.info.tags.registry}/${it.info.tags.repo}/${it.info.tags.tag}
                      -----------------------------------------
                      CVE Results:
                      Low:      ${it.info.cveVulnerabilityDistribution.low}
                      Medium:   ${it.info.cveVulnerabilityDistribution.medium}
                      High:     ${it.info.cveVulnerabilityDistribution.high}
                      Critical: ${it.info.cveVulnerabilityDistribution.critical}
                      Compliance Results:
                      Low:      ${it.info.complianceDistribution.low}
                      Medium:   ${it.info.complianceDistribution.medium}
                      High:     ${it.info.complianceDistribution.high}
                      Critical: ${it.info.complianceDistribution.critical}
                  """.stripIndent()
                }
            }
        }
    }
}

void get_twistcli() {
    echo "getting twistlock CLI"
    sh "curl -k -u '${user}':'${pass}' -H 'Content-Type: application/json' -X GET -o /usr/local/bin/twistcli ${config.url}/api/v1/util/twistcli"
    sh "chmod +x /usr/local/bin/twistcli"
    sh "twistcli -v"
}

String do_scan(images) {
    def output = sh(
            script: "twistcli images scan --details --upload --address ${config.url} -u ${user} -p '${pass}' ${images}",
            returnStdout: true
    ).trim()

    return (output =~ /Results at: (.*)$/)[0][1]
}

def parse_results(scan_url) {
    auth_64 = sh(returnStdout: true, script: "echo -n '${user}:${pass}' | openssl base64").trim()
    sh "curl -k -H 'Authorization: Basic ${auth_64}' ${scan_url} > scan.tar.gz"
    sh "tar -xvf scan.tar.gz"
    sh "mv analysis.json twistlock_results.json"
    archiveArtifacts 'twistlock_results.json'
    return readJSON(file: "twistlock_results.json")
}
