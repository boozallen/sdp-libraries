/*
  Copyright © 2018 Booz Allen Hamilton. All Rights Reserved.
  This software package is licensed under the Booz Allen Public License. The license can be found in the License file or at http://boozallen.github.io/licenses/bapl
*/

package libraries.docker

void call(String _url = null, String _credentialId = null, def body){

  def (repository, cred) = get_registry_info()

  def url = _url ?: repository
  def credentialId = _credentialId ?: cred

  docker.withRegistry(url, credentialId, body)

}
