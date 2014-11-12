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

/**
 * @author Daniel Meyer
 *
 */
public class SpinScriptTaskSupportTest extends PluggableProcessEngineTestCase {

  public void testSpinAvailableInGroovy() {
    deployProcess("groovy", "execution.setVariable('name',  S('<test />').name() )\n");
    ProcessInstance pi = runtimeService.startProcessInstanceByKey("testProcess");

    String var = (String) runtimeService.getVariable(pi.getId(), "name");
    assertEquals("test", var);
  }

  public void testSpinAvailableInJavascript() {
    deployProcess("javascript", "execution.setVariable('name',  S('<test />').name() )\n");
    ProcessInstance pi = runtimeService.startProcessInstanceByKey("testProcess");

    String var = (String) runtimeService.getVariable(pi.getId(), "name");
    assertEquals("test", var);
  }

  public void testSpinAvailableInPython() {
    deployProcess("python", "execution.setVariable('name',  S('<test />').name() )\n");
    ProcessInstance pi = runtimeService.startProcessInstanceByKey("testProcess");

    String var = (String) runtimeService.getVariable(pi.getId(), "name");
    assertEquals("test", var);
  }

  public void testSpinAvailableInRuby() {
    deployProcess("ruby", "$execution.setVariable('name',  S('<test />').name() )\n");
    ProcessInstance pi = runtimeService.startProcessInstanceByKey("testProcess");

    String var = (String) runtimeService.getVariable(pi.getId(), "name");
    assertEquals("test", var);
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
