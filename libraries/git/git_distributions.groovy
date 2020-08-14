/*
  Copyright © 2018 Booz Allen Hamilton. All Rights Reserved.
  This software package is licensed under the Booz Allen Public License. The license can be found in the License file or at http://boozallen.github.io/licenses/bapl
*/

/*
  Validate library configuration
*/
@Validate
void call(context){
    String distributionConfig = config.distribution
    List options = [ "github", "github_enterprise", "gitlab" ]

    if (!options.contains(distributionConfig)) {
        error "Distribution can only be set to one of the following: github, github_enterprise, gitlab. Currently: ${distributionConfig}"
    }

    env.GIT_LIBRARY_DISTRUBITION = distributionConfig
    def dist = this.fetch()
    dist.validate_configuration()
}

def fetch(){
    return getBinding().getStep(env.GIT_LIBRARY_DISTRUBITION)
}
