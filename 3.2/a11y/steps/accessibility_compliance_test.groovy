/*
  Copyright © 2018 Booz Allen Hamilton. All Rights Reserved.
  This software package is licensed under the Booz Allen Public License. The license can be found in the License file or at http://boozallen.github.io/licenses/bapl
*/

package libraries.a11y.steps

void call(){

  stage "Accessibility Compliance Scan", {

    def url = env.FRONTEND_URL ?: config.url ?: {
      error """
      A11y Library needs the target url.
      libraries{
        a11y{
          url = "https://example.com"
        }
      }
      """
    } ()

    inside_sdp_image "a11y", {
      sh "a11ym -o accessibility_compliance ${url}"
      archive "accessibility_compliance/**"
    }
  }

}