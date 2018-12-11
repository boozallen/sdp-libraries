/*
  Copyright © 2018 Booz Allen Hamilton. All Rights Reserved.
  This software package is licensed under the Booz Allen Public License. The license can be found in the License file or at http://boozallen.github.io/licenses/bapl
*/

def call(){
  stage "Functional Test", {

    // configurable params in this lib
    def lib_spec = """
    libraries{
      protractor{
        url = <base_url_to_test>     # base url to test
        enforce = <true or false>    # fail the build? defaults to true
        config_file = <path_to_conf> # path to protractor config file
      }
    }
    """

    // base url to pass to protractor
    def url = env.FRONTEND_URL ?: config.url ?: {
      error """
      Protractor base url undefined. Set:
      ${lib_spec}
      """.stripIndent(6)
    }()

    // fail the build if tests fail?
    def enforce = true
    if (config.enforce){
      if (!(config.enforce instanceof Boolean)){
        error """
        Protractor enforce field must be Boolean, found: ${config.enforce}
        ${lib_spec}
        """.stripIndent(8)
      }
      enforce = config.enforce
    }


    // path to protractor configuration file
    def conf = config.config_file ?: ""

    // do tests
    inside_sdp_image "protractor", {
      unstash "workspace"
      
      if (fileExists("package.json"))
        sh "npm install --only=dev"
      
      if (!fileExists(conf)) 
        error "Protractor configuration file ${conf} not found."
    
      // runs XVFB to render browser in memory
      sh "Xvfb -ac :99 -screen 0 1280x1024x16 &"
      // starts a selenium server in the container
      sh "webdriver-manager start &"
      // run tests
      def tests_fail = sh script: "protractor ${conf} --baseUrl='${url}'",
                          returnStatus: true

      if (tests_fail && enforce) error "Protractor tests failed"

    }
  }
}
