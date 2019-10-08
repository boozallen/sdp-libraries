/*
  Copyright © 2018 Booz Allen Hamilton. All Rights Reserved.
  This software package is licensed under the Booz Allen Public License. The license can be found in the License file or at http://boozallen.github.io/licenses/bapl
*/

void call(old_tag, new_tag){
    def remove_local_image = config.remove_local_image ?: false
    node{
        unstash "workspace"

        login_to_registry()

        get_images_to_build().each{ img ->
          sh "docker pull ${img.registry}/${img.repo}:${old_tag}"
          sh "docker tag ${img.registry}/${img.repo}:${old_tag} ${img.registry}/${img.repo}:${new_tag}"
          sh "docker push ${img.registry}/${img.repo}:${new_tag}"
          if (remove_local_image) sh "docker rmi -f ${img.registry}/${img.repo}:${new_tag} 2> /dev/null"
        }
    }
}
