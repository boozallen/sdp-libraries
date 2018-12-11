/*
  Copyright © 2018 Booz Allen Hamilton. All Rights Reserved.
  This software package is licensed under the Booz Allen Public License. The license can be found in the License file or at http://boozallen.github.io/licenses/bapl
*/

void call(old_tag, new_tag){
    node{
        unstash "workspace"

        login_to_registry()
        
        get_images_to_build().each{ img ->
          sh "docker pull ${img.repo}/${img.path}:${old_tag}"
          sh "docker tag ${img.repo}/${img.path}:${old_tag} ${img.repo}/${img.path}:${new_tag}"
          sh "docker push ${img.repo}/${img.path}:${new_tag}"
        }
    }
}