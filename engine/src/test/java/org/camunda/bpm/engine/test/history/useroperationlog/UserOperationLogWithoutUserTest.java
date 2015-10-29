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
package org.camunda.bpm.engine.test.history.useroperationlog;

import org.camunda.bpm.engine.history.UserOperationLogQuery;
import org.camunda.bpm.engine.impl.test.PluggableProcessEngineTestCase;
import org.camunda.bpm.engine.task.Task;
import org.camunda.bpm.engine.test.Deployment;

/**
 * @author Roman Smirnov
 *
 */
public class UserOperationLogWithoutUserTest extends PluggableProcessEngineTestCase {

  protected static final String PROCESS_PATH = "org/camunda/bpm/engine/test/api/oneTaskProcess.bpmn20.xml";
  protected static final String PROCESS_KEY = "oneTaskProcess";

  @Deployment(resources = PROCESS_PATH)
  public void testCompleteTask() {
    // given
    runtimeService.startProcessInstanceByKey(PROCESS_KEY);
    String taskId = taskService.createTaskQuery().singleResult().getId();

    // when
    taskService.complete(taskId);

    // then
    verifyNoUserOperationLogged();
  }

  @Deployment(resources = PROCESS_PATH)
  public void testAssignTask() {
    // given
    runtimeService.startProcessInstanceByKey(PROCESS_KEY);
    String taskId = taskService.createTaskQuery().singleResult().getId();

    // when
    taskService.setAssignee(taskId, "demo");

    // then
    verifyNoUserOperationLogged();
  }

  @Deployment(resources = PROCESS_PATH)
  public void testClaimTask() {
    // given
    runtimeService.startProcessInstanceByKey(PROCESS_KEY);
    String taskId = taskService.createTaskQuery().singleResult().getId();

    // when
    taskService.claim(taskId, "demo");

    // then
    verifyNoUserOperationLogged();
  }

  public void testCreateTask() {
    // when
    Task task = taskService.newTask("a-task-id");
    taskService.saveTask(task);

    // then
    verifyNoUserOperationLogged();

    taskService.deleteTask("a-task-id", true);
  }

  @Deployment(resources = PROCESS_PATH)
  public void testDelegateTask() {
    // given
    runtimeService.startProcessInstanceByKey(PROCESS_KEY);
    String taskId = taskService.createTaskQuery().singleResult().getId();

    // when
    taskService.delegateTask(taskId, "demo");

    // then
    verifyNoUserOperationLogged();
  }

  @Deployment(resources = PROCESS_PATH)
  public void testResolveTask() {
    // given
    runtimeService.startProcessInstanceByKey(PROCESS_KEY);
    String taskId = taskService.createTaskQuery().singleResult().getId();

    // when
    taskService.resolveTask(taskId);

    // then
    verifyNoUserOperationLogged();
  }

  @Deployment(resources = PROCESS_PATH)
  public void testSetOwnerTask() {
    // given
    runtimeService.startProcessInstanceByKey(PROCESS_KEY);
    String taskId = taskService.createTaskQuery().singleResult().getId();

    // when
    taskService.setOwner(taskId, "demo");

    // then
    verifyNoUserOperationLogged();
  }

  @Deployment(resources = PROCESS_PATH)
  public void testSetPriorityTask() {
    // given
    runtimeService.startProcessInstanceByKey(PROCESS_KEY);
    String taskId = taskService.createTaskQuery().singleResult().getId();

    // when
    taskService.setPriority(taskId, 60);

    // then
    verifyNoUserOperationLogged();
  }

  @Deployment(resources = PROCESS_PATH)
  public void testUpdateTask() {
    // given
    runtimeService.startProcessInstanceByKey(PROCESS_KEY);
    Task task = taskService.createTaskQuery().singleResult();
    task.setCaseInstanceId("a-case-instance-id");

    // when
    taskService.saveTask(task);

    // then
    verifyNoUserOperationLogged();
  }

  @Deployment(resources = PROCESS_PATH)
  public void testActivateProcessInstance() {
    // given
    String id = runtimeService.startProcessInstanceByKey(PROCESS_KEY).getId();

    // when
    runtimeService.activateProcessInstanceById(id);

    // then
    verifyNoUserOperationLogged();
  }

  @Deployment(resources = PROCESS_PATH)
  public void testSuspendProcessInstance() {
    // given
    String id = runtimeService.startProcessInstanceByKey(PROCESS_KEY).getId();

    // when
    runtimeService.suspendProcessInstanceById(id);

    // then
    verifyNoUserOperationLogged();
  }

  @Deployment(resources = "org/camunda/bpm/engine/test/api/runtime/oneFailingServiceProcess.bpmn20.xml")
  public void testActivateJobDefinition() {
    // given
    runtimeService.startProcessInstanceByKey("oneFailingServiceTaskProcess");
    String id = managementService.createJobDefinitionQuery().singleResult().getId();

    // when
    managementService.activateJobByJobDefinitionId(id);

    // then
    verifyNoUserOperationLogged();
  }

  @Deployment(resources = "org/camunda/bpm/engine/test/api/runtime/oneFailingServiceProcess.bpmn20.xml")
  public void testSuspendJobDefinition() {
    // given
    runtimeService.startProcessInstanceByKey("oneFailingServiceTaskProcess");
    String id = managementService.createJobDefinitionQuery().singleResult().getId();

    // when
    managementService.suspendJobByJobDefinitionId(id);

    // then
    verifyNoUserOperationLogged();
  }

  @Deployment(resources = "org/camunda/bpm/engine/test/api/runtime/oneFailingServiceProcess.bpmn20.xml")
  public void testActivateJob() {
    // given
    runtimeService.startProcessInstanceByKey("oneFailingServiceTaskProcess");
    String id = managementService.createJobQuery().singleResult().getId();

    // when
    managementService.activateJobById(id);

    // then
    verifyNoUserOperationLogged();
  }

  @Deployment(resources = "org/camunda/bpm/engine/test/api/runtime/oneFailingServiceProcess.bpmn20.xml")
  public void testSuspendJob() {
    // given
    runtimeService.startProcessInstanceByKey("oneFailingServiceTaskProcess");
    String id = managementService.createJobQuery().singleResult().getId();

    // when
    managementService.suspendJobById(id);

    // then
    verifyNoUserOperationLogged();
  }

  @Deployment(resources = "org/camunda/bpm/engine/test/api/runtime/oneFailingServiceProcess.bpmn20.xml")
  public void testSetJobRetries() {
    // given
    runtimeService.startProcessInstanceByKey("oneFailingServiceTaskProcess");
    String id = managementService.createJobQuery().singleResult().getId();

    // when
    managementService.setJobRetries(id, 5);

    // then
    verifyNoUserOperationLogged();
  }

  @Deployment(resources = PROCESS_PATH)
  public void testActivateProcessDefinition() {
    // when
    repositoryService.activateProcessDefinitionByKey(PROCESS_KEY);

    // then
    verifyNoUserOperationLogged();
  }

  @Deployment(resources = PROCESS_PATH)
  public void testSuspendProcessDefinition() {
    // when
    repositoryService.suspendProcessDefinitionByKey(PROCESS_KEY);

    // then
    verifyNoUserOperationLogged();
  }

  @Deployment(resources = PROCESS_PATH)
  public void testModifyProcessInstance() {
    // given
    String id = runtimeService.startProcessInstanceByKey(PROCESS_KEY).getId();

    // when
    runtimeService
      .createProcessInstanceModification(id)
      .cancelAllForActivity("theTask")
      .execute();

    // then
    verifyNoUserOperationLogged();
  }

  @Deployment(resources = PROCESS_PATH)
  public void testSetVariable() {
    // given
    String id = runtimeService.startProcessInstanceByKey(PROCESS_KEY).getId();

    // when
    runtimeService.setVariable(id, "aVariable", "aValue");

    // then
    verifyNoUserOperationLogged();
  }

  @Deployment(resources = PROCESS_PATH)
  public void testRemoveVariable() {
    // given
    String id = runtimeService.startProcessInstanceByKey(PROCESS_KEY).getId();
    runtimeService.setVariable(id, "aVariable", "aValue");

    // when
    runtimeService.removeVariable(id, "aVariable");

    // then
    verifyNoUserOperationLogged();
  }

  protected void verifyNoUserOperationLogged() {
    UserOperationLogQuery query = historyService.createUserOperationLogQuery();
    assertEquals(0, query.count());
  }

}
