/*
  Copyright © 2018 Booz Allen Hamilton. All Rights Reserved.
  This software package is licensed under the Booz Allen Public License. The license can be found in the License file or at http://boozallen.github.io/licenses/bapl
*/

package libraries.docker.steps

def call() {
    def ret = []
    errors = []
    ret << (config.registry ?:
            { errors << "Application Docker Image Registry, libraries.docker.registry, not defined in pipeline config" }() )
    ret << ( config.cred ?:
            { errors << "Application Docker Image Registry Credential, libraries.docker.cred, not defined in pipeline config" }() )

    if(!errors.empty) {
        error errors.join("; ")
    }

    return ret
}
