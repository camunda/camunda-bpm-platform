/*
 * Copyright Camunda Services GmbH and/or licensed to Camunda Services GmbH
 * under one or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information regarding copyright
 * ownership. Camunda licenses this file to you under the Apache License,
 * Version 2.0; you may not use this file except in compliance with the License.
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
package org.camunda.bpm.engine.test.api.multitenancy.tenantcheck;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.util.Arrays;
import java.util.List;

import org.camunda.bpm.engine.ExternalTaskService;
import org.camunda.bpm.engine.IdentityService;
import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.TaskService;
import org.camunda.bpm.engine.externaltask.LockedExternalTask;
import org.camunda.bpm.engine.test.ProcessEngineRule;
import org.camunda.bpm.engine.test.util.ProcessEngineTestRule;
import org.camunda.bpm.engine.test.util.ProvidedProcessEngineRule;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;

public class MultiTenancyExternalTaskCmdsTenantCheckTest {

  protected static final String TENANT_ONE = "tenant1";
  protected static final String TENANT_TWO = "tenant2";

  protected static final String PROCESS_DEFINITION_KEY = "twoExternalTaskProcess";
  protected static final String PROCESS_DEFINITION_KEY_ONE = "oneExternalTaskProcess";
  private static final String ERROR_DETAILS = "anErrorDetail";

  protected ProcessEngineRule engineRule = new ProvidedProcessEngineRule();

  protected ProcessEngineTestRule testRule = new ProcessEngineTestRule(engineRule);

  protected static final String WORKER_ID = "aWorkerId";

  protected static final long LOCK_TIME = 10000L;

  protected static final String TOPIC_NAME = "externalTaskTopic";

  protected static final String ERROR_MESSAGE = "errorMessage";

  protected ExternalTaskService externalTaskService;

  protected TaskService taskService;

  protected String processInstanceId;

  protected IdentityService identityService;

  @Rule
  public RuleChain ruleChain = RuleChain.outerRule(engineRule).around(testRule);

  @Before
  public void init() {

    externalTaskService = engineRule.getExternalTaskService();

    taskService = engineRule.getTaskService();

    identityService = engineRule.getIdentityService();

    testRule.deployForTenant(TENANT_ONE,
      "org/camunda/bpm/engine/test/api/externaltask/twoExternalTaskProcess.bpmn20.xml");

    processInstanceId = engineRule.getRuntimeService().startProcessInstanceByKey(PROCESS_DEFINITION_KEY).getId();

  }

  // fetch and lock test cases
  @Test
  public void testFetchAndLockWithAuthenticatedTenant() {

    identityService.setAuthentication("aUserId", null,  Arrays.asList(TENANT_ONE));

    // then
    List<LockedExternalTask> externalTasks = externalTaskService.fetchAndLock(1, WORKER_ID)
      .topic(TOPIC_NAME, LOCK_TIME)
      .execute();
    assertEquals(1, externalTasks.size());

  }

  @Test
  public void testFetchAndLockWithNoAuthenticatedTenant() {

    identityService.setAuthentication("aUserId", null);

    // then external task cannot be fetched due to the absence of tenant Id authentication
    List<LockedExternalTask> externalTasks = externalTaskService.fetchAndLock(1, WORKER_ID)
      .topic(TOPIC_NAME, LOCK_TIME)
      .execute();
    assertEquals(0, externalTasks.size());

  }

  @Test
  public void testFetchAndLockWithDifferentTenant() {

    identityService.setAuthentication("aUserId", null, Arrays.asList("tenantTwo"));

    // then external task cannot be fetched due to the absence of 'tenant1' authentication
    List<LockedExternalTask> externalTasks = externalTaskService.fetchAndLock(1, WORKER_ID)
      .topic(TOPIC_NAME, LOCK_TIME)
      .execute();
    assertEquals(0, externalTasks.size());

  }

  @Test
  public void testFetchAndLockWithDisabledTenantCheck() {

    identityService.setAuthentication("aUserId", null);
    engineRule.getProcessEngineConfiguration().setTenantCheckEnabled(false);
    // then
    List<LockedExternalTask> externalTasks = externalTaskService.fetchAndLock(1, WORKER_ID)
      .topic(TOPIC_NAME, LOCK_TIME)
      .execute();
    assertEquals(1, externalTasks.size());

  }

  @Test
  public void testFetchAndLockWithoutTenantId() {
    // given
    identityService.setAuthentication("aUserId", null,  Arrays.asList(TENANT_ONE));

    // when
    List<LockedExternalTask> externalTasks = externalTaskService.fetchAndLock(1, WORKER_ID)
      .topic(TOPIC_NAME, LOCK_TIME)
      .withoutTenantId()
      .execute();

    // then
    assertEquals(0, externalTasks.size());
  }

  @Test
  public void testFetchAndLockWithTenantId() {
    // given
    testRule.deploy("org/camunda/bpm/engine/test/api/externaltask/oneExternalTaskProcess.bpmn20.xml");
    engineRule.getRuntimeService().startProcessInstanceByKey(PROCESS_DEFINITION_KEY_ONE).getId();
    identityService.setAuthentication("aUserId", null,  Arrays.asList(TENANT_ONE));

    // when
    List<LockedExternalTask> externalTasks = externalTaskService.fetchAndLock(1, WORKER_ID)
      .topic(TOPIC_NAME, LOCK_TIME)
      .tenantIdIn(TENANT_ONE)
      .execute();

    // then
    assertEquals(1, externalTasks.size());
  }

  @Test
  public void testFetchAndLockWithTenantIdIn() {

    // when
    List<LockedExternalTask> externalTasks = externalTaskService.fetchAndLock(1, WORKER_ID)
      .topic(TOPIC_NAME, LOCK_TIME)
      .tenantIdIn(TENANT_ONE, TENANT_TWO)
      .execute();

    // then
    assertEquals(1, externalTasks.size());
  }

  @Test
  public void testFetchAndLockWithTenantIdInTwoTenants() {
    // given
    testRule.deploy("org/camunda/bpm/engine/test/api/externaltask/twoExternalTaskWithPriorityProcess.bpmn20.xml");
    engineRule.getRuntimeService().startProcessInstanceByKey("twoExternalTaskWithPriorityProcess").getId();
    testRule.deployForTenant(TENANT_TWO,
        "org/camunda/bpm/engine/test/api/externaltask/oneExternalTaskProcess.bpmn20.xml");
    String instanceId = engineRule.getRuntimeService().startProcessInstanceByKey(PROCESS_DEFINITION_KEY_ONE).getId();

    // when
    List<LockedExternalTask> externalTasks = externalTaskService.fetchAndLock(2, WORKER_ID)
      .topic(TOPIC_NAME, LOCK_TIME)
      .tenantIdIn(TENANT_ONE, TENANT_TWO)
      .execute();

    // then
    assertEquals(2, externalTasks.size());

    for (LockedExternalTask externalTask : externalTasks) {
      if (externalTask.getProcessInstanceId().equals(processInstanceId)) {
        assertEquals(TENANT_ONE, externalTask.getTenantId());
      } else if (externalTask.getProcessInstanceId().equals(instanceId)) {
        assertEquals(TENANT_TWO, externalTask.getTenantId());
      } else {
        fail("No other external tasks should be available!");
      }
    }
  }

  // complete external task test cases
  @Test
  public void testCompleteWithAuthenticatedTenant() {

    String externalTaskId = externalTaskService.fetchAndLock(1, WORKER_ID)
      .topic(TOPIC_NAME, LOCK_TIME)
      .execute()
      .get(0)
      .getId();

    assertEquals(1, externalTaskService.createExternalTaskQuery().active().count());

    identityService.setAuthentication("aUserId", null, Arrays.asList(TENANT_ONE));

    externalTaskService.complete(externalTaskId, WORKER_ID);

    assertThat(externalTaskService.createExternalTaskQuery().active().count()).isEqualTo(0L);

  }

  @Test
  public void testCompleteWithNoAuthenticatedTenant() {

    String externalTaskId = externalTaskService.fetchAndLock(1, WORKER_ID)
      .topic(TOPIC_NAME, LOCK_TIME)
      .execute()
      .get(0)
      .getId();

    assertEquals(1, externalTaskService.createExternalTaskQuery().active().count());

    identityService.setAuthentication("aUserId", null);

    // when/then
    assertThatThrownBy(() -> externalTaskService.complete(externalTaskId, WORKER_ID))
      .isInstanceOf(ProcessEngineException.class)
      .hasMessageContaining("Cannot update the process instance '"
          + processInstanceId +"' because it belongs to no authenticated tenant.");

  }

  @Test
  public void testCompleteWithDisableTenantCheck() {

    String externalTaskId = externalTaskService.fetchAndLock(1, WORKER_ID)
      .topic(TOPIC_NAME, LOCK_TIME)
      .execute()
      .get(0)
      .getId();

    assertEquals(1, externalTaskService.createExternalTaskQuery().active().count());

    identityService.setAuthentication("aUserId", null);
    engineRule.getProcessEngineConfiguration().setTenantCheckEnabled(false);

    externalTaskService.complete(externalTaskId, WORKER_ID);
    // then
    assertThat(externalTaskService.createExternalTaskQuery().active().count()).isEqualTo(0L);
  }

  // handle failure test cases
  @Test
  public void testHandleFailureWithAuthenticatedTenant() {

    LockedExternalTask task = externalTaskService.fetchAndLock(1, WORKER_ID)
      .topic(TOPIC_NAME, LOCK_TIME)
      .execute()
      .get(0);

    identityService.setAuthentication("aUserId", null, Arrays.asList(TENANT_ONE));

    externalTaskService.handleFailure(task.getId(), WORKER_ID, ERROR_MESSAGE, 1, 0);

    // then
    assertEquals(ERROR_MESSAGE, externalTaskService.fetchAndLock(1, WORKER_ID)
      .topic(TOPIC_NAME, LOCK_TIME)
      .execute()
      .get(0)
      .getErrorMessage());

  }

  @Test
  public void testHandleFailureWithNoAuthenticatedTenant() {

    LockedExternalTask task = externalTaskService.fetchAndLock(1, WORKER_ID)
      .topic(TOPIC_NAME, LOCK_TIME)
      .execute()
      .get(0);

    identityService.setAuthentication("aUserId", null);

    // when/then
    assertThatThrownBy(() -> externalTaskService.handleFailure(task.getId(), WORKER_ID, ERROR_MESSAGE, 1, 0))
      .isInstanceOf(ProcessEngineException.class)
      .hasMessageContaining("Cannot update the process instance '"
          + processInstanceId +"' because it belongs to no authenticated tenant.");
  }

  @Test
  public void testHandleFailureWithDisabledTenantCheck() {

    String taskId = externalTaskService.fetchAndLock(1, WORKER_ID)
      .topic(TOPIC_NAME, LOCK_TIME)
      .execute()
      .get(0)
      .getId();

    identityService.setAuthentication("aUserId", null);
    engineRule.getProcessEngineConfiguration().setTenantCheckEnabled(false);

    externalTaskService.handleFailure(taskId, WORKER_ID, ERROR_MESSAGE, 1, 0);
    // then
    assertEquals(ERROR_MESSAGE, externalTaskService.fetchAndLock(1, WORKER_ID)
      .topic(TOPIC_NAME, LOCK_TIME)
      .execute()
      .get(0)
      .getErrorMessage());
  }

  // handle BPMN error
  @Test
  public void testHandleBPMNErrorWithAuthenticatedTenant() {

    String taskId = externalTaskService.fetchAndLock(1, WORKER_ID)
      .topic(TOPIC_NAME, LOCK_TIME)
      .execute()
      .get(0)
      .getId();

    identityService.setAuthentication("aUserId", null, Arrays.asList(TENANT_ONE));

    // when
    externalTaskService.handleBpmnError(taskId, WORKER_ID, "ERROR-OCCURED");

    // then
    assertEquals(taskService.createTaskQuery().singleResult().getTaskDefinitionKey(), "afterBpmnError");
  }

  @Test
  public void testHandleBPMNErrorWithNoAuthenticatedTenant() {

    String taskId = externalTaskService.fetchAndLock(1, WORKER_ID)
      .topic(TOPIC_NAME, LOCK_TIME)
      .execute()
      .get(0)
      .getId();

    identityService.setAuthentication("aUserId", null);

    // when/then
    assertThatThrownBy(() -> externalTaskService.handleBpmnError(taskId, WORKER_ID, "ERROR-OCCURED"))
      .isInstanceOf(ProcessEngineException.class)
      .hasMessageContaining("Cannot update the process instance '"
          + processInstanceId +"' because it belongs to no authenticated tenant.");

  }

  @Test
  public void testHandleBPMNErrorWithDisabledTenantCheck() {

    String taskId = externalTaskService.fetchAndLock(1, WORKER_ID)
      .topic(TOPIC_NAME, LOCK_TIME)
      .execute()
      .get(0)
      .getId();

    identityService.setAuthentication("aUserId", null);
    engineRule.getProcessEngineConfiguration().setTenantCheckEnabled(false);

    // when
    externalTaskService.handleBpmnError(taskId, WORKER_ID, "ERROR-OCCURED");

    // then
    assertEquals(taskService.createTaskQuery().singleResult().getTaskDefinitionKey(), "afterBpmnError");

  }

  // setRetries test
  @Test
  public void testSetRetriesWithAuthenticatedTenant() {
    // given
    String externalTaskId = externalTaskService.fetchAndLock(5, WORKER_ID)
      .topic(TOPIC_NAME, LOCK_TIME)
      .execute()
      .get(0)
      .getId();

    identityService.setAuthentication("aUserId", null, Arrays.asList(TENANT_ONE));

    // when
    externalTaskService.setRetries(externalTaskId, 5);

    // then
    assertEquals(5, (int) externalTaskService.createExternalTaskQuery().singleResult().getRetries());
  }

  @Test
  public void testSetRetriesWithNoAuthenticatedTenant() {
    // given
    String externalTaskId = externalTaskService.fetchAndLock(5, WORKER_ID)
      .topic(TOPIC_NAME, LOCK_TIME)
      .execute()
      .get(0)
      .getId();

    identityService.setAuthentication("aUserId", null);

    // when/then
    assertThatThrownBy(() -> externalTaskService.setRetries(externalTaskId, 5))
      .isInstanceOf(ProcessEngineException.class)
      .hasMessageContaining("Cannot update the process instance '"
          + processInstanceId +"' because it belongs to no authenticated tenant.");

  }

  @Test
  public void testSetRetriesWithDisabledTenantCheck() {
    // given
    String externalTaskId = externalTaskService.fetchAndLock(5, WORKER_ID)
      .topic(TOPIC_NAME, LOCK_TIME)
      .execute()
      .get(0)
      .getId();

    identityService.setAuthentication("aUserId", null);
    engineRule.getProcessEngineConfiguration().setTenantCheckEnabled(false);

    // when
    externalTaskService.setRetries(externalTaskId, 5);

    // then
    assertEquals(5, (int) externalTaskService.createExternalTaskQuery().singleResult().getRetries());

  }

  // set priority test cases
  @Test
  public void testSetPriorityWithAuthenticatedTenant() {
    // given
    String externalTaskId = externalTaskService.fetchAndLock(5, WORKER_ID)
      .topic(TOPIC_NAME, LOCK_TIME)
      .execute()
      .get(0)
      .getId();

    identityService.setAuthentication("aUserId", null, Arrays.asList(TENANT_ONE));

    // when
    externalTaskService.setPriority(externalTaskId, 1);

    // then
    assertEquals(1, (int) externalTaskService.createExternalTaskQuery().singleResult().getPriority());
  }

  @Test
  public void testSetPriorityWithNoAuthenticatedTenant() {
    // given
    String externalTaskId = externalTaskService.fetchAndLock(5, WORKER_ID)
      .topic(TOPIC_NAME, LOCK_TIME)
      .execute()
      .get(0)
      .getId();

    identityService.setAuthentication("aUserId", null);

    // when/then
    assertThatThrownBy(() -> externalTaskService.setPriority(externalTaskId, 1))
      .isInstanceOf(ProcessEngineException.class)
      .hasMessageContaining("Cannot update the process instance '"
          + processInstanceId +"' because it belongs to no authenticated tenant.");

  }

  @Test
  public void testSetPriorityWithDisabledTenantCheck() {
    // given
    String externalTaskId = externalTaskService.fetchAndLock(5, WORKER_ID)
      .topic(TOPIC_NAME, LOCK_TIME)
      .execute()
      .get(0)
      .getId();

    identityService.setAuthentication("aUserId", null);
    engineRule.getProcessEngineConfiguration().setTenantCheckEnabled(false);

    // when
    externalTaskService.setPriority(externalTaskId, 1);

    // then
    assertEquals(1, (int) externalTaskService.createExternalTaskQuery().singleResult().getPriority());
  }

  // unlock test cases
  @Test
  public void testUnlockWithAuthenticatedTenant() {
    // given
    String externalTaskId = externalTaskService.fetchAndLock(5, WORKER_ID)
      .topic(TOPIC_NAME, LOCK_TIME)
      .execute()
      .get(0)
      .getId();

    assertThat(externalTaskService.createExternalTaskQuery().locked().count()).isEqualTo(1L);

    identityService.setAuthentication("aUserId", null, Arrays.asList(TENANT_ONE));

    // when
    externalTaskService.unlock(externalTaskId);

    // then
    assertThat(externalTaskService.createExternalTaskQuery().locked().count()).isEqualTo(0L);
  }

  @Test
  public void testUnlockWithNoAuthenticatedTenant() {
    // given
    String externalTaskId = externalTaskService.fetchAndLock(5, WORKER_ID)
      .topic(TOPIC_NAME, LOCK_TIME)
      .execute()
      .get(0)
      .getId();

    identityService.setAuthentication("aUserId", null);

    // when/then
    assertThatThrownBy(() -> externalTaskService.unlock(externalTaskId))
      .isInstanceOf(ProcessEngineException.class)
      .hasMessageContaining("Cannot update the process instance '"
          + processInstanceId +"' because it belongs to no authenticated tenant.");
  }

  @Test
  public void testUnlockWithDisabledTenantCheck() {
    // given
    String externalTaskId = externalTaskService.fetchAndLock(5, WORKER_ID)
      .topic(TOPIC_NAME, LOCK_TIME)
      .execute()
      .get(0)
      .getId();

    identityService.setAuthentication("aUserId", null);
    engineRule.getProcessEngineConfiguration().setTenantCheckEnabled(false);

    externalTaskService.unlock(externalTaskId);
    // then
    assertThat(externalTaskService.createExternalTaskQuery().locked().count()).isEqualTo(0L);
  }

  // get error details tests
  @Test
  public void testGetErrorDetailsWithAuthenticatedTenant() {
    // given
    String externalTaskId = externalTaskService.fetchAndLock(5, WORKER_ID)
      .topic(TOPIC_NAME, LOCK_TIME)
      .execute()
      .get(0)
      .getId();

    externalTaskService.handleFailure(externalTaskId,WORKER_ID,ERROR_MESSAGE,ERROR_DETAILS,1,1000L);

    identityService.setAuthentication("aUserId", null, Arrays.asList(TENANT_ONE));

    // when then
    assertThat(externalTaskService.getExternalTaskErrorDetails(externalTaskId)).isEqualTo(ERROR_DETAILS);
  }

  @Test
  public void testGetErrorDetailsWithNoAuthenticatedTenant() {
    // given
    String externalTaskId = externalTaskService.fetchAndLock(5, WORKER_ID)
        .topic(TOPIC_NAME, LOCK_TIME)
        .execute()
        .get(0)
        .getId();

    externalTaskService.handleFailure(externalTaskId,WORKER_ID,ERROR_MESSAGE,ERROR_DETAILS,1,1000L);

    identityService.setAuthentication("aUserId", null);

    // when/then
    assertThatThrownBy(() -> externalTaskService.getExternalTaskErrorDetails(externalTaskId))
      .isInstanceOf(ProcessEngineException.class)
      .hasMessageContaining("Cannot read the process instance '"
          + processInstanceId +"' because it belongs to no authenticated tenant.");
  }

  @Test
  public void testGetErrorDetailsWithDisabledTenantCheck() {
    // given
    String externalTaskId = externalTaskService.fetchAndLock(5, WORKER_ID)
        .topic(TOPIC_NAME, LOCK_TIME)
        .execute()
        .get(0)
        .getId();

    externalTaskService.handleFailure(externalTaskId,WORKER_ID,ERROR_MESSAGE,ERROR_DETAILS,1,1000L);

    identityService.setAuthentication("aUserId", null);
    engineRule.getProcessEngineConfiguration().setTenantCheckEnabled(false);

    // then
    assertThat(externalTaskService.getExternalTaskErrorDetails(externalTaskId)).isEqualTo(ERROR_DETAILS);
  }
}
