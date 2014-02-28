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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.camunda.bpm.engine.history.HistoricActivityInstance;
import org.camunda.bpm.engine.history.HistoricVariableInstance;
import org.camunda.bpm.engine.history.HistoricVariableInstanceQuery;
import org.camunda.bpm.engine.impl.persistence.entity.ExecutionEntity;
import org.camunda.bpm.engine.impl.test.PluggableProcessEngineTestCase;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.task.Task;
import org.camunda.bpm.engine.test.Deployment;

/**
 * @author Roman Smirnov
 *
 */
public class HistoricVariableInstanceScopeTest extends PluggableProcessEngineTestCase {

  @Deployment(resources={"org/camunda/bpm/engine/test/api/oneTaskProcess.bpmn20.xml"})
  public void testSetVariableOnProcessInstanceStart() {
    Map<String, Object> variables = new HashMap<String, Object>();
    variables.put("testVar", "testValue");
    ProcessInstance pi = runtimeService.startProcessInstanceByKey("oneTaskProcess", variables);

    HistoricVariableInstanceQuery query = historyService.createHistoricVariableInstanceQuery();
    assertEquals(1, query.count());

    HistoricVariableInstance variable = query.singleResult();
    assertNotNull(variable);

    // the variable is in the process instance scope
    assertEquals(pi.getId(), variable.getActivtyInstanceId());

    taskService.complete(taskService.createTaskQuery().singleResult().getId());
    assertProcessEnded(pi.getId());
  }

  @Deployment(resources={"org/camunda/bpm/engine/test/api/oneTaskProcess.bpmn20.xml"})
  public void testSetVariableLocalOnUserTask() {
    ProcessInstance pi = runtimeService.startProcessInstanceByKey("oneTaskProcess");

    Task task = taskService.createTaskQuery().singleResult();
    assertNotNull(task);

    taskService.setVariableLocal(task.getId(), "testVar", "testValue");
    ExecutionEntity taskExecution = (ExecutionEntity) runtimeService.createExecutionQuery()
        .executionId(task.getExecutionId())
        .singleResult();
    assertNotNull(taskExecution);

    HistoricVariableInstanceQuery query = historyService.createHistoricVariableInstanceQuery();
    assertEquals(1, query.count());

    HistoricVariableInstance variable = query.singleResult();
    assertNotNull(variable);

    // the variable is in the task scope
    assertEquals(taskExecution.getActivityInstanceId(), variable.getActivtyInstanceId());

    taskService.complete(task.getId());
    assertProcessEnded(pi.getId());
  }

  @Deployment(resources={"org/camunda/bpm/engine/test/api/oneTaskProcess.bpmn20.xml"})
  public void testSetVariableOnProcessIntanceStartAndSetVariableLocalOnUserTask() {
    Map<String, Object> variables = new HashMap<String, Object>();
    variables.put("testVar", "testValue");
    ProcessInstance pi = runtimeService.startProcessInstanceByKey("oneTaskProcess", variables);

    Task task = taskService.createTaskQuery().singleResult();
    assertNotNull(task);

    taskService.setVariableLocal(task.getId(), "testVar", "anotherTestValue");
    ExecutionEntity taskExecution = (ExecutionEntity) runtimeService.createExecutionQuery().singleResult();
    assertNotNull(taskExecution);

    HistoricVariableInstanceQuery query = historyService.createHistoricVariableInstanceQuery();
    assertEquals(2, query.count());

    List<HistoricVariableInstance> result = query.list();

    HistoricVariableInstance firstVar = result.get(0);
    assertEquals("testVar", firstVar.getVariableName());
    assertEquals("testValue", firstVar.getValue());
    // the variable is in the process instance scope
    assertEquals(pi.getId(), firstVar.getActivtyInstanceId());

    HistoricVariableInstance secondVar = result.get(1);
    assertEquals("testVar", secondVar.getVariableName());
    assertEquals("anotherTestValue", secondVar.getValue());
    // the variable is in the task scope
    assertEquals(taskExecution.getActivityInstanceId(), secondVar.getActivtyInstanceId());

    taskService.complete(task.getId());
    assertProcessEnded(pi.getId());
  }

  @Deployment(resources={"org/camunda/bpm/engine/test/api/oneSubProcess.bpmn20.xml"})
  public void testSetVariableOnUserTaskInsideSubProcess() {
    ProcessInstance pi = runtimeService.startProcessInstanceByKey("startSimpleSubProcess");

    Task task = taskService.createTaskQuery().singleResult();
    assertNotNull(task);

    taskService.setVariable(task.getId(), "testVar", "testValue");

    HistoricVariableInstanceQuery query = historyService.createHistoricVariableInstanceQuery();
    assertEquals(1, query.count());

    HistoricVariableInstance variable = query.singleResult();
    // the variable is in the process instance scope
    assertEquals(pi.getId(), variable.getActivtyInstanceId());

    taskService.complete(task.getId());
    assertProcessEnded(pi.getId());
  }

  @Deployment
  public void testSetVariableOnServiceTaskInsideSubProcess() {
    ProcessInstance pi = runtimeService.startProcessInstanceByKey("process");

    HistoricVariableInstanceQuery query = historyService.createHistoricVariableInstanceQuery();
    assertEquals(1, query.count());

    HistoricVariableInstance variable = query.singleResult();
    // the variable is in the process instance scope
    assertEquals(pi.getId(), variable.getActivtyInstanceId());

    assertProcessEnded(pi.getId());
  }

  @Deployment
  public void testSetVariableLocalOnServiceTaskInsideSubProcess() {
    ProcessInstance pi = runtimeService.startProcessInstanceByKey("process");

    HistoricVariableInstanceQuery query = historyService.createHistoricVariableInstanceQuery();
    assertEquals(1, query.count());

    String activityInstanceId = historyService.createHistoricActivityInstanceQuery()
        .activityId("SubProcess_1")
        .singleResult()
        .getId();

    HistoricVariableInstance variable = query.singleResult();
    // the variable is in the sub process scope
    assertEquals(activityInstanceId, variable.getActivtyInstanceId());

    assertProcessEnded(pi.getId());
  }

  @Deployment
  public void testSetVariableLocalOnTaskInsideParallelBranch() {
    ProcessInstance pi = runtimeService.startProcessInstanceByKey("process");

    Task task = taskService.createTaskQuery().singleResult();
    assertNotNull(task);

    taskService.setVariableLocal(task.getId(), "testVar", "testValue");
    ExecutionEntity taskExecution = (ExecutionEntity) runtimeService.createExecutionQuery()
        .executionId(task.getExecutionId())
        .singleResult();
    assertNotNull(taskExecution);

    HistoricVariableInstanceQuery query = historyService.createHistoricVariableInstanceQuery();
    assertEquals(1, query.count());

    HistoricVariableInstance variable = query.singleResult();
    // the variable is in the user task scope
    assertEquals(taskExecution.getActivityInstanceId(), variable.getActivtyInstanceId());

    taskService.complete(task.getId());

    assertProcessEnded(pi.getId());
  }

  @Deployment(resources={"org/camunda/bpm/engine/test/history/HistoricVariableInstanceScopeTest.testSetVariableLocalOnTaskInsideParallelBranch.bpmn"})
  public void testSetVariableOnTaskInsideParallelBranch() {
    ProcessInstance pi = runtimeService.startProcessInstanceByKey("process");

    Task task = taskService.createTaskQuery().singleResult();
    assertNotNull(task);

    taskService.setVariable(task.getId(), "testVar", "testValue");

    HistoricVariableInstanceQuery query = historyService.createHistoricVariableInstanceQuery();
    assertEquals(1, query.count());

    HistoricVariableInstance variable = query.singleResult();
    // the variable is in the process instance scope
    assertEquals(pi.getId(), variable.getActivtyInstanceId());

    taskService.complete(task.getId());

    assertProcessEnded(pi.getId());
  }

  @Deployment
  public void testSetVariableOnServiceTaskInsideParallelBranch() {
    ProcessInstance pi = runtimeService.startProcessInstanceByKey("process");

    HistoricVariableInstanceQuery query = historyService.createHistoricVariableInstanceQuery();
    assertEquals(1, query.count());

    HistoricVariableInstance variable = query.singleResult();
    // the variable is in the process instance scope
    assertEquals(pi.getId(), variable.getActivtyInstanceId());

    assertProcessEnded(pi.getId());
  }

  @Deployment
  public void testSetVariableLocalOnServiceTaskInsideParallelBranch() {
    ProcessInstance pi = runtimeService.startProcessInstanceByKey("process");

    HistoricActivityInstance serviceTask = historyService.createHistoricActivityInstanceQuery()
        .activityId("serviceTask1")
        .singleResult();
    assertNotNull(serviceTask);

    HistoricVariableInstanceQuery query = historyService.createHistoricVariableInstanceQuery();
    assertEquals(1, query.count());

    HistoricVariableInstance variable = query.singleResult();
    // the variable is in the service task scope
    assertEquals(serviceTask.getId(), variable.getActivtyInstanceId());

    assertProcessEnded(pi.getId());
  }
}
