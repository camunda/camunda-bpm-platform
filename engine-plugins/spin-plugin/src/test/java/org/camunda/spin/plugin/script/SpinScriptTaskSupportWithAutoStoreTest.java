/* Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.camunda.spin.plugin.script;

import org.camunda.bpm.engine.impl.test.PluggableProcessEngineTestCase;
import org.camunda.bpm.engine.repository.Deployment;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.model.bpmn.Bpmn;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;

import java.util.Map;

/**
 * @author Daniel Meyer
 *
 */
public class SpinScriptTaskSupportWithAutoStoreTest extends PluggableProcessEngineTestCase {

  public void setUp() {
    processEngineConfiguration.setAutoStoreScriptVariables(true);
  }

  public void tearDown() {

    processEngineConfiguration.setAutoStoreScriptVariables(false);
  }

  public void testSpinInternalVariablesNotAvailableInGroovy() {
    String importXML = "XML = org.camunda.spin.Spin.&XML\n";
    String importJSON = "JSON = org.camunda.spin.Spin.&JSON\n";

    String script = importXML + importJSON;

    deployProcess("groovy", script);
    checkVariables();
  }

  public void testSpinInternalVariablesNotAvailableInJavascript() {
    String importXML = "var XML = org.camunda.spin.Spin.XML;\n";
    String importJSON = "var JSON = org.camunda.spin.Spin.JSON;\n";

    String script = importXML + importJSON;

    deployProcess("javascript", script);
    checkVariables();
  }

  public void testSpinInternalVariablesNotAvailableInPython() {
    String importXML = "import org.camunda.spin.Spin.XML as XML;\n";
    String importJSON = "import org.camunda.spin.Spin.JSON as JSON;\n";

    String script = importXML + importJSON;

    deployProcess("python", script);
    checkVariables();
  }

  public void testSpinInternalVariablesNotAvailableInRuby() {
    String importXML = "def XML(*args)\n\torg.camunda.spin.Spin.XML(*args)\nend\n";
    String importJSON = "def JSON(*args)\n\torg.camunda.spin.Spin.JSON(*args)\nend\n";

    String script = importXML + importJSON;

    deployProcess("ruby", script);
    checkVariables();
  }

  protected void checkVariables() {
    ProcessInstance pi = runtimeService.startProcessInstanceByKey("testProcess");

    Map<String, Object> variables = runtimeService.getVariables(pi.getId());

    assertFalse(variables.containsKey("S"));
    assertFalse(variables.containsKey("XML"));
    assertFalse(variables.containsKey("JSON"));
  }

  protected void deployProcess(String scriptFormat, String scriptText) {
    BpmnModelInstance process = createProcess(scriptFormat, scriptText);
    Deployment deployment = repositoryService.createDeployment()
      .addModelInstance("testProcess.bpmn", process)
      .deploy();
    deploymentId = deployment.getId();
  }

  protected BpmnModelInstance createProcess(String scriptFormat, String scriptText) {

    return Bpmn.createExecutableProcess("testProcess")
      .startEvent()
      .scriptTask()
        .scriptFormat(scriptFormat)
        .scriptText(scriptText)
      .userTask()
      .endEvent()
    .done();

  }
}
