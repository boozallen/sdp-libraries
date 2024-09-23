/*
  Copyright © 2022 Booz Allen Hamilton. All Rights Reserved.
  This software package is licensed under the Booz Allen Public License.
  The license can be found in the License file or at http://boozallen.github.io/licenses/bapl
*/

package libraries.maven

public class MavenInvokeSpec extends JTEPipelineSpecification {
    def MavenInvoke = null

    LinkedHashMap minimalUnitTestingConfig = [
        unit_test: [
            stageName: 'Maven Unit Tests',
            buildContainer: 'mvn',
            phases: ['test']
        ]
    ]

    LinkedHashMap withMavenParams = [ 'traceability': false ]

    static class DummyException extends RuntimeException {
        DummyException(String _message) { super( _message ) }
    }

    def setup() {
        LinkedHashMap config = [:]
        LinkedHashMap env = [:]
        LinkedHashMap stepContext = [ name: 'unit_test' ]

        MavenInvoke = loadPipelineScriptForStep('maven', 'maven_invoke')

        explicitlyMockPipelineStep('inside_sdp_image')

        MavenInvoke.getBinding().setVariable('config', config)
        MavenInvoke.getBinding().setVariable('env', env)
        MavenInvoke.getBinding().setVariable('stepContext', stepContext)
    }

    def 'Completes a mvn test successfully' () {
        setup:
            getPipelineMock('sh')('mvn archetype:generate -DgroupId=com.mycompany.app -DartifactId=my-app -DarchetypeArtifactId=maven-archetype-quickstart -DarchetypeVersion=1.4 -DinteractiveMode=false && cd my-app')
            MavenInvoke.getBinding().setVariable('config', minimalUnitTestingConfig)
        when:
            MavenInvoke()
        then:
            1 * getPipelineMock('sh').call('mvn test ')
    }

    def 'Application environment settings take precendence over library config' () {
        setup:
            MavenInvoke.getBinding().setVariable('config', [
                unit_test: [
                    stageName: 'LibConfig Maven Stage',
                    buildContainer: 'mvn',
                    phases: ['clean', 'build']
                ]
            ])
        when:
            MavenInvoke([
                maven: [
                    unit_test: [
                        stageName: 'AppEnv Defined Maven Stage',
                        options: ['-v'],
                        phases: []
                    ]
                ]
            ])
        then:
            MavenInvoke.getBinding().variables.env.buildContainer == 'mvn'
            MavenInvoke.getBinding().variables.env.options == ['-v']
            MavenInvoke.getBinding().variables.env.stageName == 'AppEnv Defined Maven Stage'
            MavenInvoke.getBinding().variables.env.phases == []
    }

    def 'Artifacts get archived as expected' () {
        setup:
            getPipelineMock('sh')('mvn archetype:generate -DgroupId=com.mycompany.app -DartifactId=my-app -DarchetypeArtifactId=maven-archetype-quickstart -DarchetypeVersion=1.4 -DinteractiveMode=false && cd my-app')
            MavenInvoke.getBinding().setVariable('stepContext', [name: 'build'])
            MavenInvoke.getBinding().setVariable('config', [
                build: [
                    stageName: 'Maven Build',
                    buildContainer: 'mvn',
                    phases: ['clean', 'install'],
                    artifacts: ['target/*.jar']
                ]
            ])
        when:
            MavenInvoke()
        then:
            1 * getPipelineMock('archiveArtifacts.call')(_ as Map)
    }

    def 'Command is wrapped with withMaven if withMavenParams are defined' () {
        setup:
            getPipelineMock('sh')('mvn archetype:generate -DgroupId=com.mycompany.app -DartifactId=my-app -DarchetypeArtifactId=maven-archetype-quickstart -DarchetypeVersion=1.4 -DinteractiveMode=false && cd my-app')
            MavenInvoke.getBinding().setVariable('stepContext', [name: 'build'])
            MavenInvoke.getBinding().setVariable('config', [
                build: [
                    stageName: 'Maven Build',
                    buildContainer: 'mvn',
                    phases: ['clean', 'install'],
                    artifacts: ['target/*.jar'],
                    withMavenParams: withMavenParams
                ]
            ])
        when:
            MavenInvoke()
        then:
            1 * getPipelineMock('withMaven').call(withMavenParams)
    }
}
