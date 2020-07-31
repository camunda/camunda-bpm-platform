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
package org.camunda.bpm.engine.test.api.optimize;

import static org.camunda.bpm.engine.history.UserOperationLogEntry.CATEGORY_OPERATOR;
import static org.camunda.bpm.engine.history.UserOperationLogEntry.OPERATION_TYPE_ACTIVATE;
import static org.camunda.bpm.engine.history.UserOperationLogEntry.OPERATION_TYPE_ACTIVATE_JOB;
import static org.camunda.bpm.engine.history.UserOperationLogEntry.OPERATION_TYPE_ACTIVATE_PROCESS_DEFINITION;
import static org.camunda.bpm.engine.history.UserOperationLogEntry.OPERATION_TYPE_SUSPEND;
import static org.camunda.bpm.engine.history.UserOperationLogEntry.OPERATION_TYPE_SUSPEND_JOB;
import static org.camunda.bpm.engine.history.UserOperationLogEntry.OPERATION_TYPE_SUSPEND_PROCESS_DEFINITION;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThan;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.camunda.bpm.engine.EntityTypes;
import org.camunda.bpm.engine.IdentityService;
import org.camunda.bpm.engine.ProcessEngineConfiguration;
import org.camunda.bpm.engine.RepositoryService;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.TaskService;
import org.camunda.bpm.engine.batch.Batch;
import org.camunda.bpm.engine.history.UserOperationLogEntry;
import org.camunda.bpm.engine.identity.User;
import org.camunda.bpm.engine.impl.OptimizeService;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.impl.persistence.entity.SuspensionState;
import org.camunda.bpm.engine.impl.util.ClockUtil;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.task.Attachment;
import org.camunda.bpm.engine.task.Task;
import org.camunda.bpm.engine.test.ProcessEngineRule;
import org.camunda.bpm.engine.test.RequiredHistoryLevel;
import org.camunda.bpm.engine.test.api.runtime.BatchSuspensionHelper;
import org.camunda.bpm.engine.test.util.ProcessEngineTestRule;
import org.camunda.bpm.engine.test.util.ProvidedProcessEngineRule;
import org.camunda.bpm.model.bpmn.Bpmn;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;

@RequiredHistoryLevel(ProcessEngineConfiguration.HISTORY_FULL)
public class GetHistoricOperationLogsForOptimizeTest {

  public ProcessEngineRule engineRule = new ProvidedProcessEngineRule();
  protected ProcessEngineTestRule testHelper = new ProcessEngineTestRule(engineRule);
  protected BatchSuspensionHelper helper = new BatchSuspensionHelper(engineRule);

  @Rule
  public RuleChain ruleChain = RuleChain.outerRule(engineRule).around(testHelper);

  private OptimizeService optimizeService;

  protected String userId = "test";

  private IdentityService identityService;
  private RuntimeService runtimeService;
  private TaskService taskService;
  private RepositoryService repositoryService;

  @Before
  public void init() {
    ProcessEngineConfigurationImpl config =
      engineRule.getProcessEngineConfiguration();
    optimizeService = config.getOptimizeService();
    identityService = engineRule.getIdentityService();
    runtimeService = engineRule.getRuntimeService();
    taskService = engineRule.getTaskService();
    repositoryService = engineRule.getRepositoryService();


    createUser(userId);
    identityService.setAuthenticatedUserId(userId);
    deploySimpleDefinition();
  }

  @After
  public void cleanUp() {
    for (User user : identityService.createUserQuery().list()) {
      identityService.deleteUser(user.getId());
    }
    ClockUtil.reset();
    identityService.clearAuthentication();
    helper.removeAllRunningAndHistoricBatches();
  }

  @Test
  public void getHistoricUserOperationLogs_suspendProcessInstanceByProcessInstanceId() {
    // given
    Date now = new Date();
    ClockUtil.setCurrentTime(now);
    final ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("process");

    Date nowPlus2Seconds = new Date(now.getTime() + 2000L);
    ClockUtil.setCurrentTime(nowPlus2Seconds);
    runtimeService.suspendProcessInstanceById(processInstance.getProcessInstanceId());
    Date nowPlus4Seconds = new Date(now.getTime() + 4000L);
    ClockUtil.setCurrentTime(nowPlus4Seconds);
    runtimeService.activateProcessInstanceById(processInstance.getProcessInstanceId());

    // when
    List<UserOperationLogEntry> userOperationsLog =
      optimizeService.getHistoricUserOperationLogs(pastDate(), null, 10);

    // then
    assertThat(userOperationsLog.size(), is(2));
    assertThat(userOperationsLog.get(0), notNullValue());
    assertThat(userOperationsLog.get(0).getId(), notNullValue());
    assertThat(userOperationsLog.get(0).getOperationType(), is(OPERATION_TYPE_SUSPEND));
    assertThat(userOperationsLog.get(0).getEntityType(), is(EntityTypes.PROCESS_INSTANCE));
    assertThat(userOperationsLog.get(0).getOrgValue(), nullValue());
    assertThat(userOperationsLog.get(0).getNewValue(), is(SuspensionState.SUSPENDED.getName()));
    assertThat(userOperationsLog.get(0).getTimestamp(), notNullValue());
    assertThat(userOperationsLog.get(0).getProcessDefinitionKey(), is("process"));
    assertThat(userOperationsLog.get(0).getProcessDefinitionId(), notNullValue());
    assertThat(userOperationsLog.get(0).getProcessInstanceId(), is(processInstance.getId()));
    assertThat(userOperationsLog.get(0).getCategory(), is(CATEGORY_OPERATOR));

    assertThat(userOperationsLog.get(1), notNullValue());
    assertThat(userOperationsLog.get(1).getId(), notNullValue());
    assertThat(userOperationsLog.get(1).getOperationType(), is(OPERATION_TYPE_ACTIVATE));
    assertThat(userOperationsLog.get(1).getEntityType(), is(EntityTypes.PROCESS_INSTANCE));
    assertThat(userOperationsLog.get(1).getOrgValue(), nullValue());
    assertThat(userOperationsLog.get(1).getNewValue(), is(SuspensionState.ACTIVE.getName()));
    assertThat(userOperationsLog.get(1).getTimestamp(), notNullValue());
    assertThat(userOperationsLog.get(1).getProcessDefinitionKey(), is("process"));
    assertThat(userOperationsLog.get(1).getProcessDefinitionId(), notNullValue());
    assertThat(userOperationsLog.get(1).getProcessInstanceId(), is(processInstance.getId()));
    assertThat(userOperationsLog.get(1).getCategory(), is(CATEGORY_OPERATOR));
  }

  @Test
  public void getHistoricUserOperationLogs_suspendProcessInstanceByProcessDefinitionId() {
    // given
    Date now = new Date();
    ClockUtil.setCurrentTime(now);
    final ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("process");

    Date nowPlus2Seconds = new Date(now.getTime() + 2000L);
    ClockUtil.setCurrentTime(nowPlus2Seconds);
    runtimeService.suspendProcessInstanceByProcessDefinitionId(processInstance.getProcessDefinitionId());
    Date nowPlus4Seconds = new Date(now.getTime() + 4000L);
    ClockUtil.setCurrentTime(nowPlus4Seconds);
    runtimeService.activateProcessInstanceByProcessDefinitionId(processInstance.getProcessDefinitionId());

    // when
    List<UserOperationLogEntry> userOperationsLog =
      optimizeService.getHistoricUserOperationLogs(pastDate(), null, 10);

    // then
    assertThat(userOperationsLog.size(), is(2));
    assertThat(userOperationsLog.get(0), notNullValue());
    assertThat(userOperationsLog.get(0).getId(), notNullValue());
    assertThat(userOperationsLog.get(0).getOperationType(), is(OPERATION_TYPE_SUSPEND));
    assertThat(userOperationsLog.get(0).getEntityType(), is(EntityTypes.PROCESS_INSTANCE));
    assertThat(userOperationsLog.get(0).getOrgValue(), nullValue());
    assertThat(userOperationsLog.get(0).getNewValue(), is(SuspensionState.SUSPENDED.getName()));
    assertThat(userOperationsLog.get(0).getTimestamp(), notNullValue());
    assertThat(userOperationsLog.get(0).getProcessDefinitionKey(), is("process"));
    assertThat(userOperationsLog.get(0).getProcessDefinitionId(), notNullValue());
    assertThat(userOperationsLog.get(0).getProcessInstanceId(), nullValue());
    assertThat(userOperationsLog.get(0).getCategory(), is(CATEGORY_OPERATOR));

    assertThat(userOperationsLog.get(1), notNullValue());
    assertThat(userOperationsLog.get(1).getId(), notNullValue());
    assertThat(userOperationsLog.get(1).getOperationType(), is(OPERATION_TYPE_ACTIVATE));
    assertThat(userOperationsLog.get(1).getEntityType(), is(EntityTypes.PROCESS_INSTANCE));
    assertThat(userOperationsLog.get(1).getOrgValue(), nullValue());
    assertThat(userOperationsLog.get(1).getNewValue(), is(SuspensionState.ACTIVE.getName()));
    assertThat(userOperationsLog.get(1).getTimestamp(), notNullValue());
    assertThat(userOperationsLog.get(1).getProcessDefinitionKey(), is("process"));
    assertThat(userOperationsLog.get(1).getProcessDefinitionId(), notNullValue());
    assertThat(userOperationsLog.get(1).getProcessInstanceId(), nullValue());
    assertThat(userOperationsLog.get(1).getCategory(), is(CATEGORY_OPERATOR));
  }

  @Test
  public void getHistoricUserOperationLogs_suspendProcessInstanceByProcessDefinitionKey() {
    // given
    Date now = new Date();
    ClockUtil.setCurrentTime(now);
    runtimeService.startProcessInstanceByKey("process");

    Date nowPlus2Seconds = new Date(now.getTime() + 2000L);
    ClockUtil.setCurrentTime(nowPlus2Seconds);
    runtimeService.suspendProcessInstanceByProcessDefinitionKey("process");
    Date nowPlus4Seconds = new Date(now.getTime() + 4000L);
    ClockUtil.setCurrentTime(nowPlus4Seconds);
    runtimeService.activateProcessInstanceByProcessDefinitionKey("process");

    // when
    List<UserOperationLogEntry> userOperationsLog =
      optimizeService.getHistoricUserOperationLogs(pastDate(), null, 10);

    // then
    assertThat(userOperationsLog.size(), is(2));
    assertThat(userOperationsLog.get(0), notNullValue());
    assertThat(userOperationsLog.get(0).getId(), notNullValue());
    assertThat(userOperationsLog.get(0).getOperationType(), is(OPERATION_TYPE_SUSPEND));
    assertThat(userOperationsLog.get(0).getEntityType(), is(EntityTypes.PROCESS_INSTANCE));
    assertThat(userOperationsLog.get(0).getOrgValue(), nullValue());
    assertThat(userOperationsLog.get(0).getNewValue(), is(SuspensionState.SUSPENDED.getName()));
    assertThat(userOperationsLog.get(0).getTimestamp(), notNullValue());
    assertThat(userOperationsLog.get(0).getProcessDefinitionKey(), is("process"));
    assertThat(userOperationsLog.get(0).getProcessDefinitionId(), nullValue());
    assertThat(userOperationsLog.get(0).getProcessInstanceId(), nullValue());
    assertThat(userOperationsLog.get(0).getCategory(), is(CATEGORY_OPERATOR));

    assertThat(userOperationsLog.get(1), notNullValue());
    assertThat(userOperationsLog.get(1).getId(), notNullValue());
    assertThat(userOperationsLog.get(1).getOperationType(), is(OPERATION_TYPE_ACTIVATE));
    assertThat(userOperationsLog.get(1).getEntityType(), is(EntityTypes.PROCESS_INSTANCE));
    assertThat(userOperationsLog.get(1).getOrgValue(), nullValue());
    assertThat(userOperationsLog.get(1).getNewValue(), is(SuspensionState.ACTIVE.getName()));
    assertThat(userOperationsLog.get(1).getTimestamp(), notNullValue());
    assertThat(userOperationsLog.get(1).getProcessDefinitionKey(), is("process"));
    assertThat(userOperationsLog.get(1).getProcessDefinitionId(), nullValue());
    assertThat(userOperationsLog.get(1).getProcessInstanceId(), nullValue());
    assertThat(userOperationsLog.get(1).getCategory(), is(CATEGORY_OPERATOR));
  }

  @Test
  public void getHistoricUserOperationLogs_suspendProcessDefinitionById() {
    // given
    Date now = new Date();
    ClockUtil.setCurrentTime(now);
    final ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("process");

    Date nowPlus2Seconds = new Date(now.getTime() + 2000L);
    ClockUtil.setCurrentTime(nowPlus2Seconds);
    repositoryService.suspendProcessDefinitionById(processInstance.getProcessDefinitionId(), true, null);
    Date nowPlus4Seconds = new Date(now.getTime() + 4000L);
    ClockUtil.setCurrentTime(nowPlus4Seconds);
    repositoryService.activateProcessDefinitionById(processInstance.getProcessDefinitionId(), true, null);

    // when
    List<UserOperationLogEntry> userOperationsLog =
      optimizeService.getHistoricUserOperationLogs(pastDate(), null, 10);

    // then
    assertThat(userOperationsLog.size(), is(4));
    List<String> newPossibleValue = new ArrayList<>(Arrays.asList(
      SuspensionState.SUSPENDED.getName(),
      SuspensionState.ACTIVE.getName(),
      "true",
      "true"
    ));

    assertThat(userOperationsLog.get(0).getOperationType(), is(OPERATION_TYPE_SUSPEND_PROCESS_DEFINITION));
    assertThat(userOperationsLog.get(0).getEntityType(), is(EntityTypes.PROCESS_DEFINITION));
    assertThat(userOperationsLog.get(0).getOrgValue(), nullValue());
    assertTrue(newPossibleValue.remove(userOperationsLog.get(0).getNewValue()));
    assertThat(userOperationsLog.get(0).getTimestamp(), notNullValue());
    assertThat(userOperationsLog.get(0).getProcessDefinitionKey(), is("process"));
    assertThat(userOperationsLog.get(0).getProcessDefinitionId(), is(processInstance.getProcessDefinitionId()));
    assertThat(userOperationsLog.get(0).getProcessInstanceId(), nullValue());
    assertThat(userOperationsLog.get(0).getCategory(), is(CATEGORY_OPERATOR));

    assertThat(userOperationsLog.get(1).getOperationType(), is(OPERATION_TYPE_SUSPEND_PROCESS_DEFINITION));
    assertThat(userOperationsLog.get(1).getEntityType(), is(EntityTypes.PROCESS_DEFINITION));
    assertThat(userOperationsLog.get(1).getOrgValue(), nullValue());
    assertTrue(newPossibleValue.remove(userOperationsLog.get(1).getNewValue()));
    assertThat(userOperationsLog.get(1).getTimestamp(), notNullValue());
    assertThat(userOperationsLog.get(1).getProcessDefinitionKey(), is("process"));
    assertThat(userOperationsLog.get(1).getProcessDefinitionId(), is(processInstance.getProcessDefinitionId()));
    assertThat(userOperationsLog.get(1).getProcessInstanceId(), nullValue());
    assertThat(userOperationsLog.get(1).getCategory(), is(CATEGORY_OPERATOR));

    assertThat(userOperationsLog.get(2).getOperationType(), is(OPERATION_TYPE_ACTIVATE_PROCESS_DEFINITION));
    assertThat(userOperationsLog.get(2).getEntityType(), is(EntityTypes.PROCESS_DEFINITION));
    assertThat(userOperationsLog.get(2).getOrgValue(), nullValue());
    assertTrue(newPossibleValue.remove(userOperationsLog.get(2).getNewValue()));
    assertThat(userOperationsLog.get(2).getTimestamp(), notNullValue());
    assertThat(userOperationsLog.get(2).getProcessDefinitionKey(), is("process"));
    assertThat(userOperationsLog.get(2).getProcessDefinitionId(), is(processInstance.getProcessDefinitionId()));
    assertThat(userOperationsLog.get(2).getProcessInstanceId(), nullValue());
    assertThat(userOperationsLog.get(2).getCategory(), is(CATEGORY_OPERATOR));

    assertThat(userOperationsLog.get(3).getOperationType(), is(OPERATION_TYPE_ACTIVATE_PROCESS_DEFINITION));
    assertThat(userOperationsLog.get(3).getEntityType(), is(EntityTypes.PROCESS_DEFINITION));
    assertThat(userOperationsLog.get(3).getOrgValue(), nullValue());
    assertTrue(newPossibleValue.remove(userOperationsLog.get(3).getNewValue()));
    assertThat(userOperationsLog.get(3).getTimestamp(), notNullValue());
    assertThat(userOperationsLog.get(3).getProcessDefinitionKey(), is("process"));
    assertThat(userOperationsLog.get(3).getProcessDefinitionId(), is(processInstance.getProcessDefinitionId()));
    assertThat(userOperationsLog.get(3).getProcessInstanceId(), nullValue());
    assertThat(userOperationsLog.get(3).getCategory(), is(CATEGORY_OPERATOR));
  }

  @Test
  public void getHistoricUserOperationLogs_suspendProcessDefinitionByKey() {
    // given
    Date now = new Date();
    ClockUtil.setCurrentTime(now);
    runtimeService.startProcessInstanceByKey("process");

    Date nowPlus2Seconds = new Date(now.getTime() + 2000L);
    ClockUtil.setCurrentTime(nowPlus2Seconds);
    repositoryService.suspendProcessDefinitionByKey("process", true, null);
    Date nowPlus4Seconds = new Date(now.getTime() + 4000L);
    ClockUtil.setCurrentTime(nowPlus4Seconds);
    repositoryService.activateProcessDefinitionByKey("process", true, null);

    // when
    List<UserOperationLogEntry> userOperationsLog =
      optimizeService.getHistoricUserOperationLogs(pastDate(), null, 10);

    // then
    assertThat(userOperationsLog.size(), is(4));
    List<String> newPossibleValue = new ArrayList<>(Arrays.asList(
      SuspensionState.SUSPENDED.getName(),
      SuspensionState.ACTIVE.getName(),
      "true",
      "true"
    ));

    assertThat(userOperationsLog.get(0).getOperationType(), is(OPERATION_TYPE_SUSPEND_PROCESS_DEFINITION));
    assertThat(userOperationsLog.get(0).getEntityType(), is(EntityTypes.PROCESS_DEFINITION));
    assertThat(userOperationsLog.get(0).getOrgValue(), nullValue());
    assertTrue(newPossibleValue.remove(userOperationsLog.get(0).getNewValue()));
    assertThat(userOperationsLog.get(0).getTimestamp(), notNullValue());
    assertThat(userOperationsLog.get(0).getProcessDefinitionKey(), is("process"));
    assertThat(userOperationsLog.get(0).getProcessDefinitionId(), nullValue());
    assertThat(userOperationsLog.get(0).getProcessInstanceId(), nullValue());
    assertThat(userOperationsLog.get(0).getCategory(), is(CATEGORY_OPERATOR));

    assertThat(userOperationsLog.get(1).getOperationType(), is(OPERATION_TYPE_SUSPEND_PROCESS_DEFINITION));
    assertThat(userOperationsLog.get(1).getEntityType(), is(EntityTypes.PROCESS_DEFINITION));
    assertThat(userOperationsLog.get(1).getOrgValue(), nullValue());
    assertTrue(newPossibleValue.remove(userOperationsLog.get(1).getNewValue()));
    assertThat(userOperationsLog.get(1).getTimestamp(), notNullValue());
    assertThat(userOperationsLog.get(1).getProcessDefinitionKey(), is("process"));
    assertThat(userOperationsLog.get(1).getProcessDefinitionId(), nullValue());
    assertThat(userOperationsLog.get(1).getProcessInstanceId(), nullValue());
    assertThat(userOperationsLog.get(1).getCategory(), is(CATEGORY_OPERATOR));

    assertThat(userOperationsLog.get(2).getOperationType(), is(OPERATION_TYPE_ACTIVATE_PROCESS_DEFINITION));
    assertThat(userOperationsLog.get(2).getEntityType(), is(EntityTypes.PROCESS_DEFINITION));
    assertThat(userOperationsLog.get(2).getOrgValue(), nullValue());
    assertTrue(newPossibleValue.remove(userOperationsLog.get(2).getNewValue()));
    assertThat(userOperationsLog.get(2).getTimestamp(), notNullValue());
    assertThat(userOperationsLog.get(2).getProcessDefinitionKey(), is("process"));
    assertThat(userOperationsLog.get(2).getProcessDefinitionId(), nullValue());
    assertThat(userOperationsLog.get(2).getProcessInstanceId(), nullValue());
    assertThat(userOperationsLog.get(2).getCategory(), is(CATEGORY_OPERATOR));

    assertThat(userOperationsLog.get(3).getOperationType(), is(OPERATION_TYPE_ACTIVATE_PROCESS_DEFINITION));
    assertThat(userOperationsLog.get(3).getEntityType(), is(EntityTypes.PROCESS_DEFINITION));
    assertThat(userOperationsLog.get(3).getOrgValue(), nullValue());
    assertTrue(newPossibleValue.remove(userOperationsLog.get(3).getNewValue()));
    assertThat(userOperationsLog.get(3).getTimestamp(), notNullValue());
    assertThat(userOperationsLog.get(3).getProcessDefinitionKey(), is("process"));
    assertThat(userOperationsLog.get(3).getProcessDefinitionId(), nullValue());
    assertThat(userOperationsLog.get(3).getProcessInstanceId(), nullValue());
    assertThat(userOperationsLog.get(3).getCategory(), is(CATEGORY_OPERATOR));
  }

  @Test
  public void getHistoricUserOperationLogs_suspendByBatchJobAndProcessInstanceId() {
    // given
    Date now = new Date();
    ClockUtil.setCurrentTime(now);
    final ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("process");

    Date nowPlus2Seconds = new Date(now.getTime() + 2000L);
    ClockUtil.setCurrentTime(nowPlus2Seconds);
    Batch suspendProcess = runtimeService.updateProcessInstanceSuspensionState()
      .byProcessInstanceIds(Collections.singletonList(processInstance.getProcessInstanceId()))
      .suspendAsync();
    helper.completeSeedJobs(suspendProcess);
    helper.executeJobs(suspendProcess);
    Date nowPlus4Seconds = new Date(now.getTime() + 4000L);
    ClockUtil.setCurrentTime(nowPlus4Seconds);
    Batch resumeProcess = runtimeService.updateProcessInstanceSuspensionState()
      .byProcessInstanceIds(Collections.singletonList(processInstance.getProcessInstanceId()))
      .activateAsync();
    helper.completeSeedJobs(resumeProcess);
    helper.executeJobs(resumeProcess);

    // when
    List<UserOperationLogEntry> userOperationsLog =
      optimizeService.getHistoricUserOperationLogs(pastDate(), null, 10);

    // then
    assertThat(userOperationsLog.size(), is(4));
    assertThat(userOperationsLog.get(0).getOperationType(), is(OPERATION_TYPE_SUSPEND_JOB));
    assertThat(userOperationsLog.get(0).getEntityType(), is(EntityTypes.PROCESS_INSTANCE));
    assertThat(userOperationsLog.get(0).getProcessDefinitionKey(), nullValue());
    assertThat(userOperationsLog.get(0).getProcessDefinitionId(), nullValue());
    assertThat(userOperationsLog.get(0).getProcessInstanceId(), nullValue());

    // creates two suspend jobs, one for number of process instances affected and one for the async operation
    assertThat(userOperationsLog.get(1).getOperationType(), is(OPERATION_TYPE_SUSPEND_JOB));
    assertThat(userOperationsLog.get(1).getEntityType(), is(EntityTypes.PROCESS_INSTANCE));

    assertThat(userOperationsLog.get(2), notNullValue());
    assertThat(userOperationsLog.get(2).getOperationType(), is(OPERATION_TYPE_ACTIVATE_JOB));
    assertThat(userOperationsLog.get(2).getEntityType(), is(EntityTypes.PROCESS_INSTANCE));
    assertThat(userOperationsLog.get(2).getOrgValue(), nullValue());
    assertThat(userOperationsLog.get(2).getNewValue(), notNullValue());
    assertThat(userOperationsLog.get(2).getTimestamp(), notNullValue());
    assertThat(userOperationsLog.get(2).getProcessDefinitionKey(), nullValue());
    assertThat(userOperationsLog.get(2).getProcessDefinitionId(), nullValue());
    assertThat(userOperationsLog.get(2).getProcessInstanceId(), nullValue());
    assertThat(userOperationsLog.get(2).getCategory(), is(CATEGORY_OPERATOR));

    // creates two activate jobs, one for number of process instances affected and one for the async operation
    assertThat(userOperationsLog.get(3).getOperationType(), is(OPERATION_TYPE_ACTIVATE_JOB));
    assertThat(userOperationsLog.get(3).getEntityType(), is(EntityTypes.PROCESS_INSTANCE));
  }

  @Test
  public void getHistoricUserOperationLogs_suspendByBatchJobAndQuery() {
    // given
    Date now = new Date();
    ClockUtil.setCurrentTime(now);
    runtimeService.startProcessInstanceByKey("process");

    Date nowPlus2Seconds = new Date(now.getTime() + 2000L);
    ClockUtil.setCurrentTime(nowPlus2Seconds);
    Batch suspendprocess = runtimeService.updateProcessInstanceSuspensionState()
      .byProcessInstanceQuery(runtimeService.createProcessInstanceQuery().active())
      .suspendAsync();
    helper.completeSeedJobs(suspendprocess);
    helper.executeJobs(suspendprocess);
    Date nowPlus4Seconds = new Date(now.getTime() + 4000L);
    ClockUtil.setCurrentTime(nowPlus4Seconds);
    Batch resumeProcess = runtimeService.updateProcessInstanceSuspensionState()
      .byProcessInstanceQuery(runtimeService.createProcessInstanceQuery().suspended())
      .activateAsync();
    helper.completeSeedJobs(resumeProcess);
    helper.executeJobs(resumeProcess);

    // when
    List<UserOperationLogEntry> userOperationsLog =
      optimizeService.getHistoricUserOperationLogs(pastDate(), null, 10);

    // then
    assertThat(userOperationsLog.size(), is(4));
    assertThat(userOperationsLog.get(0).getOperationType(), is(OPERATION_TYPE_SUSPEND_JOB));
    assertThat(userOperationsLog.get(0).getEntityType(), is(EntityTypes.PROCESS_INSTANCE));
    assertThat(userOperationsLog.get(0).getProcessDefinitionKey(), nullValue());
    assertThat(userOperationsLog.get(0).getProcessDefinitionId(), nullValue());
    assertThat(userOperationsLog.get(0).getProcessInstanceId(), nullValue());

    // creates two suspend jobs, one for number of process instances affected and one for the async operation
    assertThat(userOperationsLog.get(1).getOperationType(), is(OPERATION_TYPE_SUSPEND_JOB));
    assertThat(userOperationsLog.get(1).getEntityType(), is(EntityTypes.PROCESS_INSTANCE));

    assertThat(userOperationsLog.get(2), notNullValue());
    assertThat(userOperationsLog.get(2).getOperationType(), is(OPERATION_TYPE_ACTIVATE_JOB));
    assertThat(userOperationsLog.get(2).getEntityType(), is(EntityTypes.PROCESS_INSTANCE));
    assertThat(userOperationsLog.get(2).getOrgValue(), nullValue());
    assertThat(userOperationsLog.get(2).getNewValue(), notNullValue());
    assertThat(userOperationsLog.get(2).getTimestamp(), notNullValue());
    assertThat(userOperationsLog.get(2).getProcessDefinitionKey(), nullValue());
    assertThat(userOperationsLog.get(2).getProcessDefinitionId(), nullValue());
    assertThat(userOperationsLog.get(2).getProcessInstanceId(), nullValue());
    assertThat(userOperationsLog.get(2).getCategory(), is(CATEGORY_OPERATOR));

    // creates two activate jobs, one for number of process instances affected and one for the async operation
    assertThat(userOperationsLog.get(3).getOperationType(), is(OPERATION_TYPE_ACTIVATE_JOB));
    assertThat(userOperationsLog.get(3).getEntityType(), is(EntityTypes.PROCESS_INSTANCE));
  }

  @Test
  public void occurredAfterParameterWorks() {
    // given
    Date now = new Date();
    ClockUtil.setCurrentTime(now);
    final ProcessInstance processInstance = engineRule.getRuntimeService().startProcessInstanceByKey("process");
    runtimeService.suspendProcessInstanceById(processInstance.getProcessInstanceId());

    Date nowPlus2Seconds = new Date(now.getTime() + 2000L);
    ClockUtil.setCurrentTime(nowPlus2Seconds);
    runtimeService.activateProcessInstanceById(processInstance.getProcessInstanceId());

    Date nowPlus4Seconds = new Date(now.getTime() + 4000L);
    ClockUtil.setCurrentTime(nowPlus4Seconds);
    runtimeService.suspendProcessInstanceById(processInstance.getProcessInstanceId());

    // when
    List<UserOperationLogEntry> userOperationsLog =
      optimizeService.getHistoricUserOperationLogs(now, null, 10);

    // then
    Set<String> allowedOperationsTypes = new HashSet<>(Arrays.asList(OPERATION_TYPE_SUSPEND, OPERATION_TYPE_ACTIVATE));
    assertThat(userOperationsLog.size(), is(2));
    assertTrue(allowedOperationsTypes.contains(userOperationsLog.get(0).getOperationType()));
    assertTrue(allowedOperationsTypes.contains(userOperationsLog.get(1).getOperationType()));
  }

  @Test
  public void occurredAtParameterWorks() {
    // given
    Date now = new Date();
    ClockUtil.setCurrentTime(now);
    final ProcessInstance processInstance = engineRule.getRuntimeService().startProcessInstanceByKey("process");
    runtimeService.suspendProcessInstanceById(processInstance.getProcessInstanceId());

    Date nowPlus2Seconds = new Date(now.getTime() + 2000L);
    ClockUtil.setCurrentTime(nowPlus2Seconds);
    runtimeService.activateProcessInstanceById(processInstance.getProcessInstanceId());

    // when
    List<UserOperationLogEntry> userOperationsLog =
      optimizeService.getHistoricUserOperationLogs(null, now, 10);

    // then
    assertThat(userOperationsLog.size(), is(1));
    assertThat(userOperationsLog.get(0).getOperationType(), is(OPERATION_TYPE_SUSPEND));
  }

  @Test
  public void occurredAfterAndOccurredAtParameterWorks() {
    // given
    Date now = new Date();
    ClockUtil.setCurrentTime(now);
    final ProcessInstance processInstance = engineRule.getRuntimeService().startProcessInstanceByKey("process");
    runtimeService.suspendProcessInstanceById(processInstance.getProcessInstanceId());

    Date nowPlus2Seconds = new Date(now.getTime() + 2000L);
    ClockUtil.setCurrentTime(nowPlus2Seconds);
    runtimeService.activateProcessInstanceById(processInstance.getProcessInstanceId());

    // when
    List<UserOperationLogEntry> userOperationsLog =
      optimizeService.getHistoricUserOperationLogs(now, now, 10);

    // then
    assertThat(userOperationsLog.size(), is(0));
  }

  @Test
  public void maxResultsParameterWorks() {
     // given
    final ProcessInstance processInstance = engineRule.getRuntimeService().startProcessInstanceByKey("process");
    runtimeService.suspendProcessInstanceById(processInstance.getProcessInstanceId());
    runtimeService.activateProcessInstanceById(processInstance.getProcessInstanceId());
    runtimeService.suspendProcessInstanceById(processInstance.getProcessInstanceId());
    runtimeService.activateProcessInstanceById(processInstance.getProcessInstanceId());

    // when
    List<UserOperationLogEntry> userOperationsLog =
      optimizeService.getHistoricUserOperationLogs(pastDate(), null, 3);

    // then
    assertThat(userOperationsLog.size(), is(3));
  }

  @Test
  public void resultIsSortedByTimestamp() {
    // given
    Date now = new Date();
    ClockUtil.setCurrentTime(now);
    final ProcessInstance processInstance = engineRule.getRuntimeService().startProcessInstanceByKey("process");
    runtimeService.suspendProcessInstanceById(processInstance.getProcessInstanceId());

    Date nowPlus2Seconds = new Date(now.getTime() + 2000L);
    ClockUtil.setCurrentTime(nowPlus2Seconds);
    runtimeService.activateProcessInstanceById(processInstance.getProcessInstanceId());

    Date nowPlus4Seconds = new Date(now.getTime() + 4000L);
    ClockUtil.setCurrentTime(nowPlus4Seconds);
    runtimeService.suspendProcessInstanceById(processInstance.getProcessInstanceId());

    // when
    List<UserOperationLogEntry> userOperationsLog =
      optimizeService.getHistoricUserOperationLogs(pastDate(), null, 4);

    // then
    assertThat(userOperationsLog.size(), is(3));
    assertThat(userOperationsLog.get(0).getOperationType(), is(OPERATION_TYPE_SUSPEND));
    assertThat(userOperationsLog.get(1).getOperationType(), is(OPERATION_TYPE_ACTIVATE));
    assertThat(userOperationsLog.get(2).getOperationType(), is(OPERATION_TYPE_SUSPEND));
  }

  @Test
  public void fetchOnlyProcessInstanceSuspensionStateBasedLogEntries() {
    // given
    ProcessInstance processInstance = engineRule.getRuntimeService().startProcessInstanceByKey("process");
    createLogEntriesThatShouldNotBeReturned(processInstance.getId());
    assertThat(engineRule.getHistoryService().createUserOperationLogQuery().count(), greaterThan(0L));

    // when
    List<UserOperationLogEntry> userOperationsLog =
      optimizeService.getHistoricUserOperationLogs(pastDate(), null, 10);

    // then
    assertThat(userOperationsLog.size(), is(0));
  }

  private void createLogEntriesThatShouldNotBeReturned(String processInstanceId) {
    ClockUtil.setCurrentTime(new Date());

    String processTaskId = taskService.createTaskQuery().singleResult().getId();

    // create and remove some links
    taskService.addCandidateUser(processTaskId, "er");
    taskService.deleteCandidateUser(processTaskId, "er");
    taskService.addCandidateGroup(processTaskId, "wir");
    taskService.deleteCandidateGroup(processTaskId, "wir");

    // assign and reassign the owner
    taskService.setOwner(processTaskId, "icke");

    // change priority of task
    taskService.setPriority(processTaskId, 10);

    // add and delete an attachment
    Attachment attachment = taskService.createAttachment(
      "image/ico",
      processTaskId,
      processInstanceId,
      "favicon.ico",
      "favicon",
      "http://camunda.com/favicon.ico"
    );
    taskService.deleteAttachment(attachment.getId());
    runtimeService.deleteProcessInstance(processInstanceId, "that's why");

    // create a standalone userTask
    Task userTask = taskService.newTask();
    userTask.setName("to do");
    taskService.saveTask(userTask);

    // change some properties manually to create an update event
    ClockUtil.setCurrentTime(new Date());
    userTask.setDescription("desc");
    userTask.setOwner("icke");
    userTask.setAssignee("er");
    userTask.setDueDate(new Date());
    taskService.saveTask(userTask);

    taskService.deleteTask(userTask.getId(), true);
  }

  protected void createUser(String userId) {
    User user = identityService.newUser(userId);
    identityService.saveUser(user);
  }

  private Date pastDate() {
    return new Date(2L);
  }

  private void deploySimpleDefinition() {
    BpmnModelInstance simpleDefinition = Bpmn.createExecutableProcess("process")
      .startEvent("startEvent")
      .userTask("userTask")
      .name("task")
      .endEvent("endEvent")
      .done();
    testHelper.deploy(simpleDefinition);
  }

}
