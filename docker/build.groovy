/*
  Copyright © 2018 Booz Allen Hamilton. All Rights Reserved.
  This software package is licensed under the Booz Allen Public License. The license can be found in the License file or at http://boozallen.github.io/licenses/bapl
*/

def call(){
  stage "Building Docker Image", {
    node{
      unstash "workspace" 

      login_to_registry()

      def images = get_images_to_build()
      images.each{ img ->
        sh "docker build ${img.context} -t ${img.repo}/${img.path}:${img.tag}"
        sh "docker push ${img.repo}/${img.path}:${img.tag}"
      }

    }
  }
}
