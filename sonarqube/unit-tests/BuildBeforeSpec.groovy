/*
  Copyright © 2018 Booz Allen Hamilton. All Rights Reserved.
  This software package is licensed under the Booz Allen Public License. The license can be found in the License file or at http://boozallen.github.io/licenses/bapl
*/

package sonarqube

import com.homeaway.devtools.jenkins.testing.JenkinsPipelineSpecification
import org.boozallen.plugins.jte.binding.TemplateBinding
import org.boozallen.plugins.jte.binding.injectors.StepWrapper

public class BuildBeforeSpec extends JenkinsPipelineSpecification {

  def BuildBefore = null

  public static class DummyException extends RuntimeException {
		public DummyException(String _message) { super( _message ); }
	}

  def setup() {
    BuildBefore = loadPipelineScriptForTest("sonarqube/build_before.groovy")
  }

  def "!require_build_step && !hasStep; nothing" () {
    setup:
    String stepName = "stepName"
    def context = [step:'static_code_analysis']
    def config = [build_step:stepName]

    TemplateBinding binding = Mock(TemplateBinding)
    1 * binding.hasStep(stepName) >> { return false}
    2 * binding.getVariable("config") >> {return config }
    0 * binding.getStep(stepName)

    BuildBefore.setBinding(binding)

    when:
    def res = BuildBefore.call(context)

    then:
    null == res
    notThrown(Exception)

  }

  def "require_build_step && !hasStep; throw Exception" () {
    setup:
    String stepName = "stepName"
    def context = [step:'static_code_analysis']
    def config = [build_step:stepName, require_build_step:true]

    TemplateBinding binding = Mock(TemplateBinding)
    1 * binding.hasStep(stepName) >> { return false}
    2 * binding.getVariable("config") >> {return config }
    0 * binding.getStep(stepName)

    BuildBefore.setBinding(binding)

    when:
    def res = BuildBefore.call(context)

    then:
    null == res
    Exception e = thrown(Exception)
    e.message.contains("require_build_step with no defined step named:")

  }

  def "hasStep && getStep; works" () {
    setup:
    String stepName = "stepName"
    def context = [step:'static_code_analysis']
    def config = [build_step:stepName]
    StepWrapper step = Mock(StepWrapper)
    1 * step.call(_) >> {  }
    0 * step.methodMissing(_, _)

    TemplateBinding binding = Mock(TemplateBinding)
    1 * binding.hasStep(stepName) >> { return true  }
    2 * binding.getVariable("config") >> {return config }
    1 * binding.getStep(stepName) >> { return step }

    BuildBefore.setBinding(binding)

    when:
    BuildBefore.call(context)

    then:
    notThrown(Exception)

  }

  def "hasStep && require_build_step && getStep; works" () {
    setup:
    String stepName = "stepName"
    def context = [step:'static_code_analysis']
    def config = [build_step:stepName, require_build_step:true]
    StepWrapper step = Mock(StepWrapper)
    1 * step.call(_) >> {  }
    0 * step.methodMissing(_, _)

    TemplateBinding binding = Mock(TemplateBinding)
    1 * binding.hasStep(stepName) >> { return true  }
    2 * binding.getVariable("config") >> {return config }
    1 * binding.getStep(stepName) >> { return step }

    BuildBefore.setBinding(binding)

    when:
    BuildBefore.call(context)

    then:
    notThrown(Exception)

  }

  def "hasStep && getStep && build_step_method; works" () {
    setup:
    String stepName = "stepName"
    String stepMethod = "stepMethod"
    def context = [step:'static_code_analysis']
    def config = [build_step:stepName, build_step_method:stepMethod]
    StepWrapper step = Mock(StepWrapper)
    0 * step.call(_) >> {  }
    1 * step.methodMissing(stepMethod, _) >> {}

    TemplateBinding binding = Mock(TemplateBinding)
    1 * binding.hasStep(stepName) >> { return true  }
    2 * binding.getVariable("config") >> {return config }
    1 * binding.getStep(stepName) >> { return step }

    BuildBefore.setBinding(binding)

    when:
    BuildBefore.call(context)

    then:
    notThrown(Exception)

  }

  def "hasStep && getStep && build_step_method is wrong; fails " () {
    setup:
    String stepName = "stepName"
    String stepMethod = "stepMethod"
    def context = [step:'static_code_analysis']
    def config = [build_step:stepName, build_step_method:stepMethod]
    StepWrapper step = Mock(StepWrapper)
    0 * step.call(_) >> {  }
    1 * step.methodMissing(stepMethod, _) >> { throw Exception }

    TemplateBinding binding = Mock(TemplateBinding)
    1 * binding.hasStep(stepName) >> { return true  }
    2 * binding.getVariable("config") >> {return config }
    1 * binding.getStep(stepName) >> { return step }

    BuildBefore.setBinding(binding)

    when:
    BuildBefore.call(context)

    then:
    Exception e = thrown()
    e != null

  }

}
