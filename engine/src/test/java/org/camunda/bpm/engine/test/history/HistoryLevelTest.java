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
package org.camunda.bpm.engine.test.history;

import org.camunda.bpm.engine.impl.test.PluggableProcessEngineTestCase;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.task.Task;
import org.camunda.bpm.engine.test.Deployment;

import static org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl.*;

/**
 * validates history level settings, see CAM-1844
 * @author Danny Gräf
 */
public abstract class HistoryLevelTest extends PluggableProcessEngineTestCase {

  private ProcessInstance startProcessAndCompleteUserTask() {
    ProcessInstance process = runtimeService.startProcessInstanceByKey("HistoryLevelTest");
    Task task = taskService.createTaskQuery().singleResult();
    taskService.setAssignee(task.getId(), "icke");
    taskService.complete(task.getId());
    return process;
  }

  @Deployment(resources = {"org/camunda/bpm/engine/test/history/HistoryLevelTest.bpmn20.xml"})
  public void testLevelNone() {
    processEngineConfiguration.setHistoryLevel(HISTORYLEVEL_NONE);
    ProcessInstance process = startProcessAndCompleteUserTask();
    assertProcessEnded(process.getId());

    assertEquals(5, historyService.createHistoricActivityInstanceQuery().count()); // FIXME should be 0
    assertEquals(0, historyService.createHistoricDetailQuery().count());
    assertEquals(0, historyService.createHistoricProcessInstanceQuery().count());
    assertEquals(0, historyService.createHistoricTaskInstanceQuery().count());
    assertEquals(0, historyService.createHistoricVariableInstanceQuery().count());

    // fails because historic activity instances are not removed
  }

  @Deployment(resources = {"org/camunda/bpm/engine/test/history/HistoryLevelTest.bpmn20.xml"})
  public void testLevelActivity() {
    processEngineConfiguration.setHistoryLevel(HISTORYLEVEL_ACTIVITY);
    ProcessInstance process = startProcessAndCompleteUserTask();
    assertProcessEnded(process.getId());

    assertEquals(5, historyService.createHistoricActivityInstanceQuery().count());
    assertEquals(0, historyService.createHistoricDetailQuery().count());
    assertEquals(1, historyService.createHistoricProcessInstanceQuery().count());
    assertEquals(0, historyService.createHistoricTaskInstanceQuery().count());
    assertEquals(0, historyService.createHistoricVariableInstanceQuery().count());

    // fails because historic comments are not removed
  }

  @Deployment(resources = {"org/camunda/bpm/engine/test/history/HistoryLevelTest.bpmn20.xml"})
  public void testLevelAudit() {
    processEngineConfiguration.setHistoryLevel(HISTORYLEVEL_AUDIT);
    ProcessInstance process = startProcessAndCompleteUserTask();
    assertProcessEnded(process.getId());

    assertEquals(5, historyService.createHistoricActivityInstanceQuery().count());
    assertEquals(0, historyService.createHistoricDetailQuery().count());
    assertEquals(1, historyService.createHistoricProcessInstanceQuery().count());
    assertEquals(1, historyService.createHistoricTaskInstanceQuery().count());
    assertEquals(1, historyService.createHistoricVariableInstanceQuery().count());
  }

  @Deployment(resources = {"org/camunda/bpm/engine/test/history/HistoryLevelTest.bpmn20.xml"})
  public void testLevelFull() {
    processEngineConfiguration.setHistoryLevel(HISTORYLEVEL_FULL);
    ProcessInstance process = startProcessAndCompleteUserTask();
    assertProcessEnded(process.getId());

    assertEquals(5, historyService.createHistoricActivityInstanceQuery().count());
    assertEquals(2, historyService.createHistoricDetailQuery().count());
    assertEquals(1, historyService.createHistoricProcessInstanceQuery().count());
    assertEquals(1, historyService.createHistoricTaskInstanceQuery().count());
    assertEquals(1, historyService.createHistoricVariableInstanceQuery().count());
  }
}
