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
package org.camunda.bpm.engine.test.api.authorization.history;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.camunda.bpm.engine.authorization.Authorization.ANY;
import static org.camunda.bpm.engine.authorization.Permissions.READ_HISTORY;
import static org.camunda.bpm.engine.authorization.Resources.PROCESS_DEFINITION;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.camunda.bpm.engine.AuthorizationException;
import org.camunda.bpm.engine.ProcessEngineConfiguration;
import org.camunda.bpm.engine.authorization.HistoricProcessInstancePermissions;
import org.camunda.bpm.engine.authorization.ProcessDefinitionPermissions;
import org.camunda.bpm.engine.authorization.Resources;
import org.camunda.bpm.engine.history.HistoricJobLog;
import org.camunda.bpm.engine.history.HistoricJobLogQuery;
import org.camunda.bpm.engine.history.HistoricProcessInstance;
import org.camunda.bpm.engine.impl.AbstractQuery;
import org.camunda.bpm.engine.impl.interceptor.Command;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.interceptor.CommandExecutor;
import org.camunda.bpm.engine.impl.jobexecutor.TimerSuspendProcessDefinitionHandler;
import org.camunda.bpm.engine.task.Task;
import org.camunda.bpm.engine.test.RequiredHistoryLevel;
import org.camunda.bpm.engine.test.api.authorization.AuthorizationTest;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Roman Smirnov
 *
 */
@RequiredHistoryLevel(ProcessEngineConfiguration.HISTORY_FULL)
public class HistoricJobLogAuthorizationTest extends AuthorizationTest {

  protected static final String TIMER_START_PROCESS_KEY = "timerStartProcess";
  protected static final String TIMER_BOUNDARY_PROCESS_KEY = "timerBoundaryProcess";
  protected static final String ONE_INCIDENT_PROCESS_KEY = "process";

  protected String batchId;
  protected String deploymentId;

  @Before
  public void setUp() throws Exception {
    deploymentId = testRule.deploy(
        "org/camunda/bpm/engine/test/api/authorization/timerStartEventProcess.bpmn20.xml",
        "org/camunda/bpm/engine/test/api/authorization/timerBoundaryEventProcess.bpmn20.xml",
        "org/camunda/bpm/engine/test/api/authorization/oneIncidentProcess.bpmn20.xml")
            .getId();
    super.setUp();
  }

  @After
  public void tearDown() {
    super.tearDown();
    CommandExecutor commandExecutor = processEngineConfiguration.getCommandExecutorTxRequired();
    commandExecutor.execute(new Command<Object>() {
      public Object execute(CommandContext commandContext) {
        commandContext.getHistoricJobLogManager().deleteHistoricJobLogsByHandlerType(TimerSuspendProcessDefinitionHandler.TYPE);
        return null;
      }
    });
    processEngineConfiguration.setEnableHistoricInstancePermissions(false);

    if (batchId != null) {
      managementService.deleteBatch(batchId, true);
      batchId = null;
    }
  }

  // historic job log query (start timer job) ////////////////////////////////

  @Test
  public void testStartTimerJobLogQueryWithoutAuthorization() {
    // given

    // when

    HistoricJobLogQuery query = historyService.createHistoricJobLogQuery();

    // then
    verifyQueryResults(query, 0);
  }

  @Test
  public void testStartTimerJobLogQueryWithReadHistoryPermissionOnProcessDefinition() {
    // given
    createGrantAuthorization(PROCESS_DEFINITION, TIMER_START_PROCESS_KEY, userId, READ_HISTORY);

    // when
    HistoricJobLogQuery query = historyService.createHistoricJobLogQuery();

    // then
    verifyQueryResults(query, 1);
  }

  @Test
  public void testStartTimerJobLogQueryWithReadHistoryPermissionOnAnyProcessDefinition() {
    // given
    createGrantAuthorization(PROCESS_DEFINITION, ANY, userId, READ_HISTORY);

    // when
    HistoricJobLogQuery query = historyService.createHistoricJobLogQuery();

    // then
    verifyQueryResults(query, 1);
  }

  // historic job log query ////////////////////////////////////////////////

  @Test
  public void testSimpleQueryWithoutAuthorization() {
    // given
    startProcessAndExecuteJob(ONE_INCIDENT_PROCESS_KEY);

    // when
    HistoricJobLogQuery query = historyService.createHistoricJobLogQuery();

    // then
    verifyQueryResults(query, 0);
  }

  @Test
  public void testSimpleQueryWithHistoryReadPermissionOnProcessDefinition() {
    // given
    startProcessAndExecuteJob(ONE_INCIDENT_PROCESS_KEY);
    createGrantAuthorization(PROCESS_DEFINITION, ONE_INCIDENT_PROCESS_KEY, userId, READ_HISTORY);

    // when
    HistoricJobLogQuery query = historyService.createHistoricJobLogQuery();

    // then
    verifyQueryResults(query, 4);
  }

  @Test
  public void testSimpleQueryWithHistoryReadPermissionOnAnyProcessDefinition() {
    // given
    startProcessAndExecuteJob(ONE_INCIDENT_PROCESS_KEY);
    createGrantAuthorization(PROCESS_DEFINITION, ANY, userId, READ_HISTORY);

    // when
    HistoricJobLogQuery query = historyService.createHistoricJobLogQuery();

    // then
    verifyQueryResults(query, 5);
  }

  @Test
  public void testSimpleQueryWithMultiple() {
    // given
    startProcessAndExecuteJob(ONE_INCIDENT_PROCESS_KEY);
    createGrantAuthorization(PROCESS_DEFINITION, ANY, userId, READ_HISTORY);
    createGrantAuthorization(PROCESS_DEFINITION, ONE_INCIDENT_PROCESS_KEY, userId, READ_HISTORY);

    // when
    HistoricJobLogQuery query = historyService.createHistoricJobLogQuery();

    // then
    verifyQueryResults(query, 5);
  }

  // historic job log query (multiple process instance) ////////////////////////////////////////////////

  @Test
  public void testQueryWithoutAuthorization() {
    // given
    startProcessAndExecuteJob(ONE_INCIDENT_PROCESS_KEY);
    startProcessAndExecuteJob(ONE_INCIDENT_PROCESS_KEY);
    startProcessAndExecuteJob(ONE_INCIDENT_PROCESS_KEY);

    disableAuthorization();
    String jobId = managementService.createJobQuery().processDefinitionKey(TIMER_START_PROCESS_KEY).singleResult().getId();
    managementService.executeJob(jobId);
    jobId = managementService.createJobQuery().processDefinitionKey(TIMER_START_PROCESS_KEY).singleResult().getId();
    managementService.executeJob(jobId);
    enableAuthorization();

    // when
    HistoricJobLogQuery query = historyService.createHistoricJobLogQuery();

    // then
    verifyQueryResults(query, 0);
  }

  @Test
  public void testQueryWithHistoryReadPermissionOnProcessDefinition() {
    // given
    startProcessAndExecuteJob(ONE_INCIDENT_PROCESS_KEY);
    startProcessAndExecuteJob(ONE_INCIDENT_PROCESS_KEY);
    startProcessAndExecuteJob(ONE_INCIDENT_PROCESS_KEY);

    disableAuthorization();
    String jobId = managementService.createJobQuery().processDefinitionKey(TIMER_START_PROCESS_KEY).singleResult().getId();
    managementService.executeJob(jobId);
    jobId = managementService.createJobQuery().processDefinitionKey(TIMER_START_PROCESS_KEY).singleResult().getId();
    managementService.executeJob(jobId);
    enableAuthorization();

    createGrantAuthorization(PROCESS_DEFINITION, ONE_INCIDENT_PROCESS_KEY, userId, READ_HISTORY);

    // when
    HistoricJobLogQuery query = historyService.createHistoricJobLogQuery();

    // then
    verifyQueryResults(query, 12);
  }

  @Test
  public void testQueryWithHistoryReadPermissionOnAnyProcessDefinition() {
    // given
    startProcessAndExecuteJob(ONE_INCIDENT_PROCESS_KEY);
    startProcessAndExecuteJob(ONE_INCIDENT_PROCESS_KEY);
    startProcessAndExecuteJob(ONE_INCIDENT_PROCESS_KEY);

    disableAuthorization();
    String jobId = managementService.createJobQuery().processDefinitionKey(TIMER_START_PROCESS_KEY).singleResult().getId();
    managementService.executeJob(jobId);
    jobId = managementService.createJobQuery().processDefinitionKey(TIMER_START_PROCESS_KEY).singleResult().getId();
    managementService.executeJob(jobId);
    enableAuthorization();

    createGrantAuthorization(PROCESS_DEFINITION, ANY, userId, READ_HISTORY);

    // when
    HistoricJobLogQuery query = historyService.createHistoricJobLogQuery();

    // then
    verifyQueryResults(query, 17);
  }

  // historic job log query (standalone job) ///////////////////////

  @Test
  public void testQueryAfterStandaloneJob() {
    // given
    disableAuthorization();
    repositoryService.suspendProcessDefinitionByKey(TIMER_BOUNDARY_PROCESS_KEY, true, new Date());
    enableAuthorization();

    // when
    HistoricJobLogQuery query = historyService.createHistoricJobLogQuery();

    // then
    verifyQueryResults(query, 1);

    HistoricJobLog jobLog = query.singleResult();
    assertNull(jobLog.getProcessDefinitionKey());

    deleteDeployment(deploymentId);

    disableAuthorization();
    String jobId = managementService.createJobQuery().singleResult().getId();
    managementService.deleteJob(jobId);
    enableAuthorization();
  }

  // delete deployment (cascade = false)

  @Test
  public void testQueryAfterDeletingDeployment() {
    // given
    startProcessInstanceByKey(TIMER_BOUNDARY_PROCESS_KEY);
    startProcessInstanceByKey(TIMER_BOUNDARY_PROCESS_KEY);
    startProcessInstanceByKey(TIMER_BOUNDARY_PROCESS_KEY);
    createGrantAuthorization(PROCESS_DEFINITION, TIMER_BOUNDARY_PROCESS_KEY, userId, READ_HISTORY);

    disableAuthorization();
    List<Task> tasks = taskService.createTaskQuery().list();
    for (Task task : tasks) {
      taskService.complete(task.getId());
    }
    enableAuthorization();

    disableAuthorization();
    repositoryService.deleteDeployment(deploymentId);
    enableAuthorization();

    // when
    HistoricJobLogQuery query = historyService.createHistoricJobLogQuery();

    // then
    verifyQueryResults(query, 6);

    disableAuthorization();
    List<HistoricProcessInstance> instances = historyService.createHistoricProcessInstanceQuery().list();
    for (HistoricProcessInstance instance : instances) {
      historyService.deleteHistoricProcessInstance(instance.getId());
    }
    enableAuthorization();
  }

  // get historic job log exception stacktrace (standalone) /////////////////////

  @Test
  public void testGetHistoricStandaloneJobLogExceptionStacktrace() {
    // given
    disableAuthorization();
    repositoryService.suspendProcessDefinitionByKey(TIMER_BOUNDARY_PROCESS_KEY, true, new Date());
    enableAuthorization();
    String jobLogId = historyService.createHistoricJobLogQuery().singleResult().getId();

    // when
    String stacktrace = historyService.getHistoricJobLogExceptionStacktrace(jobLogId);

    // then
    assertNull(stacktrace);

    deleteDeployment(deploymentId);

    disableAuthorization();
    String jobId = managementService.createJobQuery().singleResult().getId();
    managementService.deleteJob(jobId);
    enableAuthorization();
  }

  // get historic job log exception stacktrace /////////////////////

  @Test
  public void testGetHistoricJobLogExceptionStacktraceWithoutAuthorization() {
    // given
    startProcessAndExecuteJob(ONE_INCIDENT_PROCESS_KEY);

    disableAuthorization();
    String jobLogId = historyService.createHistoricJobLogQuery().failureLog().listPage(0, 1).get(0).getId();
    enableAuthorization();

    try {
      // when
      historyService.getHistoricJobLogExceptionStacktrace(jobLogId);
      fail("Exception expected: It should not be possible to get the historic job log exception stacktrace");
    } catch (AuthorizationException e) {
      // then
      String message = e.getMessage();
      testRule.assertTextPresent(userId, message);
      testRule.assertTextPresent(READ_HISTORY.getName(), message);
      testRule.assertTextPresent(ONE_INCIDENT_PROCESS_KEY, message);
      testRule.assertTextPresent(PROCESS_DEFINITION.resourceName(), message);
    }
  }

  @Test
  public void testGetHistoricJobLogExceptionStacktraceWithReadHistoryPermissionOnProcessDefinition() {
    // given
    startProcessAndExecuteJob(ONE_INCIDENT_PROCESS_KEY);

    disableAuthorization();
    String jobLogId = historyService.createHistoricJobLogQuery().failureLog().listPage(0, 1).get(0).getId();
    enableAuthorization();

    createGrantAuthorization(PROCESS_DEFINITION, ONE_INCIDENT_PROCESS_KEY, userId, READ_HISTORY);

    // when
    String stacktrace = historyService.getHistoricJobLogExceptionStacktrace(jobLogId);

    // then
    assertNotNull(stacktrace);
  }

  @Test
  public void testGetHistoricJobLogExceptionStacktraceWithReadHistoryPermissionOnAnyProcessDefinition() {
    // given
    startProcessAndExecuteJob(ONE_INCIDENT_PROCESS_KEY);

    disableAuthorization();
    String jobLogId = historyService.createHistoricJobLogQuery().failureLog().listPage(0, 1).get(0).getId();
    enableAuthorization();

    createGrantAuthorization(PROCESS_DEFINITION, ANY, userId, READ_HISTORY);

    // when
    String stacktrace = historyService.getHistoricJobLogExceptionStacktrace(jobLogId);

    // then
    assertNotNull(stacktrace);
  }

  @Test
  public void testCheckNonePermissionOnHistoricProcessInstance() {
    // given
    processEngineConfiguration.setEnableHistoricInstancePermissions(true);

    String processInstanceId = startProcessAndExecuteJob(ONE_INCIDENT_PROCESS_KEY)
        .getProcessInstanceId();

    createGrantAuthorization(Resources.HISTORIC_PROCESS_INSTANCE, processInstanceId, userId,
        HistoricProcessInstancePermissions.NONE);

    // when
    HistoricJobLogQuery query = historyService.createHistoricJobLogQuery();

    // then
    assertThat(query.list()).isEmpty();
  }

  @Test
  public void testCheckReadPermissionOnHistoricProcessInstance() {
    // given
    processEngineConfiguration.setEnableHistoricInstancePermissions(true);

    String processInstanceId = startProcessAndExecuteJob(ONE_INCIDENT_PROCESS_KEY)
        .getProcessInstanceId();

    createGrantAuthorization(Resources.HISTORIC_PROCESS_INSTANCE, processInstanceId, userId,
        HistoricProcessInstancePermissions.READ);

    // when
    HistoricJobLogQuery query = historyService.createHistoricJobLogQuery();

    // then
    assertThat(query.list())
        .extracting("processInstanceId")
        .containsExactly(
            processInstanceId,
            processInstanceId,
            processInstanceId,
            processInstanceId
        );
  }

  @Test
  public void testCheckNoneOnHistoricProcessInstanceAndReadHistoryPermissionOnProcessDefinition() {
    // given
    processEngineConfiguration.setEnableHistoricInstancePermissions(true);

    String processInstanceId = startProcessAndExecuteJob(ONE_INCIDENT_PROCESS_KEY)
        .getProcessInstanceId();

    createGrantAuthorization(Resources.HISTORIC_PROCESS_INSTANCE, processInstanceId, userId,
        HistoricProcessInstancePermissions.NONE);
    createGrantAuthorization(PROCESS_DEFINITION, ONE_INCIDENT_PROCESS_KEY, userId, READ_HISTORY);

    // when
    HistoricJobLogQuery query = historyService.createHistoricJobLogQuery();

    // then
    assertThat(query.list())
        .extracting("processInstanceId")
        .containsExactly(
            processInstanceId,
            processInstanceId,
            processInstanceId,
            processInstanceId
        );
  }

  @Test
  public void testCheckReadOnHistoricProcessInstanceAndNonePermissionOnProcessDefinition() {
    // given
    processEngineConfiguration.setEnableHistoricInstancePermissions(true);

    String processInstanceId = startProcessAndExecuteJob(ONE_INCIDENT_PROCESS_KEY)
        .getProcessInstanceId();

    createGrantAuthorization(Resources.HISTORIC_PROCESS_INSTANCE, processInstanceId, userId,
        HistoricProcessInstancePermissions.READ);
    createGrantAuthorization(PROCESS_DEFINITION, ONE_INCIDENT_PROCESS_KEY, userId,
        ProcessDefinitionPermissions.NONE);

    // when
    HistoricJobLogQuery query = historyService.createHistoricJobLogQuery();

    // then
    assertThat(query.list())
        .extracting("processInstanceId")
        .containsExactly(
            processInstanceId,
            processInstanceId,
            processInstanceId,
            processInstanceId
        );
  }

  @Test
  public void testHistoricProcessInstancePermissionsAuthorizationDisabled() {
    // given
    processEngineConfiguration.setEnableHistoricInstancePermissions(true);

    String processInstanceId = startProcessAndExecuteJob(ONE_INCIDENT_PROCESS_KEY)
        .getProcessInstanceId();

    disableAuthorization();

    // when
    HistoricJobLogQuery query = historyService.createHistoricJobLogQuery()
        .processInstanceId(processInstanceId);

    // then
    assertThat(query.list())
        .extracting("processInstanceId")
        .containsExactly(
            processInstanceId,
            processInstanceId,
            processInstanceId,
            processInstanceId
        );
  }

  @Test
  public void testSkipAuthOnNonProcessJob() {
    // given
    String processInstanceId = startProcessAndExecuteJob(ONE_INCIDENT_PROCESS_KEY)
        .getProcessInstanceId();

    disableAuthorization();
    batchId =
        runtimeService.deleteProcessInstancesAsync(Arrays.asList(processInstanceId), "bar")
            .getId();
    enableAuthorization();

    // when
    HistoricJobLogQuery query = historyService.createHistoricJobLogQuery();

    // then
    assertThat(query.list())
        .extracting("jobDefinitionType", "processInstanceId")
        .containsExactly(tuple("batch-seed-job", null));
  }

  @Test
  public void testSkipAuthOnNonProcessJob_HistoricInstancePermissionsEnabled() {
    // given
    processEngineConfiguration.setEnableHistoricInstancePermissions(true);

    String processInstanceId = startProcessAndExecuteJob(ONE_INCIDENT_PROCESS_KEY)
        .getProcessInstanceId();

    disableAuthorization();
    batchId =
        runtimeService.deleteProcessInstancesAsync(Arrays.asList(processInstanceId), "bar")
            .getId();
    enableAuthorization();

    // when
    HistoricJobLogQuery query = historyService.createHistoricJobLogQuery();

    // then
    assertThat(query.list())
        .extracting("jobDefinitionType", "processInstanceId")
        .containsExactly(tuple("batch-seed-job", null));
  }

  // helper ////////////////////////////////////////////////////////

  protected void verifyQueryResults(HistoricJobLogQuery query, int countExpected) {
    verifyQueryResults((AbstractQuery<?, ?>) query, countExpected);
  }

}
