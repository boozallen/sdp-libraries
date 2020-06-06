/*
  Copyright © 2018 Booz Allen Hamilton. All Rights Reserved.
  This software package is licensed under the Booz Allen Public License. The license can be found in the License file or at http://boozallen.github.io/licenses/bapl
*/

package sdp

import com.homeaway.devtools.jenkins.testing.JenkinsPipelineSpecification

public class NodeSpec extends JenkinsPipelineSpecification {

  def Node = null

  def testConfig = [:]

  public static class DummyException extends RuntimeException {
		public DummyException(String _message) { super( _message ); }
	}

  def setup() {
    Node = loadPipelineScriptForTest("./sdp/node.groovy")
  }

