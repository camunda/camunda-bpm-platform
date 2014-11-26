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
package org.camunda.bpm.engine.test.standalone.history;

import java.util.Collections;
import java.util.List;

import org.camunda.bpm.engine.impl.test.PluggableProcessEngineTestCase;
import org.camunda.bpm.engine.task.Task;
import org.camunda.bpm.engine.test.Deployment;
import org.camunda.bpm.engine.variable.Variables;

/**
 * @author Thorben Lindhauer
 *
 */
public class HistoricTaskInstanceQueryTest extends PluggableProcessEngineTestCase {


  @Deployment(resources = "org/camunda/bpm/engine/test/api/oneTaskProcess.bpmn20.xml")
  public void testProcessVariableValueEqualsNumber() throws Exception {
    // long
    runtimeService.startProcessInstanceByKey("oneTaskProcess",
        Collections.<String, Object>singletonMap("var", 123L));

    // non-matching long
    runtimeService.startProcessInstanceByKey("oneTaskProcess",
        Collections.<String, Object>singletonMap("var", 12345L));

    // short
    runtimeService.startProcessInstanceByKey("oneTaskProcess",
        Collections.<String, Object>singletonMap("var", (short) 123));

    // double
    runtimeService.startProcessInstanceByKey("oneTaskProcess",
        Collections.<String, Object>singletonMap("var", 123.0d));

    // integer
    runtimeService.startProcessInstanceByKey("oneTaskProcess",
        Collections.<String, Object>singletonMap("var", 123));

    // untyped null (should not match)
    runtimeService.startProcessInstanceByKey("oneTaskProcess",
        Collections.<String, Object>singletonMap("var", null));

    // typed null (should not match)
    runtimeService.startProcessInstanceByKey("oneTaskProcess",
        Collections.<String, Object>singletonMap("var", Variables.longValue(null)));

    runtimeService.startProcessInstanceByKey("oneTaskProcess",
        Collections.<String, Object>singletonMap("var", "123"));

    assertEquals(4, historyService.createHistoricTaskInstanceQuery().processVariableValueEquals("var", Variables.numberValue(123)).count());
    assertEquals(4, historyService.createHistoricTaskInstanceQuery().processVariableValueEquals("var", Variables.numberValue(123L)).count());
    assertEquals(4, historyService.createHistoricTaskInstanceQuery().processVariableValueEquals("var", Variables.numberValue(123.0d)).count());
    assertEquals(4, historyService.createHistoricTaskInstanceQuery().processVariableValueEquals("var", Variables.numberValue((short) 123)).count());

    assertEquals(1, historyService.createHistoricTaskInstanceQuery().processVariableValueEquals("var", Variables.numberValue(null)).count());
  }

  @Deployment(resources = "org/camunda/bpm/engine/test/api/oneTaskProcess.bpmn20.xml")
  public void testTaskVariableValueEqualsNumber() throws Exception {
    runtimeService.startProcessInstanceByKey("oneTaskProcess");
    runtimeService.startProcessInstanceByKey("oneTaskProcess");
    runtimeService.startProcessInstanceByKey("oneTaskProcess");
    runtimeService.startProcessInstanceByKey("oneTaskProcess");
    runtimeService.startProcessInstanceByKey("oneTaskProcess");
    runtimeService.startProcessInstanceByKey("oneTaskProcess");
    runtimeService.startProcessInstanceByKey("oneTaskProcess");
    runtimeService.startProcessInstanceByKey("oneTaskProcess");

    List<Task> tasks = taskService.createTaskQuery().processDefinitionKey("oneTaskProcess").list();
    assertEquals(8, tasks.size());
    taskService.setVariableLocal(tasks.get(0).getId(), "var", 123L);
    taskService.setVariableLocal(tasks.get(1).getId(), "var", 12345L);
    taskService.setVariableLocal(tasks.get(2).getId(), "var", (short) 123);
    taskService.setVariableLocal(tasks.get(3).getId(), "var", 123.0d);
    taskService.setVariableLocal(tasks.get(4).getId(), "var", 123);
    taskService.setVariableLocal(tasks.get(5).getId(), "var", null);
    taskService.setVariableLocal(tasks.get(6).getId(), "var", Variables.longValue(null));
    taskService.setVariableLocal(tasks.get(7).getId(), "var", "123");

    assertEquals(4, historyService.createHistoricTaskInstanceQuery().taskVariableValueEquals("var", Variables.numberValue(123)).count());
    assertEquals(4, historyService.createHistoricTaskInstanceQuery().taskVariableValueEquals("var", Variables.numberValue(123L)).count());
    assertEquals(4, historyService.createHistoricTaskInstanceQuery().taskVariableValueEquals("var", Variables.numberValue(123.0d)).count());
    assertEquals(4, historyService.createHistoricTaskInstanceQuery().taskVariableValueEquals("var", Variables.numberValue((short) 123)).count());

    assertEquals(1, historyService.createHistoricTaskInstanceQuery().taskVariableValueEquals("var", Variables.numberValue(null)).count());
  }

}
