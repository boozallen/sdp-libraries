/*
  Copyright © 2018 Booz Allen Hamilton. All Rights Reserved.
  This software package is licensed under the Booz Allen Public License. The license can be found in the License file or at http://boozallen.github.io/licenses/bapl
*/

package libraries.anchore.steps

import groovy.json.*

void call(){
    stage("Scanning Container Image: Anchore Scan"){
        def images = get_images_to_build()
        def anchore_engine_base_url = config.anchore_engine_url
        def anchore_policy_id = config.policy_id ?: null
        def image_wait_timeout = config.image_wait_timeout ?: 300
        def archive_only = false
        def bail_on_fail = true
        def perf_vuln_scan = true
        def perf_policy_eval = true

        if (config.archive_only != null) {
            archive_only = config.archive_only
        }
        if (config.bail_on_fail != null) {
            bail_on_fail = config.bail_on_fail
        }
        if (config.perform_vulnerability_scan != null) {
            perf_vuln_scan = config.perform_vulnerability_scan
        }
        if (config.perform_policy_evaluation != null) {
            perf_policy_eval = config.perform_policy_evaluation 
        }

        println """
        Library Configuration: 
          anchore_engine_url=${anchore_engine_base_url}
          image_wait_timeout=${image_wait_timeout} 
          policy_id=${anchore_policy_id} 
          archive_only=${archive_only} 
          bail_on_fail=${bail_on_fail} 
          perform_policy_evaluation=${perf_policy_eval} 
          perform_vulnerability_scan=${perf_vuln_scan}
        """.stripIndent(4).trim()
        
        node{
            sh "mkdir -p anchore_results"
            try {
                withCredentials([usernamePassword(credentialsId: config.cred, passwordVariable: 'pass', usernameVariable: 'user')]) {      	
                    images.each { img ->
                        def input_image_fulltag = "${img.registry}/${img.repo}:${img.tag}"

                        success = false
                        timeout(time: 1200, unit: 'SECONDS') {		  		  
                            (success, new_image) = this.add_image(config, user, pass, input_image_fulltag)
                        }
                        if (success) {
                            println("Image analysis successful")
                        } else {
                            error "Failed to add image to Anchore Engine for analysis"
                        }

                        if (perf_vuln_scan) {
                            success = false
                            timeout(time: 1200, unit: 'SECONDS') {
                                (success, vulnerabilities) = this.get_image_vulnerabilities(config, user, pass, new_image)
                            }
                            if (success) {
                                println("Image vulnerabilities report generation complete")
                                if(!archive_only){
                                    print_image_vulnerabilities(vulnerabilities)
                                }
                            } else {
                                error "Failed to retrieve vulnerability results from Anchore Engine from analyzed image"
                            }
                        }

                        if (perf_policy_eval) {
                            success = false
                            timeout(time: 1200, unit: 'SECONDS') {		  
                                (success, evaluations) = get_image_evaluations(config, user, pass, new_image, input_image_fulltag)
                            }
                            if (success) {
                                println("Image policy evaluation report generation complete")
                                String final_action = evaluations.final_action
                                if(!archive_only){
                                    print_image_evalutations(evaluations)
                                }
                                if (bail_on_fail) {
                                    // check policy eval final action and exit if STOP
                                    if (final_action == "stop" || final_action == 'STOP') {
                                        error "Anchore policy evaluation resulted in STOP action - failing scan."
                                    }
                                }
                            } else {
                                error "Failed to retrieve policy evaluation results from Anchore Engine from analyzed image"
                            }		    
                        }
                    }
                }
            } catch (any) {
                throw any
            } finally {
                archiveArtifacts allowEmptyArchive: true, artifacts: 'anchore_results/'
            }
        }
    }
}

def parse_json(input_file) {
    return readJSON(file: "${input_file}")
}

def add_image(config, user, pass, input_image_fulltag) {
    String anchore_engine_base_url = config.anchore_engine_url
    int anchore_image_wait_timeout = config.image_wait_timeout ?: 300
    Boolean done = false
    Boolean success = false
    def ret_image = null
    String url
    String image_digest
    def input_image = [tag: "${input_image_fulltag}"]
    def input_image_json = JsonOutput.toJson(input_image)
    http_result = "new_anchore_image.json"
    try {
        url = "${anchore_engine_base_url}/images"
        sh "curl -u '${user}':'${pass}' -H 'content-type: application/json' -X POST -o ${http_result} '${url}' -d '${input_image_json}' 2>curl.err"
        def new_image = this.parse_json(http_result)[0]
        image_digest = new_image.imageDigest
        if (!image_digest) {
            throw new Exception("Error response from Anchore Engine")
        }
    } catch (any) {
        try {
            sh "cat curl.err ${http_result}"    
        } catch (ignore) {}
        throw any
    }

    try {
        url = "${anchore_engine_base_url}/images/${image_digest}"
        timeout(time: anchore_image_wait_timeout, unit: 'SECONDS') {
            while(!done) {
                http_result = "new_anchore_image_check.json"
                try {
                    sh "curl -u '${user}':'${pass}' -H 'content-type: application/json' -X GET -o ${http_result} '${url}' 2>curl.err"
                    def new_image_check = this.parse_json(http_result)[0]
                    if (new_image_check.analysis_status == "analyzed") {
                        done = true
                        success = true
                        ret_image = new_image_check
                    } else if ( new_image_check.analysis_status == "analysis_failed") {
                        done = true
                        success = false
                    } else {
                        println("image not yet analyzed - status is ${new_image_check.analysis_status}")
                        sleep 5
                    }
                } catch (any) {
                    success = false
                    done = true
                    throw any
                } 
            }
        }
    } catch (any) {
        println("Timed out or error waiting for image to reach analyzed state")
        success = false
        ret_image = null
        try {
            sh "cat curl.err ${http_result}"       
        } catch (ignore) {}
    } 
    return [success, ret_image]
}

def get_image_vulnerabilities(config, user, pass, image) {
    String anchore_engine_base_url = config.anchore_engine_url
    Boolean success = false
    def vulnerabilities = null
    ArrayList ret_vulnerabilities = null
    String url = null

    http_result = "anchore_results/anchore_vulnerabilities.json"
    try {
        url = "${anchore_engine_base_url}/images/${image.imageDigest}/vuln/all?vendor_only=True"
        sh "curl -u '${user}':'${pass}' -H 'content-type: application/json' -o ${http_result} '${url}' 2>curl.err"
        vulnerabilities = this.parse_json(http_result)
        if (vulnerabilities.containsKey("vulnerabilities")) {
            ret_vulnerabilities = vulnerabilities.vulnerabilities
            success = true
        } else {
            throw new Exception ("ERROR response from Anchore Engine")
        }
    } catch (any) {
        try {
            sh "cat curl.err ${http_result}"           
        } catch (ignore) {}
        throw any
    }
    return [success, ret_vulnerabilities]
}

void print_image_vulnerabilities(vulnerabilities){
    vulnerability_result =  "Anchore Image Scan Vulnerability Results\n"
    vulnerability_result += "****************************************\n\n"
    if (vulnerabilities) {
        vulnerability_result += "VulnID".padRight(16, ' ')+"\t" + "Severity".padRight(12, ' ') + "\t" + "Package".padRight(30, ' ') + "\t" + "Type".padRight(6, ' ') + "\t" + "Fix Available".padRight(12, ' ') + "\tLink\n"
        vulnerabilities.each { vuln ->
            vid = vuln.vuln.padRight(16, ' ')
            vsev = vuln.severity.padRight(12, ' ')
            vpkg = vuln.package.padRight(30, ' ') 
            vtype = vuln.package_type.padRight(6, ' ')
            vfix = vuln.fix.padRight(12, ' ')
            vurl = vuln.url
            vulnerability_result += "${vid}\t${vsev}\t${vpkg}\t${vtype}\t${vfix}\t${vurl}\n"
        }
    } else {
        vulnerability_result += "No vulnerabilities detected\n"
    }
    println vulnerability_result
}

def get_image_evaluations(config, user, pass, image, input_image_fulltag) {
    String anchore_engine_base_url = config.anchore_engine_url
    String anchore_policy_id = config.policy_id ?: null
    Boolean success = false
    def evaluations = null
    def ret_evaluations = null
    String url = null

    String image_digest = image.imageDigest
    String image_id = image.image_detail[0].imageId

    http_result = "anchore_results/anchore_policy_evaluations.json"
    try {
        url = "${anchore_engine_base_url}/images/${image_digest}/check?history=false&detail=true&tag=${input_image_fulltag}"
        if (anchore_policy_id) {
            url += "&policyId=${anchore_policy_id}"
        }
        sh "curl -u '${user}':'${pass}' -H 'content-type: application/json' -o ${http_result} '${url}' 2>curl.err"
        evaluations = this.parse_json(http_result)
        ret_evaluations = evaluations[0]["${image_digest}"]["${input_image_fulltag}"]["detail"]["result"]["result"][0]["${image_id}"]["result"]
        success = true
    } catch (any) {
        try {
            sh "cat curl.err ${http_result}"               
        } catch (ignore) {}
        throw any
    }

    return [success, ret_evaluations]
}

void print_image_evalutations(evaluations){
    if (evaluations) {
        evaluation_result =  "Anchore Image Scan Policy Evaluation Results\n"
        evaluation_result += "********************************************\n"
        evaluation_result += "Gate".padRight(12, ' ')+"\t" + "Trigger".padRight(12, ' ') + "\t" + "Action".padRight(6, ' ') + "\t" + "Details\n"
        evaluations.rows.each { eval ->
            egate = eval[3].padRight(12, ' ')
            etrigger = eval[4].padRight(12, ' ')
            eaction = eval[6].padRight(6, ' ')
            edetail = eval[5]
            evaluation_result += "${egate}\t${etrigger}\t${eaction}\t${edetail}"
        }
    } else {
        evaluation_result = "No evaluations to report\n"
    }
    println evaluation_result
}