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
import org.camunda.bpm.engine.task.Task;
import org.camunda.bpm.engine.variable.VariableMap;
import org.camunda.bpm.engine.variable.Variables;
import org.camunda.bpm.model.bpmn.Bpmn;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;

import java.util.Map;

/**
 * @author Daniel Meyer
 *
 */
public class SpinScriptTaskSupportWithAutoStoreScriptVariablesTest extends PluggableProcessEngineTestCase {

  protected static String TEST_SCRIPT = "var_s = S('{}')\n" +
                                        "var_xml = XML('<root/>')\n" +
                                        "var_json = JSON('{}')\n";

  protected ProcessInstance processInstance;

  public void setUp() {
    processEngineConfiguration.setAutoStoreScriptVariables(true);
  }

  public void tearDown() {
    processEngineConfiguration.setAutoStoreScriptVariables(false);
  }

  public void testSpinInternalVariablesNotExportedGroovyScriptTask() {
    String importXML = "XML = org.camunda.spin.Spin.&XML\n";
    String importJSON = "JSON = org.camunda.spin.Spin.&JSON\n";

    String script = importXML + importJSON + TEST_SCRIPT;

    deployProcess("groovy", script);

    startProcess();
    checkVariables("foo", "var_s", "var_xml", "var_json");
    continueProcess();
    checkVariables("foo", "var_s", "var_xml", "var_json");
  }

  public void testSpinInternalVariablesNotExportedByJavascriptScriptTask() {
    String importXML = "var XML = org.camunda.spin.Spin.XML;\n";
    String importJSON = "var JSON = org.camunda.spin.Spin.JSON;\n";

    String script = importXML + importJSON + TEST_SCRIPT;

    deployProcess("javascript", script);

    startProcess();
    checkVariables("foo", "var_s", "var_xml", "var_json");
    continueProcess();
    checkVariables("foo", "var_s", "var_xml", "var_json");
  }

  public void testSpinInternalVariablesNotExportedByPythonScriptTask() {
    String importXML = "import org.camunda.spin.Spin.XML as XML;\n";
    String importJSON = "import org.camunda.spin.Spin.JSON as JSON;\n";

    String script = importXML + importJSON + TEST_SCRIPT;

    deployProcess("python", script);

    startProcess();
    checkVariables("foo", "var_s", "var_xml", "var_json");
    continueProcess();
    checkVariables("foo", "var_s", "var_xml", "var_json");
  }

  public void testSpinInternalVariablesNotExportedByRubyScriptTask() {
    String importXML = "def XML(*args)\n\torg.camunda.spin.Spin.XML(*args)\nend\n";
    String importJSON = "def JSON(*args)\n\torg.camunda.spin.Spin.JSON(*args)\nend\n";

    String script = importXML + importJSON + TEST_SCRIPT;

    deployProcess("ruby", script);

    startProcess();
    checkVariables("foo");
    continueProcess();
    checkVariables("foo");
  }

  protected void startProcess() {
    VariableMap variables = Variables.putValue("foo", "bar");
    processInstance = runtimeService.startProcessInstanceByKey("testProcess", variables);
  }

  protected void continueProcess() {
    Task task = taskService.createTaskQuery().singleResult();
    assertNotNull(task);
    taskService.complete(task.getId());
  }

  protected void checkVariables(String... expectedVariables) {
    Map<String, Object> variables = runtimeService.getVariables(processInstance.getId());

    assertFalse(variables.containsKey("S"));
    assertFalse(variables.containsKey("XML"));
    assertFalse(variables.containsKey("JSON"));

    for (String expectedVariable : expectedVariables) {
      assertTrue(variables.containsKey(expectedVariable));
    }

    assertEquals(expectedVariables.length, variables.size());
  }

  protected void deployProcess(String scriptFormat, String scriptText) {
    BpmnModelInstance process = createProcess(scriptFormat, scriptText);
    Deployment deployment = repositoryService.createDeployment()
      .addModelInstance("testProcess.bpmn", process)
      .addString("testScript.txt", scriptText)
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
      .scriptTask()
        .scriptFormat(scriptFormat)
        .camundaResource("deployment://testScript.txt")
      .userTask()
      .endEvent()
    .done();

  }
}
