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
package org.camunda.bpm.engine.test.api.multitenancy.query.history;

import org.camunda.bpm.engine.*;
import org.camunda.bpm.engine.exception.NullValueException;
import org.camunda.bpm.engine.externaltask.ExternalTask;
import org.camunda.bpm.engine.externaltask.LockedExternalTask;
import org.camunda.bpm.engine.history.HistoricExternalTaskLog;
import org.camunda.bpm.engine.history.HistoricExternalTaskLogQuery;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.test.ProcessEngineRule;
import org.camunda.bpm.engine.test.RequiredHistoryLevel;
import org.camunda.bpm.engine.test.util.ProcessEngineTestRule;
import org.camunda.bpm.engine.test.util.ProvidedProcessEngineRule;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.camunda.bpm.engine.test.api.runtime.migration.models.ExternalTaskModels.ONE_EXTERNAL_TASK_PROCESS;
import static org.camunda.bpm.engine.test.api.runtime.migration.models.builder.DefaultExternalTaskModelBuilder.DEFAULT_PROCESS_KEY;
import static org.camunda.bpm.engine.test.api.runtime.migration.models.builder.DefaultExternalTaskModelBuilder.DEFAULT_TOPIC;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

@RequiredHistoryLevel(ProcessEngineConfiguration.HISTORY_FULL)
public class MultiTenancyHistoricExternalTaskLogTest {

  protected ProcessEngineRule engineRule = new ProvidedProcessEngineRule();

  protected ProcessEngineTestRule testRule = new ProcessEngineTestRule(engineRule);

  protected HistoryService historyService;
  protected RuntimeService runtimeService;
  protected RepositoryService repositoryService;
  protected IdentityService identityService;
  protected ExternalTaskService externalTaskService;

  @Rule
  public RuleChain ruleChain = RuleChain.outerRule(engineRule).around(testRule);

  protected final String TENANT_ONE = "tenant1";
  protected final String TENANT_TWO = "tenant2";

  protected final String WORKER_ID = "aWorkerId";
  protected final String ERROR_DETAILS = "These are the error details!";
  protected final long LOCK_DURATION = 5 * 60L * 1000L;


  @Before
  public void setUp() {
    repositoryService = engineRule.getRepositoryService();
    historyService = engineRule.getHistoryService();
    runtimeService = engineRule.getRuntimeService();
    identityService = engineRule.getIdentityService();
    externalTaskService = engineRule.getExternalTaskService();

    testRule.deployForTenant(TENANT_ONE, ONE_EXTERNAL_TASK_PROCESS);
    testRule.deployForTenant(TENANT_TWO, ONE_EXTERNAL_TASK_PROCESS);

    startProcessInstanceAndFailExternalTask(TENANT_ONE);
    startProcessInstanceFailAndCompleteExternalTask(TENANT_TWO);
  }

  @Test
  public void testQueryWithoutTenantId() {

    //given two process with different tenants

    // when
    HistoricExternalTaskLogQuery query = historyService.
      createHistoricExternalTaskLogQuery();

    // then
    assertThat(query.count(), is(5L));
  }

  @Test
  public void testQueryByTenantId() {

    // given two process with different tenants

    // when
    HistoricExternalTaskLogQuery queryTenant1 = historyService
      .createHistoricExternalTaskLogQuery()
      .tenantIdIn(TENANT_ONE);
    HistoricExternalTaskLogQuery queryTenant2 = historyService
      .createHistoricExternalTaskLogQuery()
      .tenantIdIn(TENANT_TWO);

    // then
    assertThat(queryTenant1.count(), is(2L));
    assertThat(queryTenant2.count(), is(3L));
  }

  @Test
  public void testQueryByTenantIds() {

    //given two process with different tenants

    // when
    HistoricExternalTaskLogQuery query = historyService
      .createHistoricExternalTaskLogQuery()
      .tenantIdIn(TENANT_ONE, TENANT_TWO);

    // then
    assertThat(query.count(), is(5L));
  }

  @Test
  public void testQueryByNonExistingTenantId() {

    //given two process with different tenants

    // when
    HistoricExternalTaskLogQuery query = historyService
      .createHistoricExternalTaskLogQuery()
      .tenantIdIn("nonExisting");

    // then
    assertThat(query.count(), is(0L));
  }

  @Test
  public void testFailQueryByTenantIdNull() {
    try {
      historyService.createHistoricExternalTaskLogQuery()
        .tenantIdIn((String) null);

      fail("expected exception");
    } catch (NullValueException e) {
      // test passed
    }
  }

  @Test
  public void testQuerySortingAsc() {

    //given two process with different tenants

    // when
    List<HistoricExternalTaskLog> HistoricExternalTaskLogs = historyService.createHistoricExternalTaskLogQuery()
      .orderByTenantId()
      .asc()
      .list();

    // then
    assertThat(HistoricExternalTaskLogs.size(), is(5));
    assertThat(HistoricExternalTaskLogs.get(0).getTenantId(), is(TENANT_ONE));
    assertThat(HistoricExternalTaskLogs.get(1).getTenantId(), is(TENANT_ONE));
    assertThat(HistoricExternalTaskLogs.get(2).getTenantId(), is(TENANT_TWO));
    assertThat(HistoricExternalTaskLogs.get(3).getTenantId(), is(TENANT_TWO));
    assertThat(HistoricExternalTaskLogs.get(4).getTenantId(), is(TENANT_TWO));
  }

  @Test
  public void testQuerySortingDesc() {

    //given two process with different tenants

    // when
    List<HistoricExternalTaskLog> HistoricExternalTaskLogs = historyService.createHistoricExternalTaskLogQuery()
      .orderByTenantId()
      .desc()
      .list();

    // then
    assertThat(HistoricExternalTaskLogs.size(), is(5));
    assertThat(HistoricExternalTaskLogs.get(0).getTenantId(), is(TENANT_TWO));
    assertThat(HistoricExternalTaskLogs.get(1).getTenantId(), is(TENANT_TWO));
    assertThat(HistoricExternalTaskLogs.get(2).getTenantId(), is(TENANT_TWO));
    assertThat(HistoricExternalTaskLogs.get(3).getTenantId(), is(TENANT_ONE));
    assertThat(HistoricExternalTaskLogs.get(4).getTenantId(), is(TENANT_ONE));
  }

  @Test
  public void testQueryNoAuthenticatedTenants() {

    // given
    identityService.setAuthentication("user", null, null);

    // when
    HistoricExternalTaskLogQuery query = historyService.createHistoricExternalTaskLogQuery();

    // then
    assertThat(query.count(), is(0L));
  }

  @Test
  public void testQueryAuthenticatedTenant() {
    // given
    identityService.setAuthentication("user", null, Collections.singletonList(TENANT_ONE));

    // when
    HistoricExternalTaskLogQuery query = historyService.createHistoricExternalTaskLogQuery();

    // then
    assertThat(query.count(), is(2L));
    assertThat(query.tenantIdIn(TENANT_ONE).count(), is(2L));
    assertThat(query.tenantIdIn(TENANT_TWO).count(), is(0L));
    assertThat(query.tenantIdIn(TENANT_ONE, TENANT_TWO).count(), is(2L));
  }

  @Test
  public void testQueryAuthenticatedTenants() {
    // given
    identityService.setAuthentication("user", null, Arrays.asList(TENANT_ONE, TENANT_TWO));

    // when
    HistoricExternalTaskLogQuery query = historyService.createHistoricExternalTaskLogQuery();

    // then
    assertThat(query.count(), is(5L));
    assertThat(query.tenantIdIn(TENANT_ONE).count(), is(2L));
    assertThat(query.tenantIdIn(TENANT_TWO).count(), is(3L));
  }

  @Test
  public void testQueryDisabledTenantCheck() {
    // given
    engineRule.getProcessEngineConfiguration().setTenantCheckEnabled(false);
    identityService.setAuthentication("user", null, null);

    // when
    HistoricExternalTaskLogQuery query = historyService.createHistoricExternalTaskLogQuery();

    // then
    assertThat(query.count(), is(5L));
  }

  @Test
  public void testGetErrorDetailsNoAuthenticatedTenants() {
    // given
    identityService.setAuthentication("user", null, Collections.singletonList(TENANT_ONE));

    String failedHistoricExternalTaskLogId = historyService
      .createHistoricExternalTaskLogQuery()
      .failureLog()
      .tenantIdIn(TENANT_ONE)
      .singleResult()
      .getId();
    identityService.clearAuthentication();
    identityService.setAuthentication("user", null, null);


    try {
      // when
      historyService.getHistoricExternalTaskLogErrorDetails(failedHistoricExternalTaskLogId);
      fail("Exception expected: It should not be possible to retrieve the error details");
    } catch (ProcessEngineException e) {
      // then
      String errorMessage = e.getMessage();
      assertThat(errorMessage.contains("Cannot get the historic external task log "), is(true));
      assertThat(errorMessage.contains(failedHistoricExternalTaskLogId), is(true));
      assertThat(errorMessage.contains("because it belongs to no authenticated tenant."), is(true));
    }
  }

  @Test
  public void testGetErrorDetailsAuthenticatedTenant() {
    // given
    identityService.setAuthentication("user", null, Collections.singletonList(TENANT_ONE));

    String failedHistoricExternalTaskLogId = historyService
      .createHistoricExternalTaskLogQuery()
      .failureLog()
      .tenantIdIn(TENANT_ONE)
      .singleResult()
      .getId();

    // when
    String stacktrace = historyService.getHistoricExternalTaskLogErrorDetails(failedHistoricExternalTaskLogId);

    // then
    assertThat(stacktrace, is(notNullValue()));
    assertThat(stacktrace, is(ERROR_DETAILS));
  }

  @Test
  public void testGetErrorDetailsAuthenticatedTenants() {
    // given
    identityService.setAuthentication("user", null, Arrays.asList(TENANT_ONE, TENANT_TWO));

    String logIdTenant1 = historyService
      .createHistoricExternalTaskLogQuery()
      .failureLog()
      .tenantIdIn(TENANT_ONE)
      .singleResult()
      .getId();

    String logIdTenant2 = historyService
      .createHistoricExternalTaskLogQuery()
      .failureLog()
      .tenantIdIn(TENANT_ONE)
      .singleResult()
      .getId();

    // when
    String stacktrace1 = historyService.getHistoricExternalTaskLogErrorDetails(logIdTenant1);
    String stacktrace2 = historyService.getHistoricExternalTaskLogErrorDetails(logIdTenant2);

    // then
    assertThat(stacktrace1, is(notNullValue()));
    assertThat(stacktrace1, is(ERROR_DETAILS));
    assertThat(stacktrace2, is(notNullValue()));
    assertThat(stacktrace2, is(ERROR_DETAILS));
  }

  // helper methods

  protected void completeExternalTask(String externalTaskId) {
    List<LockedExternalTask> list = externalTaskService.fetchAndLock(100, WORKER_ID, true)
      .topic(DEFAULT_TOPIC, LOCK_DURATION)
      .execute();
    externalTaskService.complete(externalTaskId, WORKER_ID);
    // unlock the remaining tasks
    for (LockedExternalTask lockedExternalTask : list) {
      if (!lockedExternalTask.getId().equals(externalTaskId)) {
        externalTaskService.unlock(lockedExternalTask.getId());
      }
    }
  }

  @SuppressWarnings("deprecation")
  protected ExternalTask startProcessInstanceAndFailExternalTask(String tenant) {
    ProcessInstance pi = runtimeService.createProcessInstanceByKey(DEFAULT_PROCESS_KEY).processDefinitionTenantId(tenant).execute();
    ExternalTask externalTask = externalTaskService
      .createExternalTaskQuery()
      .processInstanceId(pi.getId())
      .singleResult();
    reportExternalTaskFailure(externalTask.getId());
    return externalTask;
  }

  protected void startProcessInstanceFailAndCompleteExternalTask(String tenant) {
    ExternalTask task = startProcessInstanceAndFailExternalTask(tenant);
    completeExternalTask(task.getId());
  }

  protected void reportExternalTaskFailure(String externalTaskId) {
    reportExternalTaskFailure(externalTaskId, DEFAULT_TOPIC, WORKER_ID, 1, false, "This is an error!");
  }

  protected void reportExternalTaskFailure(String externalTaskId, String topic, String workerId, Integer retries, boolean usePriority, String errorMessage) {
    List<LockedExternalTask> list = externalTaskService.fetchAndLock(100, workerId, usePriority)
      .topic(topic, LOCK_DURATION)
      .execute();
    externalTaskService.handleFailure(externalTaskId, workerId, errorMessage, ERROR_DETAILS, retries, 0L);

    for (LockedExternalTask lockedExternalTask : list) {
      externalTaskService.unlock(lockedExternalTask.getId());
    }
  }
}
