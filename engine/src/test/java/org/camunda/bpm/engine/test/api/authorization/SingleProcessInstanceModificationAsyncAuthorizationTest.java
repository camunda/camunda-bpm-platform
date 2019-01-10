/*
 * Copyright © 2013-2019 camunda services GmbH and various authors (info@camunda.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.camunda.bpm.engine.test.api.authorization;

import static org.camunda.bpm.engine.authorization.Authorization.ANY;
import static org.camunda.bpm.engine.authorization.Permissions.CREATE;
import static org.camunda.bpm.engine.authorization.BatchPermissions.CREATE_BATCH_MODIFY_PROCESS_INSTANCES;
import static org.camunda.bpm.engine.authorization.Permissions.CREATE_INSTANCE;
import static org.camunda.bpm.engine.authorization.Permissions.READ_INSTANCE;
import static org.camunda.bpm.engine.authorization.Permissions.UPDATE_INSTANCE;
import static org.camunda.bpm.engine.authorization.Resources.BATCH;
import static org.camunda.bpm.engine.authorization.Resources.PROCESS_DEFINITION;
import static org.camunda.bpm.engine.authorization.Resources.PROCESS_INSTANCE;
import static org.camunda.bpm.engine.test.util.ActivityInstanceAssert.assertThat;
import static org.camunda.bpm.engine.test.util.ActivityInstanceAssert.describeActivityInstanceTree;
import static org.camunda.bpm.engine.test.util.ExecutionAssert.assertThat;
import static org.camunda.bpm.engine.test.util.ExecutionAssert.describeExecutionTree;

import java.util.List;

import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.batch.Batch;
import org.camunda.bpm.engine.repository.ProcessDefinition;
import org.camunda.bpm.engine.runtime.ActivityInstance;
import org.camunda.bpm.engine.runtime.Job;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.task.Task;
import org.camunda.bpm.engine.test.Deployment;
import org.camunda.bpm.engine.test.util.ExecutionTree;

public class SingleProcessInstanceModificationAsyncAuthorizationTest extends AuthorizationTest {

  protected static final String PARALLEL_GATEWAY_PROCESS = "org/camunda/bpm/engine/test/api/runtime/ProcessInstanceModificationTest.parallelGateway.bpmn20.xml";

  @Override
  public void tearDown() {
    disableAuthorization();
    List<Batch> batches = managementService.createBatchQuery().list();
    for (Batch batch : batches) {
      managementService.deleteBatch(batch.getId(), true);
    }

    List<Job> jobs = managementService.createJobQuery().list();
    for (Job job : jobs) {
      managementService.deleteJob(job.getId());
    }
    enableAuthorization();
    super.tearDown();
  }

  @Deployment(resources = PARALLEL_GATEWAY_PROCESS)
  public void testModificationWithAllPermissions() {
    // given
    createGrantAuthorization(PROCESS_DEFINITION, "parallelGateway", userId, CREATE_INSTANCE, READ_INSTANCE, UPDATE_INSTANCE);
    createGrantAuthorization(PROCESS_INSTANCE, ANY, userId, CREATE);
    createGrantAuthorization(BATCH, ANY, userId, CREATE);

    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("parallelGateway");
    String processInstanceId = processInstance.getId();
    String processDefinitionId = processInstance.getProcessDefinitionId();
    disableAuthorization();
    ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery().processDefinitionId(processDefinitionId).singleResult();
    enableAuthorization();

    ActivityInstance tree = runtimeService.getActivityInstance(processInstance.getId());
    // when
    Batch modificationBatch = runtimeService
        .createProcessInstanceModification(processInstance.getId())
        .cancelActivityInstance(getInstanceIdForActivity(tree, "task1"))
        .executeAsync();
    assertNotNull(modificationBatch);
    Job job = managementService.createJobQuery().jobDefinitionId(modificationBatch.getSeedJobDefinitionId()).singleResult();
    // seed job
    managementService.executeJob(job.getId());

    // then
    for (Job pending : managementService.createJobQuery().jobDefinitionId(modificationBatch.getBatchJobDefinitionId()).list()) {
      managementService.executeJob(pending.getId());
      assertEquals(processDefinition.getDeploymentId(), pending.getDeploymentId());
    }

    ActivityInstance updatedTree = runtimeService.getActivityInstance(processInstanceId);
    assertNotNull(updatedTree);
    assertEquals(processInstanceId, updatedTree.getProcessInstanceId());

    assertThat(updatedTree).hasStructure(describeActivityInstanceTree(processInstance.getProcessDefinitionId()).activity("task2").done());

    ExecutionTree executionTree = ExecutionTree.forExecution(processInstanceId, processEngine);

    assertThat(executionTree).matches(describeExecutionTree("task2").scope().done());

    // complete the process
    disableAuthorization();
    completeTasksInOrder("task2");
    assertProcessEnded(processInstanceId);
    enableAuthorization();
  }

  @Deployment(resources = PARALLEL_GATEWAY_PROCESS)
  public void testModificationWithoutBatchPermissions() {
    // given
    createGrantAuthorization(PROCESS_DEFINITION, "parallelGateway", userId, CREATE_INSTANCE, READ_INSTANCE, UPDATE_INSTANCE);
    createGrantAuthorization(PROCESS_INSTANCE, ANY, userId, CREATE);

    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("parallelGateway");

    ActivityInstance tree = runtimeService.getActivityInstance(processInstance.getId());
    try {
      // when
      runtimeService
        .createProcessInstanceModification(processInstance.getId())
        .cancelActivityInstance(getInstanceIdForActivity(tree, "task1"))
        .executeAsync();
      fail("expected exception");
    } catch (ProcessEngineException e) {
      // then
      assertTrue(e.getMessage().contains("The user with id 'test' does not have"));
      assertTrue(e.getMessage().contains("'CREATE' permission on resource 'Batch'"));
      assertTrue(e.getMessage().contains("'CREATE_BATCH_MODIFY_PROCESS_INSTANCES' permission on resource 'Batch'"));
    }
  }

  @Deployment(resources = PARALLEL_GATEWAY_PROCESS)
  public void testModificationRevoke() {
    // given
    createGrantAuthorization(PROCESS_DEFINITION, "parallelGateway", userId, CREATE_INSTANCE, READ_INSTANCE, UPDATE_INSTANCE);
    createGrantAuthorization(PROCESS_INSTANCE, ANY, userId, CREATE);
    createGrantAuthorization(BATCH, ANY, userId, CREATE);
    createRevokeAuthorization(BATCH, ANY, userId, CREATE_BATCH_MODIFY_PROCESS_INSTANCES);

    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("parallelGateway");

    ActivityInstance tree = runtimeService.getActivityInstance(processInstance.getId());
    try {
      // when
      runtimeService
        .createProcessInstanceModification(processInstance.getId())
        .cancelActivityInstance(getInstanceIdForActivity(tree, "task1"))
        .executeAsync();
      fail("expected exception");
    } catch (ProcessEngineException e) {
      // then
      assertTrue(e.getMessage().contains("The user with id 'test' does not have"));
      assertTrue(e.getMessage().contains("'CREATE' permission on resource 'Batch'"));
      assertTrue(e.getMessage().contains("'CREATE_BATCH_MODIFY_PROCESS_INSTANCES' permission on resource 'Batch'"));
    }
  }

  protected String getInstanceIdForActivity(ActivityInstance activityInstance, String activityId) {
    ActivityInstance instance = getChildInstanceForActivity(activityInstance, activityId);
    if (instance != null) {
      return instance.getId();
    }
    return null;
  }


  protected ActivityInstance getChildInstanceForActivity(ActivityInstance activityInstance, String activityId) {
    if (activityId.equals(activityInstance.getActivityId())) {
      return activityInstance;
    }

    for (ActivityInstance childInstance : activityInstance.getChildActivityInstances()) {
      ActivityInstance instance = getChildInstanceForActivity(childInstance, activityId);
      if (instance != null) {
        return instance;
      }
    }

    return null;
  }

  protected void completeTasksInOrder(String... taskNames) {
    for (String taskName : taskNames) {
      // complete any task with that name
      List<Task> tasks = taskService.createTaskQuery().taskDefinitionKey(taskName).listPage(0, 1);
      assertTrue("task for activity " + taskName + " does not exist", !tasks.isEmpty());
      taskService.complete(tasks.get(0).getId());
    }
  }

}
