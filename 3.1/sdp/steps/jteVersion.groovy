/*
  Copyright © 2018 Booz Allen Hamilton. All Rights Reserved.
  This software package is licensed under the Booz Allen Public License. 
  The license can be found in the License file or at http://boozallen.github.io/licenses/bapl
*/
package libraries.sdp.steps

import jenkins.model.Jenkins
import groovy.transform.Field

@Field static private final int LESS_THAN = -1
@Field static private final int GREATER_THAN = 1
@Field static private final int EQUAL_TO = 0

String get(){
  return Jenkins.get().pluginManager.getPlugin("templating-engine").version
}

boolean lessThan(String version){
  return compare(this.get(), version) == LESS_THAN
}

boolean greaterThan(String version){
  return compare(this.get(), version) == GREATER_THAN
}

boolean equalTo(String version){
  return compare(this.get(), version) == EQUAL_TO
}

boolean lessThanOrEqualTo(String version){
  return compare(this.get(), version) in [LESS_THAN, EQUAL_TO]
}

boolean greaterThanOrEqualTo(String version){
  return compare(this.get(), version) in [LESS_THAN, GREATER_THAN]
}

int compare(String v1, String v2){
  return compareVersions(v1: v1, v2: v2)
}

