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

import static org.camunda.bpm.engine.history.UserOperationLogEntry.*;
import static org.camunda.bpm.engine.impl.persistence.entity.TaskEntity.*;

import java.util.HashMap;

import org.camunda.bpm.engine.history.UserOperationLogEntry;
import org.camunda.bpm.engine.history.UserOperationLogQuery;
import org.camunda.bpm.engine.impl.test.PluggableProcessEngineTestCase;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.task.DelegationState;
import org.camunda.bpm.engine.task.Task;
import org.camunda.bpm.engine.test.Deployment;

/**
 * @author Danny Gräf
 */
public class OperationLogTaskProcessTest extends PluggableProcessEngineTestCase {

  private ProcessInstance process;
  private Task task;

  @Deployment(resources = {"org/camunda/bpm/engine/test/history/oneTaskProcess.bpmn20.xml"})
  public void testCreateAndCompleteTask() {
    identityService.setAuthenticatedUserId("icke");
    startTestProcess();

    // expect: no entry for the task creation by process engine
    UserOperationLogQuery query = historyService.createUserOperationLogQuery();
    assertEquals(0, query.count());

    completeTestProcess();

    // expect: one entry for the task completion
    query = queryOperationDetails(OPERATION_TYPE_COMPLETE);
    assertEquals(1, query.count());
    UserOperationLogEntry complete = query.singleResult();
    assertEquals(DELETE, complete.getProperty());
    assertTrue(Boolean.parseBoolean(complete.getNewValue()));
  }

  @Deployment(resources = {"org/camunda/bpm/engine/test/history/oneTaskProcess.bpmn20.xml"})
  public void testAssignTask() {
    startTestProcess();

    // then: assign the task
    taskService.setAssignee(task.getId(), "icke");

    // expect: one entry for the task assignment
    UserOperationLogQuery query = queryOperationDetails(OPERATION_TYPE_ASSIGN);
    assertEquals(1, query.count());

    // assert: details
    UserOperationLogEntry assign = query.singleResult();
    assertEquals(ASSIGNEE, assign.getProperty());
    assertEquals("icke", assign.getNewValue());

    completeTestProcess();
  }

  @Deployment(resources = {"org/camunda/bpm/engine/test/history/oneTaskProcess.bpmn20.xml"})
  public void testChangeTaskOwner() {
    startTestProcess();

    // then: change the task owner
    taskService.setOwner(task.getId(), "icke");

    // expect: one entry for the owner change
    UserOperationLogQuery query = queryOperationDetails(OPERATION_TYPE_SET_OWNER);
    assertEquals(1, query.count());

    // assert: details
    UserOperationLogEntry change = query.singleResult();
    assertEquals(OWNER, change.getProperty());
    assertEquals("icke", change.getNewValue());

    completeTestProcess();
  }

  @Deployment(resources = {"org/camunda/bpm/engine/test/history/oneTaskProcess.bpmn20.xml"})
  public void testSetPriority() {
    startTestProcess();

    // then: set the priority of the task to 10
    taskService.setPriority(task.getId(), 10);

    // expect: one entry for the priority update
    UserOperationLogQuery query = queryOperationDetails(OPERATION_TYPE_SET_PRIORITY);
    assertEquals(1, query.count());

    // assert: correct priority set
    UserOperationLogEntry userOperationLogEntry = query.singleResult();
    assertEquals(PRIORITY, userOperationLogEntry.getProperty());
    // note: 50 is the default task priority
    assertEquals(50, Integer.parseInt(userOperationLogEntry.getOrgValue()));
    assertEquals(10, Integer.parseInt(userOperationLogEntry.getNewValue()));

    // then: set priority again
    taskService.setPriority(task.getId(), 75);

    // expect: one entry for the priority update
    query = queryOperationDetails(OPERATION_TYPE_SET_PRIORITY);
    assertEquals(2, query.count());

    // assert: correct priority set
    userOperationLogEntry = query.orderByTimestamp().asc().list().get(1);
    assertEquals(PRIORITY, userOperationLogEntry.getProperty());
    assertEquals(10, Integer.parseInt(userOperationLogEntry.getOrgValue()));
    assertEquals(75, Integer.parseInt(userOperationLogEntry.getNewValue()));
  }

  @Deployment(resources = {"org/camunda/bpm/engine/test/history/oneTaskProcess.bpmn20.xml"})
  public void testClaimTask() {
    startTestProcess();

    // then: claim a new the task
    taskService.claim(task.getId(), "icke");

    // expect: one entry for the claim
    UserOperationLogQuery query = queryOperationDetails(OPERATION_TYPE_CLAIM);
    assertEquals(1, query.count());

    // assert: details
    UserOperationLogEntry claim = query.singleResult();
    assertEquals(ASSIGNEE, claim.getProperty());
    assertEquals("icke", claim.getNewValue());

    completeTestProcess();
  }

  @Deployment(resources = {"org/camunda/bpm/engine/test/history/oneTaskProcess.bpmn20.xml"})
  public void testDelegateTask() {
    startTestProcess();

    // then: delegate the assigned task
    taskService.claim(task.getId(), "icke");
    taskService.delegateTask(task.getId(), "er");

    // expect: three entries for the delegation
    UserOperationLogQuery query = queryOperationDetails(OPERATION_TYPE_DELEGATE);
    assertEquals(3, query.count());

    // assert: details
    assertEquals("icke", queryOperationDetails(OPERATION_TYPE_DELEGATE, OWNER).singleResult().getNewValue());
    assertEquals("er", queryOperationDetails(OPERATION_TYPE_DELEGATE, ASSIGNEE).singleResult().getNewValue());
    assertEquals(DelegationState.PENDING.toString(), queryOperationDetails(OPERATION_TYPE_DELEGATE, DELEGATION).singleResult().getNewValue());

    completeTestProcess();
  }

  @Deployment(resources = {"org/camunda/bpm/engine/test/history/oneTaskProcess.bpmn20.xml"})
  public void testResolveTask() {
    startTestProcess();

    // then: resolve the task
    taskService.resolveTask(task.getId());

    // expect: one entry for the resolving
    UserOperationLogQuery query = queryOperationDetails(OPERATION_TYPE_RESOLVE);
    assertEquals(1, query.count());

    // assert: details
    assertEquals(DelegationState.RESOLVED.toString(), query.singleResult().getNewValue());

    completeTestProcess();
  }

  @Deployment(resources = {"org/camunda/bpm/engine/test/history/oneTaskProcess.bpmn20.xml"})
  public void testSubmitTaskForm_Complete() {
    startTestProcess();

    formService.submitTaskForm(task.getId(), new HashMap<String, Object>());

    // expect: two entries for the resolving (delegation and assignee changed)
    UserOperationLogQuery query = queryOperationDetails(OPERATION_TYPE_COMPLETE);
    assertEquals(1, query.count());

    // assert: delete
    assertFalse(Boolean.parseBoolean(query.property("delete").singleResult().getOrgValue()));
    assertTrue(Boolean.parseBoolean(query.property("delete").singleResult().getNewValue()));

    assertProcessEnded(process.getId());
  }

  @Deployment(resources = {"org/camunda/bpm/engine/test/history/oneTaskProcess.bpmn20.xml"})
  public void testSubmitTaskForm_Resolve() {
    startTestProcess();

    taskService.delegateTask(task.getId(), "demo");

    formService.submitTaskForm(task.getId(), new HashMap<String, Object>());

    // expect: two entries for the resolving (delegation and assignee changed)
    UserOperationLogQuery query = queryOperationDetails(OPERATION_TYPE_RESOLVE);
    assertEquals(2, query.count());

    // assert: delegation
    assertEquals(DelegationState.PENDING.toString(), query.property("delegation").singleResult().getOrgValue());
    assertEquals(DelegationState.RESOLVED.toString(), query.property("delegation").singleResult().getNewValue());

    // assert: assignee
    assertEquals("demo", query.property("assignee").singleResult().getOrgValue());
    assertEquals(null, query.property("assignee").singleResult().getNewValue());

    completeTestProcess();
  }

  private void startTestProcess() {
    process = runtimeService.startProcessInstanceByKey("oneTaskProcess");
    task = taskService.createTaskQuery().singleResult();
  }

  private UserOperationLogQuery queryOperationDetails(String type) {
    return historyService.createUserOperationLogQuery().operationType(type);
  }

  private UserOperationLogQuery queryOperationDetails(String type, String property) {
    return historyService.createUserOperationLogQuery().operationType(type).property(property);
  }

  private void completeTestProcess() {
    taskService.complete(task.getId());
    assertProcessEnded(process.getId());
  }

}
