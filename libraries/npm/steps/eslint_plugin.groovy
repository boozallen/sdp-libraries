/*
  Copyright © 2018 Booz Allen Hamilton. All Rights Reserved.
  This software package is licensed under the Booz Allen Public License. The license can be found in the License file or at http://boozallen.github.io/licenses/bapl
*/

package libraries.npm.steps

@AfterStep({ hookContext.step.equals("lint_code") })
void call() {
    usingEslintPlugin = config?.lint_code?.use_eslint_plugin ?: false

    if (usingEslintPlugin) {
        evaluate "recordIssues enabledForFailure: true, tool: esLint(pattern: 'eslint-report.xml')"
    }
}
