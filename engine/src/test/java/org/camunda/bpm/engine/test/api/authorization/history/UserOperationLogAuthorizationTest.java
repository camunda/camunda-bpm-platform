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
import static org.camunda.bpm.engine.authorization.Authorization.ANY;
import static org.camunda.bpm.engine.authorization.Permissions.DELETE_HISTORY;
import static org.camunda.bpm.engine.authorization.Permissions.READ_HISTORY;
import static org.camunda.bpm.engine.authorization.Permissions.UPDATE;
import static org.camunda.bpm.engine.authorization.ProcessDefinitionPermissions.UPDATE_HISTORY;
import static org.camunda.bpm.engine.authorization.Resources.HISTORIC_TASK;
import static org.camunda.bpm.engine.authorization.Resources.OPERATION_LOG_CATEGORY;
import static org.camunda.bpm.engine.authorization.Resources.PROCESS_DEFINITION;
import static org.camunda.bpm.engine.authorization.UserOperationLogCategoryPermissions.DELETE;
import static org.camunda.bpm.engine.authorization.UserOperationLogCategoryPermissions.READ;
import static org.camunda.bpm.engine.history.UserOperationLogEntry.CATEGORY_ADMIN;
import static org.camunda.bpm.engine.history.UserOperationLogEntry.CATEGORY_OPERATOR;
import static org.camunda.bpm.engine.history.UserOperationLogEntry.CATEGORY_TASK_WORKER;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

import java.util.Date;
import java.util.List;

import org.camunda.bpm.engine.AuthorizationException;
import org.camunda.bpm.engine.ProcessEngineConfiguration;
import org.camunda.bpm.engine.authorization.HistoricProcessInstancePermissions;
import org.camunda.bpm.engine.authorization.HistoricTaskPermissions;
import org.camunda.bpm.engine.authorization.ProcessDefinitionPermissions;
import org.camunda.bpm.engine.authorization.Resources;
import org.camunda.bpm.engine.authorization.UserOperationLogCategoryPermissions;
import org.camunda.bpm.engine.history.HistoricIncident;
import org.camunda.bpm.engine.history.HistoricProcessInstance;
import org.camunda.bpm.engine.history.UserOperationLogEntry;
import org.camunda.bpm.engine.history.UserOperationLogQuery;
import org.camunda.bpm.engine.impl.AbstractQuery;
import org.camunda.bpm.engine.impl.context.Context;
import org.camunda.bpm.engine.impl.interceptor.Command;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.interceptor.CommandExecutor;
import org.camunda.bpm.engine.impl.jobexecutor.TimerSuspendProcessDefinitionHandler;
import org.camunda.bpm.engine.impl.persistence.entity.HistoricIncidentEntity;
import org.camunda.bpm.engine.runtime.Job;
import org.camunda.bpm.engine.test.RequiredHistoryLevel;
import org.camunda.bpm.engine.test.api.authorization.AuthorizationTest;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

/**
 * @author Roman Smirnov
 *
 */
@RequiredHistoryLevel(ProcessEngineConfiguration.HISTORY_FULL)
public class UserOperationLogAuthorizationTest extends AuthorizationTest {

  protected static final String ONE_TASK_PROCESS_KEY = "oneTaskProcess";
  protected static final String ONE_TASK_CASE_KEY = "oneTaskCase";
  protected static final String TIMER_BOUNDARY_PROCESS_KEY = "timerBoundaryProcess";

  protected String deploymentId;
  protected String taskId;

  @Before
  public void setUp() throws Exception {
    deploymentId = testRule.deploy(
        "org/camunda/bpm/engine/test/api/oneTaskProcess.bpmn20.xml",
        "org/camunda/bpm/engine/test/api/authorization/oneTaskCase.cmmn",
        "org/camunda/bpm/engine/test/api/authorization/timerBoundaryEventProcess.bpmn20.xml")
            .getId();
    super.setUp();
  }

  @After
  public void tearDown() {
    super.tearDown();
    processEngineConfiguration.setEnableHistoricInstancePermissions(false);

    if (taskId != null) {
      deleteTask(taskId, true);
      taskId = null;

    }
  }

  // standalone task ///////////////////////////////

  @Test
  public void testQueryCreateStandaloneTaskUserOperationLogWithoutAuthorization() {
    // given
    String taskId = "myTask";
    createTask(taskId);

    // when
    UserOperationLogQuery query = historyService.createUserOperationLogQuery();

    // then
    verifyQueryResults(query, 0);

    deleteTask(taskId, true);
  }
  
  @Test
  public void testQueryCreateStandaloneTaskUserOperationLogWithReadHistoryPermissionOnProcessDefinition() {
    // given
    String taskId = "myTask";
    createTask(taskId);
    
    createGrantAuthorizationWithoutAuthentication(PROCESS_DEFINITION, ONE_TASK_PROCESS_KEY, userId, READ_HISTORY);
    
    // when
    UserOperationLogQuery query = historyService.createUserOperationLogQuery();
    
    // then
    verifyQueryResults(query, 0);
    
    deleteTask(taskId, true);
  }
  
  // CAM-9888
  public void failing_testQueryCreateStandaloneTaskUserOperationLogWithReadHistoryPermissionOnAnyProcessDefinition() {
    // given
    String taskId = "myTask";
    createTask(taskId);
    
    createGrantAuthorizationWithoutAuthentication(PROCESS_DEFINITION, ANY, userId, READ_HISTORY);
    
    // when
    UserOperationLogQuery query = historyService.createUserOperationLogQuery();
    
    // then
    verifyQueryResults(query, 0);
    
    deleteTask(taskId, true);
  }
  
  @Test
  public void testQueryCreateStandaloneTaskUserOperationLogWithReadPermissionOnCategory() {
    // given
    String taskId = "myTask";
    createTask(taskId);
    
    createGrantAuthorizationWithoutAuthentication(OPERATION_LOG_CATEGORY, CATEGORY_TASK_WORKER, userId, READ);
    
    // when
    UserOperationLogQuery query = historyService.createUserOperationLogQuery();
    
    // then
    verifyQueryResults(query, 1);
    
    deleteTask(taskId, true);
  }
  
  @Test
  public void testQueryCreateStandaloneTaskUserOperationLogWithReadPermissionOnAnyCategory() {
    // given
    String taskId = "myTask";
    createTask(taskId);
    
    createGrantAuthorizationWithoutAuthentication(OPERATION_LOG_CATEGORY, ANY, userId, READ);
    
    // when
    UserOperationLogQuery query = historyService.createUserOperationLogQuery();
    
    // then
    verifyQueryResults(query, 1);
    
    deleteTask(taskId, true);
  }
  
  @Test
  public void testQueryCreateStandaloneTaskUserOperationLogWithReadPermissionOnAnyCategoryAndRevokeReadHistoryOnProcessDefinition() {
    // given
    String taskId = "myTask";
    createTask(taskId);
    
    createGrantAuthorizationWithoutAuthentication(OPERATION_LOG_CATEGORY, ANY, userId, READ);
    createRevokeAuthorizationWithoutAuthentication(PROCESS_DEFINITION, ONE_TASK_PROCESS_KEY, userId, READ_HISTORY);
    
    // when
    UserOperationLogQuery query = historyService.createUserOperationLogQuery();
    
    // then
    verifyQueryResults(query, 1);// "revoke specific process definition" has no effect since task log is not related to a definition
    
    deleteTask(taskId, true);
  }

  // CAM-9888
  public void failing_testQueryCreateStandaloneTaskUserOperationLogWithReadPermissionOnAnyCategoryAndRevokeReadHistoryOnAnyProcessDefinition() {
    // given
    String taskId = "myTask";
    createTask(taskId);
    
    createGrantAuthorizationWithoutAuthentication(OPERATION_LOG_CATEGORY, ANY, userId, READ);
    createRevokeAuthorizationWithoutAuthentication(PROCESS_DEFINITION, ANY, userId, READ_HISTORY);
    
    // when
    UserOperationLogQuery query = historyService.createUserOperationLogQuery();
    
    // then
    // "grant all categories" should preceed over "revoke all process definitions" 
    verifyQueryResults(query, 1);
    
    deleteTask(taskId, true);
  }

  @Test
  public void testQuerySetAssigneeStandaloneTaskUserOperationLogWithoutAuthorization() {
    // given
    String taskId = "myTask";
    createTask(taskId);
    setAssignee(taskId, "demo");

    // when
    UserOperationLogQuery query = historyService.createUserOperationLogQuery();

    // then
    verifyQueryResults(query, 0);

    deleteTask(taskId, true);
  }
  
  @Test
  public void testQuerySetAssigneeStandaloneTaskUserOperationLogWithReadPermissionOnProcessDefinition() {
    // given
    String taskId = "myTask";
    createTask(taskId);
    setAssignee(taskId, "demo");
    
    createGrantAuthorizationWithoutAuthentication(PROCESS_DEFINITION, ONE_TASK_PROCESS_KEY, userId, READ_HISTORY);
    
    // when
    UserOperationLogQuery query = historyService.createUserOperationLogQuery();
    
    // then
    verifyQueryResults(query, 0);
    
    deleteTask(taskId, true);
  }
  
  // CAM-9888
  public void failing_testQuerySetAssigneeStandaloneTaskUserOperationLogWithReadPermissionOnAnyProcessDefinition() {
    // given
    String taskId = "myTask";
    createTask(taskId);
    setAssignee(taskId, "demo");

    createGrantAuthorizationWithoutAuthentication(PROCESS_DEFINITION, ANY, userId, READ_HISTORY);
    
    // when
    UserOperationLogQuery query = historyService.createUserOperationLogQuery();

    // then
    verifyQueryResults(query, 0);

    deleteTask(taskId, true);
  }
  
  @Test
  public void testQuerySetAssigneeStandaloneTaskUserOperationLogWithReadPermissionOnCategory() {
    // given
    String taskId = "myTask";
    createTask(taskId);
    setAssignee(taskId, "demo");
    
    createGrantAuthorizationWithoutAuthentication(OPERATION_LOG_CATEGORY, CATEGORY_TASK_WORKER, userId, READ);

    // when
    UserOperationLogQuery query = historyService.createUserOperationLogQuery();

    // then
    verifyQueryResults(query, 2);

    deleteTask(taskId, true);
  }
  
  @Test
  public void testQuerySetAssigneeStandaloneTaskUserOperationLogWithReadPermissionOnAnyCategory() {
    // given
    String taskId = "myTask";
    createTask(taskId);
    setAssignee(taskId, "demo");
    
    createGrantAuthorizationWithoutAuthentication(OPERATION_LOG_CATEGORY, ANY, userId, READ);

    // when
    UserOperationLogQuery query = historyService.createUserOperationLogQuery();

    // then
    verifyQueryResults(query, 2);

    deleteTask(taskId, true);
  }

  // (process) user task /////////////////////////////

  @Test
  public void testQuerySetAssigneeTaskUserOperationLogWithoutAuthorization() {
    // given
    startProcessInstanceByKey(ONE_TASK_PROCESS_KEY);
    String taskId = selectSingleTask().getId();
    setAssignee(taskId, "demo");

    // when
    UserOperationLogQuery query = historyService.createUserOperationLogQuery();

    // then
    verifyQueryResults(query, 0);
  }

  @Test
  public void testQuerySetAssigneeTaskUserOperationLogWithReadHistoryPermissionOnProcessDefinition() {
    // given
    startProcessInstanceByKey(ONE_TASK_PROCESS_KEY);
    String taskId = selectSingleTask().getId();
    setAssignee(taskId, "demo");

    createGrantAuthorizationWithoutAuthentication(PROCESS_DEFINITION, ONE_TASK_PROCESS_KEY, userId, READ_HISTORY);

    // when
    UserOperationLogQuery query = historyService.createUserOperationLogQuery();

    // then
    verifyQueryResults(query, 2);
  }

  @Test
  public void testQuerySetAssigneeTaskUserOperationLogWithReadHistoryPermissionOnAnyProcessDefinition() {
    // given
    startProcessInstanceByKey(ONE_TASK_PROCESS_KEY);
    String taskId = selectSingleTask().getId();
    setAssignee(taskId, "demo");

    createGrantAuthorizationWithoutAuthentication(PROCESS_DEFINITION, ANY, userId, READ_HISTORY);

    // when
    UserOperationLogQuery query = historyService.createUserOperationLogQuery();

    // then
    verifyQueryResults(query, 2);
  }

  @Test
  public void testQuerySetAssigneeTaskUserOperationLogWithMultiple() {
    // given
    startProcessInstanceByKey(ONE_TASK_PROCESS_KEY);
    String taskId = selectSingleTask().getId();
    setAssignee(taskId, "demo");

    createGrantAuthorizationWithoutAuthentication(PROCESS_DEFINITION, ANY, userId, READ_HISTORY);
    createGrantAuthorizationWithoutAuthentication(PROCESS_DEFINITION, ONE_TASK_PROCESS_KEY, userId, READ_HISTORY);

    // when
    UserOperationLogQuery query = historyService.createUserOperationLogQuery();

    // then
    verifyQueryResults(query, 2);
  }

  @Test
  public void testCheckNonePermissionOnHistoricProcessInstance() {
    // given
    processEngineConfiguration.setEnableHistoricInstancePermissions(true);

    String processInstanceId = startProcessInstanceByKey(ONE_TASK_PROCESS_KEY)
        .getProcessInstanceId();

    String taskId = selectSingleTask().getId();
    setAssignee(taskId, "demo");

    createGrantAuthorizationWithoutAuthentication(Resources.HISTORIC_PROCESS_INSTANCE, processInstanceId, userId,
        HistoricProcessInstancePermissions.NONE);

    // when
    UserOperationLogQuery query = historyService.createUserOperationLogQuery();

    // then
    assertThat(query.list()).isEmpty();
  }

  @Test
  public void testCheckReadPermissionOnHistoricProcessInstance() {
    // given
    processEngineConfiguration.setEnableHistoricInstancePermissions(true);

    String processInstanceId = startProcessInstanceByKey(ONE_TASK_PROCESS_KEY)
        .getProcessInstanceId();

    String taskId = selectSingleTask().getId();
    setAssignee(taskId, "demo");

    createGrantAuthorizationWithoutAuthentication(Resources.HISTORIC_PROCESS_INSTANCE, processInstanceId, userId,
        HistoricProcessInstancePermissions.READ);

    // when
    UserOperationLogQuery query = historyService.createUserOperationLogQuery();

    // then
    assertThat(query.list())
        .extracting("processInstanceId")
        .containsExactly(processInstanceId, processInstanceId);
  }

  @Test
  public void testCheckNoneOnHistoricProcessInstanceAndReadHistoryPermissionOnProcessDefinition() {
    // given
    processEngineConfiguration.setEnableHistoricInstancePermissions(true);

    String processInstanceId = startProcessInstanceByKey(ONE_TASK_PROCESS_KEY)
        .getProcessInstanceId();

    String taskId = selectSingleTask().getId();
    setAssignee(taskId, "demo");

    createGrantAuthorizationWithoutAuthentication(Resources.HISTORIC_PROCESS_INSTANCE, processInstanceId, userId,
        HistoricProcessInstancePermissions.NONE);
    createGrantAuthorizationWithoutAuthentication(PROCESS_DEFINITION, ONE_TASK_PROCESS_KEY, userId,
        ProcessDefinitionPermissions.READ_HISTORY);

    // when
    UserOperationLogQuery query = historyService.createUserOperationLogQuery();

    // then
    assertThat(query.list())
        .extracting("processInstanceId")
        .containsExactly(processInstanceId, processInstanceId);
  }

  @Test
  public void testCheckReadOnHistoricProcessInstanceAndNonePermissionOnProcessDefinition() {
    // given
    processEngineConfiguration.setEnableHistoricInstancePermissions(true);

    String processInstanceId = startProcessInstanceByKey(ONE_TASK_PROCESS_KEY)
        .getProcessInstanceId();

    String taskId = selectSingleTask().getId();
    setAssignee(taskId, "demo");

    createGrantAuthorizationWithoutAuthentication(Resources.HISTORIC_PROCESS_INSTANCE, processInstanceId, userId,
        HistoricProcessInstancePermissions.READ);
    createGrantAuthorizationWithoutAuthentication(PROCESS_DEFINITION, ONE_TASK_PROCESS_KEY, userId,
        ProcessDefinitionPermissions.NONE);

    // when
    UserOperationLogQuery query = historyService.createUserOperationLogQuery();

    // then
    assertThat(query.list())
        .extracting("processInstanceId")
        .containsExactly(processInstanceId, processInstanceId);
  }

  @Test
  public void testCheckNoneOnHistoricProcessInstanceAndTaskWorkerCategory() {
    // given
    processEngineConfiguration.setEnableHistoricInstancePermissions(true);

    String processInstanceId = startProcessInstanceByKey(ONE_TASK_PROCESS_KEY)
        .getProcessInstanceId();

    String taskId = selectSingleTask().getId();
    setAssignee(taskId, "demo");

    createGrantAuthorizationWithoutAuthentication(Resources.HISTORIC_PROCESS_INSTANCE,
        processInstanceId, userId, HistoricProcessInstancePermissions.NONE);
    createGrantAuthorizationWithoutAuthentication(OPERATION_LOG_CATEGORY, CATEGORY_TASK_WORKER,
        userId, READ);

    // when
    UserOperationLogQuery query = historyService.createUserOperationLogQuery();

    // then
    assertThat(query.list())
        .extracting("processInstanceId")
        .containsExactly(processInstanceId);
  }

  @Test
  public void testCheckReadOnHistoricProcessInstanceAndAdminCategory() {
    // given
    processEngineConfiguration.setEnableHistoricInstancePermissions(true);

    String processInstanceId = startProcessInstanceByKey(ONE_TASK_PROCESS_KEY)
        .getProcessInstanceId();

    String taskId = selectSingleTask().getId();
    setAssignee(taskId, "demo");

    createGrantAuthorizationWithoutAuthentication(Resources.HISTORIC_PROCESS_INSTANCE, processInstanceId, userId,
        HistoricProcessInstancePermissions.READ);
    createGrantAuthorizationWithoutAuthentication(OPERATION_LOG_CATEGORY, CATEGORY_ADMIN, userId, READ);

    // when
    UserOperationLogQuery query = historyService.createUserOperationLogQuery();

    // then
    assertThat(query.list())
        .extracting("processInstanceId")
        .containsExactly(processInstanceId, processInstanceId);
  }

  @Test
  public void testHistoricProcessInstancePermissionsAuthorizationDisabled() {
    // given
    processEngineConfiguration.setEnableHistoricInstancePermissions(true);

    String processInstanceId = startProcessInstanceByKey(ONE_TASK_PROCESS_KEY)
        .getProcessInstanceId();

    String taskId = selectSingleTask().getId();
    setAssignee(taskId, "demo");

    disableAuthorization();

    // when
    UserOperationLogQuery query = historyService.createUserOperationLogQuery()
        .processInstanceId(processInstanceId);

    // then
    assertThat(query.list())
        .extracting("processInstanceId")
        .containsExactly(processInstanceId, processInstanceId);
  }

  @Test
  public void testCheckNonePermissionOnHistoricTask() {
    // given
    processEngineConfiguration.setEnableHistoricInstancePermissions(true);

    startProcessInstanceByKey(ONE_TASK_PROCESS_KEY);

    String taskId = selectSingleTask().getId();
    setAssignee(taskId, "demo");

    createGrantAuthorizationWithoutAuthentication(HISTORIC_TASK, taskId, userId,
        HistoricTaskPermissions.NONE);

    // when
    UserOperationLogQuery query = historyService.createUserOperationLogQuery();

    // then
    assertThat(query.list()).isEmpty();
  }

  @Test
  public void testCheckReadPermissionOnHistoricTask() {
    // given
    processEngineConfiguration.setEnableHistoricInstancePermissions(true);

    startProcessInstanceByKey(ONE_TASK_PROCESS_KEY);
    String taskId = selectSingleTask().getId();
    setAssignee(taskId, "demo");

    createGrantAuthorizationWithoutAuthentication(HISTORIC_TASK, taskId, userId,
        HistoricTaskPermissions.READ);

    // when
    UserOperationLogQuery query = historyService.createUserOperationLogQuery();

    // then
    assertThat(query.list())
        .extracting("taskId")
        .containsExactly(taskId);
  }

  @Test
  public void testCheckReadPermissionOnStandaloneHistoricTask() {
    // given
    processEngineConfiguration.setEnableHistoricInstancePermissions(true);

    taskId = "aTaskId";
    createTask(taskId);

    disableAuthorization();
    taskService.setAssignee(taskId, userId);
    enableAuthorization();

    createGrantAuthorizationWithoutAuthentication(HISTORIC_TASK, taskId, userId,
        HistoricTaskPermissions.READ);

    // when
    UserOperationLogQuery query = historyService.createUserOperationLogQuery();

    // then
    assertThat(query.list())
        .extracting("taskId")
        .containsExactly(taskId, taskId);
  }

  @Test
  public void testCheckNonePermissionOnStandaloneHistoricTask() {
    // given
    processEngineConfiguration.setEnableHistoricInstancePermissions(true);

    taskId = "aTaskId";
    createTask(taskId);
    disableAuthorization();
    taskService.setAssignee(taskId, userId);
    enableAuthorization();

    createGrantAuthorizationWithoutAuthentication(HISTORIC_TASK, taskId, userId,
        HistoricTaskPermissions.NONE);

    // when
    UserOperationLogQuery query = historyService.createUserOperationLogQuery();

    // then
    assertThat(query.list()).isEmpty();
  }

  @Test
  public void testCheckReadPermissionOnCompletedHistoricTask() {
    // given
    processEngineConfiguration.setEnableHistoricInstancePermissions(true);

    startProcessInstanceByKey(ONE_TASK_PROCESS_KEY);
    String taskId = selectSingleTask().getId();
    disableAuthorization();
    taskService.setAssignee(taskId, userId);
    taskService.complete(taskId);
    enableAuthorization();

    createGrantAuthorizationWithoutAuthentication(HISTORIC_TASK, taskId, userId,
        HistoricTaskPermissions.READ);

    // when
    UserOperationLogQuery query = historyService.createUserOperationLogQuery();

    // then
    assertThat(query.list())
        .extracting("taskId")
        .containsExactly(taskId, taskId);
  }

  @Test
  public void testCheckNonePermissionOnHistoricTaskAndReadHistoryPermissionOnProcessDefinition() {
    // given
    processEngineConfiguration.setEnableHistoricInstancePermissions(true);

    startProcessInstanceByKey(ONE_TASK_PROCESS_KEY);
    String taskId = selectSingleTask().getId();
    disableAuthorization();
    taskService.complete(taskId);
    enableAuthorization();

    createGrantAuthorizationWithoutAuthentication(HISTORIC_TASK, taskId, userId,
        HistoricTaskPermissions.NONE);
    createGrantAuthorizationWithoutAuthentication(PROCESS_DEFINITION, ONE_TASK_PROCESS_KEY,
        userId, ProcessDefinitionPermissions.READ_HISTORY);

    // when
    UserOperationLogQuery query = historyService.createUserOperationLogQuery();

    // then
    assertThat(query.list())
        .extracting("taskId")
        .containsExactlyInAnyOrder(taskId, null);
  }

  @Test
  public void testCheckReadPermissionOnHistoricTaskAndNonePermissionOnProcessDefinition() {
    // given
    processEngineConfiguration.setEnableHistoricInstancePermissions(true);

    startProcessInstanceByKey(ONE_TASK_PROCESS_KEY);
    String taskId = selectSingleTask().getId();
    disableAuthorization();
    taskService.complete(taskId);
    enableAuthorization();

    createGrantAuthorizationWithoutAuthentication(HISTORIC_TASK, taskId, userId,
        HistoricTaskPermissions.READ);
    createGrantAuthorizationWithoutAuthentication(PROCESS_DEFINITION, ONE_TASK_PROCESS_KEY, userId,
        ProcessDefinitionPermissions.NONE);

    // when
    UserOperationLogQuery query = historyService.createUserOperationLogQuery();

    // then
    assertThat(query.list())
        .extracting("taskId")
        .containsExactly(taskId);
  }

  @Test
  public void testCheckNoneOnHistoricTaskAndTaskWorkerCategory() {
    // given
    processEngineConfiguration.setEnableHistoricInstancePermissions(true);

    startProcessInstanceByKey(ONE_TASK_PROCESS_KEY);

    String taskId = selectSingleTask().getId();
    setAssignee(taskId, "demo");

    createGrantAuthorizationWithoutAuthentication(Resources.HISTORIC_TASK,
        taskId, userId, HistoricTaskPermissions.NONE);
    createGrantAuthorizationWithoutAuthentication(OPERATION_LOG_CATEGORY, CATEGORY_TASK_WORKER,
        userId, UserOperationLogCategoryPermissions.READ);

    // when
    UserOperationLogQuery query = historyService.createUserOperationLogQuery();

    // then
    assertThat(query.list())
        .extracting("taskId")
        .containsExactly(taskId);
  }

  @Test
  public void testCheckReadOnHistoricTaskAndAdminCategory() {
    // given
    processEngineConfiguration.setEnableHistoricInstancePermissions(true);

    startProcessInstanceByKey(ONE_TASK_PROCESS_KEY);

    String taskId = selectSingleTask().getId();
    setAssignee(taskId, "demo");

    createGrantAuthorizationWithoutAuthentication(Resources.HISTORIC_TASK, taskId, userId,
        HistoricTaskPermissions.READ);
    createGrantAuthorizationWithoutAuthentication(OPERATION_LOG_CATEGORY,
        CATEGORY_ADMIN, userId, UserOperationLogCategoryPermissions.READ);

    // when
    UserOperationLogQuery query = historyService.createUserOperationLogQuery();

    // then
    assertThat(query.list())
        .extracting("taskId")
        .containsExactly(taskId);
  }

  @Test
  public void testHistoricTaskPermissionsAuthorizationDisabled() {
    // given
    processEngineConfiguration.setEnableHistoricInstancePermissions(true);

    startProcessInstanceByKey(ONE_TASK_PROCESS_KEY);
    String taskId = selectSingleTask().getId();
    setAssignee(taskId, "demo");
    disableAuthorization();

    // when
    UserOperationLogQuery query = historyService.createUserOperationLogQuery();

    // then
    assertThat(query.list())
        .extracting("taskId")
        .containsExactlyInAnyOrder(taskId, null);
  }


  @Test
  public void testQuerySetAssigneeTaskUserOperationLogWithReadPermissionOnCategory() {
    // given
    startProcessInstanceByKey(ONE_TASK_PROCESS_KEY);
    String taskId = selectSingleTask().getId();
    setAssignee(taskId, "demo");

    createGrantAuthorizationWithoutAuthentication(OPERATION_LOG_CATEGORY, CATEGORY_TASK_WORKER, userId, READ);

    // when
    UserOperationLogQuery query = historyService.createUserOperationLogQuery();

    // then
    verifyQueryResults(query, 1);
  }

  @Test
  public void testQuerySetAssigneeTaskUserOperationLogWithReadPermissionOnAnyCategory() {
    // given
    startProcessInstanceByKey(ONE_TASK_PROCESS_KEY);
    String taskId = selectSingleTask().getId();
    setAssignee(taskId, "demo");

    createGrantAuthorizationWithoutAuthentication(OPERATION_LOG_CATEGORY, ANY, userId, READ);

    // when
    UserOperationLogQuery query = historyService.createUserOperationLogQuery();

    // then
    verifyQueryResults(query, 2);
  }
  
  @Test
  public void testQuerySetAssigneeTaskUserOperationLogWithReadPermissionOnAnyCategoryAndRevokeOnProcessDefinition() {
    // given
    startProcessInstanceByKey(ONE_TASK_PROCESS_KEY);
    String taskId = selectSingleTask().getId();
    setAssignee(taskId, "demo");

    createGrantAuthorizationWithoutAuthentication(OPERATION_LOG_CATEGORY, ANY, userId, READ);
    createRevokeAuthorizationWithoutAuthentication(PROCESS_DEFINITION, ONE_TASK_PROCESS_KEY, userId, READ_HISTORY);

    // when
    UserOperationLogQuery query = historyService.createUserOperationLogQuery();

    // then
    verifyQueryResults(query, 0);// "revoke process definition" wins over "grant all categories" since task log is related to the definition
  }
  
  @Test
  public void testQuerySetAssigneeTaskUserOperationLogWithReadPermissionOnAnyCategoryAndRevokeOnUnrelatedProcessDefinition() {
    // given
    startProcessInstanceByKey(ONE_TASK_PROCESS_KEY);
    String taskId = selectSingleTask().getId();
    setAssignee(taskId, "demo");

    createGrantAuthorizationWithoutAuthentication(OPERATION_LOG_CATEGORY, ANY, userId, READ);
    createRevokeAuthorizationWithoutAuthentication(PROCESS_DEFINITION, ONE_TASK_CASE_KEY, userId, READ_HISTORY);

    // when
    UserOperationLogQuery query = historyService.createUserOperationLogQuery();

    // then
    verifyQueryResults(query, 2);// "revoke process definition" has no effect since task log is not related to the definition
  }
  
  @Test
  public void testQuerySetAssigneeTaskUserOperationLogWithReadPermissionOnAnyCategoryAndRevokeOnAnyProcessDefinition() {
    // given
    startProcessInstanceByKey(ONE_TASK_PROCESS_KEY);
    String taskId = selectSingleTask().getId();
    setAssignee(taskId, "demo");

    createGrantAuthorizationWithoutAuthentication(OPERATION_LOG_CATEGORY, ANY, userId, READ);
    createRevokeAuthorizationWithoutAuthentication(PROCESS_DEFINITION, ANY, userId, READ_HISTORY);

    // when
    UserOperationLogQuery query = historyService.createUserOperationLogQuery();

    // then
    verifyQueryResults(query, 0);// "revoke all process definitions" wins over "grant all categories"
  }

  // (case) human task /////////////////////////////

  @Test
  public void testQuerySetAssigneeHumanTaskUserOperationLogWithoutAuthorization() {
    // given
    testRule.createCaseInstanceByKey(ONE_TASK_CASE_KEY);
    String taskId = selectSingleTask().getId();
    setAssignee(taskId, "demo");

    // when
    UserOperationLogQuery query = historyService.createUserOperationLogQuery();

    // then
    verifyQueryResults(query, 0);
  }
  
  @Test
  public void testQuerySetAssigneeHumanTaskUserOperationLogWithReadHistoryPermissionOnProcessDefinition() {
    // given
    testRule.createCaseInstanceByKey(ONE_TASK_CASE_KEY);
    String taskId = selectSingleTask().getId();
    setAssignee(taskId, "demo");

    createGrantAuthorizationWithoutAuthentication(PROCESS_DEFINITION, ONE_TASK_CASE_KEY, userId, READ_HISTORY);
    
    // when
    UserOperationLogQuery query = historyService.createUserOperationLogQuery();

    // then
    verifyQueryResults(query, 0);
  }
  
  // CAM-9888
  public void failing_testQuerySetAssigneeHumanTaskUserOperationLogWithReadHistoryPermissionOnAnyProcessDefinition() {
    // given
    testRule.createCaseInstanceByKey(ONE_TASK_CASE_KEY);
    String taskId = selectSingleTask().getId();
    setAssignee(taskId, "demo");

    createGrantAuthorizationWithoutAuthentication(PROCESS_DEFINITION, ANY, userId, READ_HISTORY);
    
    // when
    UserOperationLogQuery query = historyService.createUserOperationLogQuery();

    // then
    verifyQueryResults(query, 0);
  }
  
  @Test
  public void testQuerySetAssigneeHumanTaskUserOperationLogWithReadPermissionOnCategory() {
    // given
    testRule.createCaseInstanceByKey(ONE_TASK_CASE_KEY);
    String taskId = selectSingleTask().getId();
    setAssignee(taskId, "demo");

    createGrantAuthorizationWithoutAuthentication(OPERATION_LOG_CATEGORY, CATEGORY_TASK_WORKER, userId, READ);
    
    // when
    UserOperationLogQuery query = historyService.createUserOperationLogQuery();

    // then
    verifyQueryResults(query, 1);
  }
  
  @Test
  public void testQuerySetAssigneeHumanTaskUserOperationLogWithReadPermissionOnAnyCategory() {
    // given
    testRule.createCaseInstanceByKey(ONE_TASK_CASE_KEY);
    String taskId = selectSingleTask().getId();
    setAssignee(taskId, "demo");

    createGrantAuthorizationWithoutAuthentication(OPERATION_LOG_CATEGORY, ANY, userId, READ);
    
    // when
    UserOperationLogQuery query = historyService.createUserOperationLogQuery();

    // then
    verifyQueryResults(query, 1);
  }

  // standalone job ///////////////////////////////

  @Test
  public void testQuerySetStandaloneJobRetriesUserOperationLogWithoutAuthorization() {
    // given
    disableAuthorization();
    repositoryService.suspendProcessDefinitionByKey(ONE_TASK_PROCESS_KEY, true, new Date());
    enableAuthorization();

    disableAuthorization();
    String jobId = managementService.createJobQuery().singleResult().getId();
    managementService.setJobRetries(jobId, 5);
    enableAuthorization();

    // when
    UserOperationLogQuery query = historyService.createUserOperationLogQuery();

    // then
    verifyQueryResults(query, 0);

    disableAuthorization();
    managementService.deleteJob(jobId);
    enableAuthorization();

    clearDatabase();
  }
  
  @Test
  public void testQuerySetStandaloneJobRetriesUserOperationLogWithReadHistoryPermissionOnProcessDefinition() {
    // given
    disableAuthorization();
    repositoryService.suspendProcessDefinitionByKey(ONE_TASK_PROCESS_KEY, true, new Date());
    enableAuthorization();
    
    disableAuthorization();
    String jobId = managementService.createJobQuery().singleResult().getId();
    managementService.setJobRetries(jobId, 5);
    enableAuthorization();
    
    createGrantAuthorizationWithoutAuthentication(PROCESS_DEFINITION, ONE_TASK_PROCESS_KEY, userId, READ_HISTORY);
    
    // when
    UserOperationLogQuery query = historyService.createUserOperationLogQuery();
    
    // then only user operation logs of non standalone jobs are visible
    verifyQueryResults(query, 2);
    assertEquals(ONE_TASK_PROCESS_KEY, query.list().get(0).getProcessDefinitionKey());
    assertEquals(ONE_TASK_PROCESS_KEY, query.list().get(1).getProcessDefinitionKey());

    disableAuthorization();
    managementService.deleteJob(jobId);
    enableAuthorization();
    
    clearDatabase();
  }

  @Ignore("CAM-9888")
  @Test
  public void testQuerySetStandaloneJobRetriesUserOperationLogWithReadHistoryPermissionOnAnyProcessDefinition() {
    // given
    disableAuthorization();
    identityService.clearAuthentication();
    repositoryService.suspendProcessDefinitionByKey(ONE_TASK_PROCESS_KEY, true, new Date());
    enableAuthorization();

    disableAuthorization();
    identityService.setAuthentication(userId, null);
    String jobId = managementService.createJobQuery().singleResult().getId();
    managementService.setJobRetries(jobId, 5);
    enableAuthorization();

    createGrantAuthorizationWithoutAuthentication(PROCESS_DEFINITION, ANY, userId, READ_HISTORY);
    
    // when
    UserOperationLogQuery query = historyService.createUserOperationLogQuery();

    // then only non-stadalone jobs entries
    verifyQueryResults(query, 1);

    disableAuthorization();
    managementService.deleteJob(jobId);
    enableAuthorization();

    clearDatabase();
  }
  
  @Test
  public void testQuerySetStandaloneJobRetriesUserOperationLogWithReadPermissionOnCategory() {
    // given
    disableAuthorization();
    repositoryService.suspendProcessDefinitionByKey(ONE_TASK_PROCESS_KEY, true, new Date());
    enableAuthorization();

    disableAuthorization();
    String jobId = managementService.createJobQuery().singleResult().getId();
    managementService.setJobRetries(jobId, 5);
    enableAuthorization();

    createGrantAuthorizationWithoutAuthentication(OPERATION_LOG_CATEGORY, CATEGORY_OPERATOR, userId, READ);
    
    // when
    UserOperationLogQuery query = historyService.createUserOperationLogQuery();

    // then expect 3 entries (due to necessary permission on 'Operator' category, the definition
    // suspension, as well as the flag for related instances suspension can be seen as well)
    verifyQueryResults(query, 3);

    disableAuthorization();
    managementService.deleteJob(jobId);
    enableAuthorization();

    clearDatabase();
  }
  
  @Test
  public void testQuerySetStandaloneJobRetriesUserOperationLogWithReadPermissionOnAnyCategory() {
    // given
    disableAuthorization();
    repositoryService.suspendProcessDefinitionByKey(ONE_TASK_PROCESS_KEY, true, new Date());
    enableAuthorization();

    disableAuthorization();
    String jobId = managementService.createJobQuery().singleResult().getId();
    managementService.setJobRetries(jobId, 5);
    enableAuthorization();

    createGrantAuthorizationWithoutAuthentication(OPERATION_LOG_CATEGORY, ANY, userId, READ);
    
    // when
    UserOperationLogQuery query = historyService.createUserOperationLogQuery();

    // then
    verifyQueryResults(query, 3);

    disableAuthorization();
    managementService.deleteJob(jobId);
    enableAuthorization();

    clearDatabase();
  }

  @Test
  public void testQuerySetStandaloneJobRetriesUserOperationLogWithReadPermissionOnWrongCategory() {
    // given
    disableAuthorization();
    repositoryService.suspendProcessDefinitionByKey(ONE_TASK_PROCESS_KEY, true, new Date());
    enableAuthorization();

    disableAuthorization();
    String jobId = managementService.createJobQuery().singleResult().getId();
    managementService.setJobRetries(jobId, 5);
    enableAuthorization();

    createGrantAuthorizationWithoutAuthentication(OPERATION_LOG_CATEGORY, CATEGORY_TASK_WORKER, userId, READ);
    
    // when
    UserOperationLogQuery query = historyService.createUserOperationLogQuery();

    // then
    verifyQueryResults(query, 0);

    disableAuthorization();
    managementService.deleteJob(jobId);
    enableAuthorization();

    clearDatabase();
  }

  // job ///////////////////////////////

  @Test
  public void testQuerySetJobRetriesUserOperationLogWithoutAuthorization() {
    // given
    startProcessInstanceByKey(TIMER_BOUNDARY_PROCESS_KEY);
    String jobId = selectSingleJob().getId();

    disableAuthorization();
    managementService.setJobRetries(jobId, 5);
    enableAuthorization();

    // when
    UserOperationLogQuery query = historyService.createUserOperationLogQuery();

    // then
    verifyQueryResults(query, 0);
  }

  @Test
  public void testQuerySetJobRetriesUserOperationLogWithReadHistoryPermissionOnProcessDefinition() {
    // given
    startProcessInstanceByKey(TIMER_BOUNDARY_PROCESS_KEY);
    String jobId = selectSingleJob().getId();

    disableAuthorization();
    managementService.setJobRetries(jobId, 5);
    enableAuthorization();

    createGrantAuthorizationWithoutAuthentication(PROCESS_DEFINITION, TIMER_BOUNDARY_PROCESS_KEY, userId, READ_HISTORY);

    // when
    UserOperationLogQuery query = historyService.createUserOperationLogQuery();

    // then
    verifyQueryResults(query, 2);
  }

  @Test
  public void testQuerySetJobRetriesUserOperationLogWithReadHistoryPermissionOnAnyProcessDefinition() {
    // given
    startProcessInstanceByKey(TIMER_BOUNDARY_PROCESS_KEY);
    String jobId = selectSingleJob().getId();

    disableAuthorization();
    managementService.setJobRetries(jobId, 5);
    enableAuthorization();

    createGrantAuthorizationWithoutAuthentication(PROCESS_DEFINITION, ANY, userId, READ_HISTORY);

    // when
    UserOperationLogQuery query = historyService.createUserOperationLogQuery();

    // then
    verifyQueryResults(query, 2);
  }
  
  @Test
  public void testQuerySetJobRetriesUserOperationLogWithReadPermissionOnCategory() {
    // given
    startProcessInstanceByKey(TIMER_BOUNDARY_PROCESS_KEY);
    String jobId = selectSingleJob().getId();

    disableAuthorization();
    managementService.setJobRetries(jobId, 5);
    enableAuthorization();

    createGrantAuthorizationWithoutAuthentication(OPERATION_LOG_CATEGORY, CATEGORY_OPERATOR, userId, READ);

    // when
    UserOperationLogQuery query = historyService.createUserOperationLogQuery();

    // then
    verifyQueryResults(query, 2);
  }

  @Test
  public void testQuerySetJobRetriesUserOperationLogWithReadPermissionOnAnyCategory() {
    // given
    startProcessInstanceByKey(TIMER_BOUNDARY_PROCESS_KEY);
    String jobId = selectSingleJob().getId();

    disableAuthorization();
    managementService.setJobRetries(jobId, 5);
    enableAuthorization();

    createGrantAuthorizationWithoutAuthentication(OPERATION_LOG_CATEGORY, ANY, userId, READ);

    // when
    UserOperationLogQuery query = historyService.createUserOperationLogQuery();

    // then
    verifyQueryResults(query, 2);
  }

  // process definition ////////////////////////////////////////////

  @Test
  public void testQuerySuspendProcessDefinitionUserOperationLogWithoutAuthorization() {
    // given
    suspendProcessDefinitionByKey(ONE_TASK_PROCESS_KEY);

    // when
    UserOperationLogQuery query = historyService.createUserOperationLogQuery();

    // then
    verifyQueryResults(query, 0);

    clearDatabase();
  }

  @Test
  public void testQuerySuspendProcessDefinitionUserOperationLogWithReadHistoryPermissionOnProcessDefinition() {
    // given
    suspendProcessDefinitionByKey(ONE_TASK_PROCESS_KEY);
    createGrantAuthorizationWithoutAuthentication(PROCESS_DEFINITION, ONE_TASK_PROCESS_KEY, userId, READ_HISTORY);

    // when
    UserOperationLogQuery query = historyService.createUserOperationLogQuery();

    // then
    verifyQueryResults(query, 2);

    clearDatabase();
  }

  @Test
  public void testQuerySuspendProcessDefinitionUserOperationLogWithReadHistoryPermissionOnAnyProcessDefinition() {
    // given
    suspendProcessDefinitionByKey(ONE_TASK_PROCESS_KEY);
    createGrantAuthorizationWithoutAuthentication(PROCESS_DEFINITION, ANY, userId, READ_HISTORY);

    // when
    UserOperationLogQuery query = historyService.createUserOperationLogQuery();

    // then
    verifyQueryResults(query, 2);

    clearDatabase();
  }
  
  @Test
  public void testQuerySuspendProcessDefinitionUserOperationLogWithReadHPermissionOnCategory() {
    // given
    suspendProcessDefinitionByKey(ONE_TASK_PROCESS_KEY);
    createGrantAuthorizationWithoutAuthentication(OPERATION_LOG_CATEGORY, CATEGORY_OPERATOR, userId, READ);

    // when
    UserOperationLogQuery query = historyService.createUserOperationLogQuery();

    // then
    verifyQueryResults(query, 2);

    clearDatabase();
  }
  
  @Test
  public void testQuerySuspendProcessDefinitionUserOperationLogWithReadHPermissionOnAnyCategory() {
    // given
    suspendProcessDefinitionByKey(ONE_TASK_PROCESS_KEY);
    createGrantAuthorizationWithoutAuthentication(OPERATION_LOG_CATEGORY, ANY, userId, READ);

    // when
    UserOperationLogQuery query = historyService.createUserOperationLogQuery();

    // then
    verifyQueryResults(query, 2);

    clearDatabase();
  }

  // process instance //////////////////////////////////////////////

  @Test
  public void testQuerySuspendProcessInstanceUserOperationLogWithoutAuthorization() {
    // given
    String processInstanceId = startProcessInstanceByKey(ONE_TASK_PROCESS_KEY).getId();
    suspendProcessInstanceById(processInstanceId);

    // when
    UserOperationLogQuery query = historyService.createUserOperationLogQuery();

    // then
    verifyQueryResults(query, 0);

    clearDatabase();
  }

  @Test
  public void testQuerySuspendProcessInstanceUserOperationLogWithReadHistoryPermissionOnProcessDefinition() {
    // given
    String processInstanceId = startProcessInstanceByKey(ONE_TASK_PROCESS_KEY).getId();
    suspendProcessInstanceById(processInstanceId);

    createGrantAuthorizationWithoutAuthentication(PROCESS_DEFINITION, ONE_TASK_PROCESS_KEY, userId, READ_HISTORY);

    // when
    UserOperationLogQuery query = historyService.createUserOperationLogQuery();

    // then
    verifyQueryResults(query, 2);

    clearDatabase();
  }

  @Test
  public void testQuerySuspendProcessInstanceUserOperationLogWithReadHistoryPermissionOnAnyProcessDefinition() {
    // given
    String processInstanceId = startProcessInstanceByKey(ONE_TASK_PROCESS_KEY).getId();
    suspendProcessInstanceById(processInstanceId);

    createGrantAuthorizationWithoutAuthentication(PROCESS_DEFINITION, ANY, userId, READ_HISTORY);

    // when
    UserOperationLogQuery query = historyService.createUserOperationLogQuery();

    // then
    verifyQueryResults(query, 2);

    clearDatabase();
  }
  
  @Test
  public void testQuerySuspendProcessInstanceUserOperationLogWithReadPermissionOnCategory() {
    // given
    String processInstanceId = startProcessInstanceByKey(ONE_TASK_PROCESS_KEY).getId();
    suspendProcessInstanceById(processInstanceId);

    createGrantAuthorizationWithoutAuthentication(OPERATION_LOG_CATEGORY, CATEGORY_OPERATOR, userId, READ);

    // when
    UserOperationLogQuery query = historyService.createUserOperationLogQuery();

    // then
    verifyQueryResults(query, 2);

    clearDatabase();
  }
  
  @Test
  public void testQuerySuspendProcessInstanceUserOperationLogWithReadPermissionOnAnyCategory() {
    // given
    String processInstanceId = startProcessInstanceByKey(ONE_TASK_PROCESS_KEY).getId();
    suspendProcessInstanceById(processInstanceId);

    createGrantAuthorizationWithoutAuthentication(OPERATION_LOG_CATEGORY, ANY, userId, READ);

    // when
    UserOperationLogQuery query = historyService.createUserOperationLogQuery();

    // then
    verifyQueryResults(query, 2);

    clearDatabase();
  }

  // delete deployment (cascade = false)

  @Test
  public void testQueryAfterDeletingDeploymentWithoutAuthorization() {
    // given
    startProcessInstanceByKey(ONE_TASK_PROCESS_KEY);
    String taskId = selectSingleTask().getId();
    setAssignee(taskId, "demo");

    disableAuthorization();
    taskService.complete(taskId);
    enableAuthorization();

    deleteDeployment(deploymentId, false);

    // when
    UserOperationLogQuery query = historyService.createUserOperationLogQuery();

    // then
    verifyQueryResults(query, 0);

    disableAuthorization();
    List<HistoricProcessInstance> instances = historyService.createHistoricProcessInstanceQuery().list();
    for (HistoricProcessInstance instance : instances) {
      historyService.deleteHistoricProcessInstance(instance.getId());
    }
    enableAuthorization();
  }
  
  @Test
  public void testQueryAfterDeletingDeploymentWithReadHistoryPermissionOnProcessDefinition() {
    // given
    startProcessInstanceByKey(ONE_TASK_PROCESS_KEY);
    String taskId = selectSingleTask().getId();
    setAssignee(taskId, "demo");
    createGrantAuthorizationWithoutAuthentication(PROCESS_DEFINITION, ONE_TASK_PROCESS_KEY, userId, READ_HISTORY);

    disableAuthorization();
    taskService.complete(taskId);
    enableAuthorization();

    deleteDeployment(deploymentId, false);

    // when
    UserOperationLogQuery query = historyService.createUserOperationLogQuery();

    // then
    verifyQueryResults(query, 3);

    disableAuthorization();
    List<HistoricProcessInstance> instances = historyService.createHistoricProcessInstanceQuery().list();
    for (HistoricProcessInstance instance : instances) {
      historyService.deleteHistoricProcessInstance(instance.getId());
    }
    enableAuthorization();
  }
  
  @Test
  public void testQueryAfterDeletingDeploymentWithReadHistoryPermissionOnAnyProcessDefinition() {
    // given
    startProcessInstanceByKey(ONE_TASK_PROCESS_KEY);
    String taskId = selectSingleTask().getId();
    setAssignee(taskId, "demo");
    createGrantAuthorizationWithoutAuthentication(PROCESS_DEFINITION, ANY, userId, READ_HISTORY);

    disableAuthorization();
    taskService.complete(taskId);
    enableAuthorization();

    deleteDeployment(deploymentId, false);

    // when
    UserOperationLogQuery query = historyService.createUserOperationLogQuery();

    // then
    verifyQueryResults(query, 3);

    disableAuthorization();
    List<HistoricProcessInstance> instances = historyService.createHistoricProcessInstanceQuery().list();
    for (HistoricProcessInstance instance : instances) {
      historyService.deleteHistoricProcessInstance(instance.getId());
    }
    enableAuthorization();
  }
  
  @Test
  public void testQueryAfterDeletingDeploymentWithReadPermissionOnCategory() {
    // given
    startProcessInstanceByKey(ONE_TASK_PROCESS_KEY);
    String taskId = selectSingleTask().getId();
    setAssignee(taskId, "demo");

    disableAuthorization();
    taskService.complete(taskId);
    enableAuthorization();

    deleteDeployment(deploymentId, false);

    // when
    createGrantAuthorizationWithoutAuthentication(OPERATION_LOG_CATEGORY, CATEGORY_OPERATOR, userId, READ);
    UserOperationLogQuery query = historyService.createUserOperationLogQuery();

    // then expect 1 entry (start process instance)
    verifyQueryResults(query, 1);
    
    // and when
    createGrantAuthorizationWithoutAuthentication(OPERATION_LOG_CATEGORY, CATEGORY_TASK_WORKER, userId, READ);
    
    // then expect 3 entries (start process instance, set assignee, complete task)
    verifyQueryResults(query, 3);

    disableAuthorization();
    List<HistoricProcessInstance> instances = historyService.createHistoricProcessInstanceQuery().list();
    for (HistoricProcessInstance instance : instances) {
      historyService.deleteHistoricProcessInstance(instance.getId());
    }
    enableAuthorization();
  }
  
  @Test
  public void testQueryAfterDeletingDeploymentWithReadPermissionOnAnyCategory() {
    // given
    startProcessInstanceByKey(ONE_TASK_PROCESS_KEY);
    String taskId = selectSingleTask().getId();
    setAssignee(taskId, "demo");
    createGrantAuthorizationWithoutAuthentication(OPERATION_LOG_CATEGORY, ANY, userId, READ);

    disableAuthorization();
    taskService.complete(taskId);
    enableAuthorization();

    deleteDeployment(deploymentId, false);

    // when
    UserOperationLogQuery query = historyService.createUserOperationLogQuery();

    // then
    verifyQueryResults(query, 3);

    disableAuthorization();
    List<HistoricProcessInstance> instances = historyService.createHistoricProcessInstanceQuery().list();
    for (HistoricProcessInstance instance : instances) {
      historyService.deleteHistoricProcessInstance(instance.getId());
    }
    enableAuthorization();
  }

  // delete user operation log (standalone) ////////////////////////

  @Test
  public void testDeleteStandaloneEntryWithoutAuthorization() {
    // given
    String taskId = "myTask";
    createTask(taskId);
    
    disableAuthorization();
    String entryId = historyService.createUserOperationLogQuery().singleResult().getId();
    enableAuthorization();
    
    // when
    try {
      historyService.deleteUserOperationLogEntry(entryId);
      fail("Exception expected: It should not be possible to delete the user operation log");
    } catch (AuthorizationException e) {
      // then
      String message = e.getMessage();
      testRule.assertTextPresent(userId, message);
      testRule.assertTextPresent(DELETE.getName(), message);
      testRule.assertTextPresent(OPERATION_LOG_CATEGORY.resourceName(), message);
      testRule.assertTextPresent(CATEGORY_TASK_WORKER, message);
    }
    
    deleteTask(taskId, true);
  }
  
  @Test
  public void testDeleteStandaloneEntryWithDeleteHistoryPermissionOnProcessDefinition() {
    // given
    String taskId = "myTask";
    createTask(taskId);

    disableAuthorization();
    String entryId = historyService.createUserOperationLogQuery().singleResult().getId();
    enableAuthorization();
    
    createGrantAuthorizationWithoutAuthentication(PROCESS_DEFINITION, ONE_TASK_PROCESS_KEY, userId, DELETE_HISTORY);

    // when
    try {
      historyService.deleteUserOperationLogEntry(entryId);
      fail("Exception expected: It should not be possible to delete the user operation log");
    } catch (AuthorizationException e) {
      // then
      String message = e.getMessage();
      testRule.assertTextPresent(userId, message);
      testRule.assertTextPresent(DELETE.getName(), message);
      testRule.assertTextPresent(OPERATION_LOG_CATEGORY.resourceName(), message);
      testRule.assertTextPresent(CATEGORY_TASK_WORKER, message);
    }
    
    deleteTask(taskId, true);
  }
  
  @Test
  public void testDeleteStandaloneEntryWithDeleteHistoryPermissionOnAnyProcessDefinition() {
    // given
    String taskId = "myTask";
    createTask(taskId);

    disableAuthorization();
    String entryId = historyService.createUserOperationLogQuery().singleResult().getId();
    enableAuthorization();
    
    createGrantAuthorizationWithoutAuthentication(PROCESS_DEFINITION, ANY, userId, DELETE_HISTORY);

    // when
    try {
      historyService.deleteUserOperationLogEntry(entryId);
      fail("Exception expected: It should not be possible to delete the user operation log");
    } catch (AuthorizationException e) {
      // then
      String message = e.getMessage();
      testRule.assertTextPresent(userId, message);
      testRule.assertTextPresent(DELETE.getName(), message);
      testRule.assertTextPresent(OPERATION_LOG_CATEGORY.resourceName(), message);
      testRule.assertTextPresent(CATEGORY_TASK_WORKER, message);
    }
    
    deleteTask(taskId, true);
  }
  
  @Test
  public void testDeleteStandaloneEntryWithDeletePermissionOnCategory() {
    // given
    String taskId = "myTask";
    createTask(taskId);

    disableAuthorization();
    String entryId = historyService.createUserOperationLogQuery().singleResult().getId();
    enableAuthorization();
    
    createGrantAuthorizationWithoutAuthentication(OPERATION_LOG_CATEGORY, CATEGORY_TASK_WORKER, userId, DELETE);

    // when
    historyService.deleteUserOperationLogEntry(entryId);

    // then
    assertNull(historyService.createUserOperationLogQuery().singleResult());

    deleteTask(taskId, true);
  }
  
  @Test
  public void testDeleteStandaloneEntryWithDeletePermissionOnAnyCategory() {
    // given
    String taskId = "myTask";
    createTask(taskId);

    disableAuthorization();
    String entryId = historyService.createUserOperationLogQuery().singleResult().getId();
    enableAuthorization();
    
    createGrantAuthorizationWithoutAuthentication(OPERATION_LOG_CATEGORY, ANY, userId, DELETE);

    // when
    historyService.deleteUserOperationLogEntry(entryId);

    // then
    assertNull(historyService.createUserOperationLogQuery().singleResult());

    deleteTask(taskId, true);
  }

  // delete user operation log /////////////////////////////////////

  @Test
  public void testDeleteEntryWithoutAuthorization() {
    // given
    startProcessInstanceByKey(ONE_TASK_PROCESS_KEY);
    String taskId = selectSingleTask().getId();
    setAssignee(taskId, "demo");

    disableAuthorization();
    String entryId = historyService.createUserOperationLogQuery().entityType("Task").singleResult().getId();
    enableAuthorization();

    try {
      // when
      historyService.deleteUserOperationLogEntry(entryId);
      fail("Exception expected: It should not be possible to delete the user operation log");
    } catch (AuthorizationException e) {
      // then
      String message = e.getMessage();
      testRule.assertTextPresent(userId, message);
      testRule.assertTextPresent(DELETE_HISTORY.getName(), message);
      testRule.assertTextPresent(ONE_TASK_PROCESS_KEY, message);
      testRule.assertTextPresent(PROCESS_DEFINITION.resourceName(), message);
      testRule.assertTextPresent(DELETE.getName(), message);
      testRule.assertTextPresent(OPERATION_LOG_CATEGORY.resourceName(), message);
      testRule.assertTextPresent(CATEGORY_TASK_WORKER, message);
    }
  }

  @Test
  public void testDeleteEntryWithDeleteHistoryPermissionOnProcessDefinition() {
    // given
    startProcessInstanceByKey(ONE_TASK_PROCESS_KEY);
    String taskId = selectSingleTask().getId();
    setAssignee(taskId, "demo");
    createGrantAuthorization(PROCESS_DEFINITION, ONE_TASK_PROCESS_KEY, userId, DELETE_HISTORY);

    disableAuthorization();
    String entryId = historyService.createUserOperationLogQuery().entityType("Task").singleResult().getId();
    enableAuthorization();

    // when
    historyService.deleteUserOperationLogEntry(entryId);

    // then
    disableAuthorization();
    assertNull(historyService.createUserOperationLogQuery().entityType("Task").singleResult());
    enableAuthorization();
  }

  @Test
  public void testDeleteEntryWithDeleteHistoryPermissionOnAnyProcessDefinition() {
    // given
    startProcessInstanceByKey(ONE_TASK_PROCESS_KEY);
    String taskId = selectSingleTask().getId();
    setAssignee(taskId, "demo");
    createGrantAuthorization(PROCESS_DEFINITION, ANY, userId, DELETE_HISTORY);

    disableAuthorization();
    String entryId = historyService.createUserOperationLogQuery().entityType("Task").singleResult().getId();
    enableAuthorization();

    // when
    historyService.deleteUserOperationLogEntry(entryId);

    // then
    disableAuthorization();
    assertNull(historyService.createUserOperationLogQuery().entityType("Task").singleResult());
    enableAuthorization();
  }
  
  @Test
  public void testDeleteEntryWithDeletePermissionOnCategory() {
    // given
    startProcessInstanceByKey(ONE_TASK_PROCESS_KEY);
    String taskId = selectSingleTask().getId();
    setAssignee(taskId, "demo");
    createGrantAuthorization(OPERATION_LOG_CATEGORY, CATEGORY_TASK_WORKER, userId, DELETE);

    disableAuthorization();
    String entryId = historyService.createUserOperationLogQuery().entityType("Task").singleResult().getId();
    enableAuthorization();

    // when
    historyService.deleteUserOperationLogEntry(entryId);

    // then
    disableAuthorization();
    assertNull(historyService.createUserOperationLogQuery().entityType("Task").singleResult());
    enableAuthorization();
  }
  
  @Test
  public void testDeleteEntryWithDeletePermissionOnAnyCategory() {
    // given
    startProcessInstanceByKey(ONE_TASK_PROCESS_KEY);
    String taskId = selectSingleTask().getId();
    setAssignee(taskId, "demo");
    createGrantAuthorization(OPERATION_LOG_CATEGORY, ANY, userId, DELETE);

    disableAuthorization();
    String entryId = historyService.createUserOperationLogQuery().entityType("Task").singleResult().getId();
    enableAuthorization();

    // when
    historyService.deleteUserOperationLogEntry(entryId);

    // then
    disableAuthorization();
    assertNull(historyService.createUserOperationLogQuery().entityType("Task").singleResult());
    enableAuthorization();
  }

  @Test
  public void testDeleteEntryAfterDeletingDeployment() {
    // given
    String processInstanceId = startProcessInstanceByKey(ONE_TASK_PROCESS_KEY).getId();
    String taskId = selectSingleTask().getId();
    createGrantAuthorization(PROCESS_DEFINITION, ONE_TASK_PROCESS_KEY, userId, READ_HISTORY, DELETE_HISTORY);

    disableAuthorization();
    taskService.complete(taskId);
    enableAuthorization();

    deleteDeployment(deploymentId, false);

    String entryId = historyService.createUserOperationLogQuery().entityType("Task").singleResult().getId();

    // when
    historyService.deleteUserOperationLogEntry(entryId);

    // then
    disableAuthorization();
    assertNull(historyService.createUserOperationLogQuery().entityType("Task").singleResult());
    enableAuthorization();

    disableAuthorization();
    historyService.deleteHistoricProcessInstance(processInstanceId);
    enableAuthorization();
  }

  // delete user operation log (case) //////////////////////////////

  @Test
  public void testCaseDeleteEntryWithoutAuthorization() {
    // given
    testRule.createCaseInstanceByKey(ONE_TASK_CASE_KEY);
    String taskId = selectSingleTask().getId();
    setAssignee(taskId, "demo");
    
    disableAuthorization();
    String entryId = historyService.createUserOperationLogQuery().singleResult().getId();
    enableAuthorization();
    
    // when
    try {
      historyService.deleteUserOperationLogEntry(entryId);
      fail("Exception expected: It should not be possible to delete the user operation log");
    } catch (AuthorizationException e) {
      // then
      String message = e.getMessage();
      testRule.assertTextPresent(userId, message);
      testRule.assertTextPresent(DELETE.getName(), message);
      testRule.assertTextPresent(OPERATION_LOG_CATEGORY.resourceName(), message);
      testRule.assertTextPresent(CATEGORY_TASK_WORKER, message);
    }
  }
  
  @Test
  public void testCaseDeleteEntryWithDeleteHistoryPermissionOnProcessDefinition() {
    // given
    testRule.createCaseInstanceByKey(ONE_TASK_CASE_KEY);
    String taskId = selectSingleTask().getId();
    setAssignee(taskId, "demo");
    
    disableAuthorization();
    String entryId = historyService.createUserOperationLogQuery().singleResult().getId();
    enableAuthorization();
    
    createGrantAuthorizationWithoutAuthentication(PROCESS_DEFINITION, ONE_TASK_CASE_KEY, userId, DELETE_HISTORY);
    
    // when
    try {
      historyService.deleteUserOperationLogEntry(entryId);
      fail("Exception expected: It should not be possible to delete the user operation log");
    } catch (AuthorizationException e) {
      // then
      String message = e.getMessage();
      testRule.assertTextPresent(userId, message);
      testRule.assertTextPresent(DELETE.getName(), message);
      testRule.assertTextPresent(OPERATION_LOG_CATEGORY.resourceName(), message);
      testRule.assertTextPresent(CATEGORY_TASK_WORKER, message);
    }
  }
  
  @Test
  public void testCaseDeleteEntryWithDeleteHistoryPermissionOnAnyProcessDefinition() {
    // given
    testRule.createCaseInstanceByKey(ONE_TASK_CASE_KEY);
    String taskId = selectSingleTask().getId();
    setAssignee(taskId, "demo");
    
    disableAuthorization();
    String entryId = historyService.createUserOperationLogQuery().singleResult().getId();
    enableAuthorization();
    
    createGrantAuthorizationWithoutAuthentication(PROCESS_DEFINITION, ANY, userId, DELETE_HISTORY);
    
    // when
    try {
      historyService.deleteUserOperationLogEntry(entryId);
      fail("Exception expected: It should not be possible to delete the user operation log");
    } catch (AuthorizationException e) {
      // then
      String message = e.getMessage();
      testRule.assertTextPresent(userId, message);
      testRule.assertTextPresent(DELETE.getName(), message);
      testRule.assertTextPresent(OPERATION_LOG_CATEGORY.resourceName(), message);
      testRule.assertTextPresent(CATEGORY_TASK_WORKER, message);
    }
  }
  
  @Test
  public void testCaseDeleteEntryWithDeletePermissionOnCategory() {
    // given
    testRule.createCaseInstanceByKey(ONE_TASK_CASE_KEY);
    String taskId = selectSingleTask().getId();
    setAssignee(taskId, "demo");
    
    disableAuthorization();
    String entryId = historyService.createUserOperationLogQuery().singleResult().getId();
    enableAuthorization();
    
    createGrantAuthorizationWithoutAuthentication(OPERATION_LOG_CATEGORY, CATEGORY_TASK_WORKER, userId, DELETE);
    
    // when
    historyService.deleteUserOperationLogEntry(entryId);
    
    // then
    assertNull(historyService.createUserOperationLogQuery().singleResult());
  }
  
  @Test
  public void testCaseDeleteEntryWithDeletePermissionOnAnyCategory() {
    // given
    testRule.createCaseInstanceByKey(ONE_TASK_CASE_KEY);
    String taskId = selectSingleTask().getId();
    setAssignee(taskId, "demo");
    
    disableAuthorization();
    String entryId = historyService.createUserOperationLogQuery().singleResult().getId();
    enableAuthorization();
    
    createGrantAuthorizationWithoutAuthentication(OPERATION_LOG_CATEGORY, ANY, userId, DELETE);
    
    // when
    historyService.deleteUserOperationLogEntry(entryId);
    
    // then
    assertNull(historyService.createUserOperationLogQuery().singleResult());
  }

  // update user operation log //////////////////////////////

  @Test
  public void testUpdateEntryWithUpdateHistoryPermissionOnProcessDefinition() {
    // given
    startProcessInstanceByKey(ONE_TASK_PROCESS_KEY);

    String taskId = selectSingleTask().getId();
    setAssignee(taskId, "demo");

    createGrantAuthorization(PROCESS_DEFINITION, ONE_TASK_PROCESS_KEY, userId, UPDATE_HISTORY);

    disableAuthorization();

    String operationId = historyService.createUserOperationLogQuery()
        .entityType("Task")
        .singleResult()
        .getOperationId();

    enableAuthorization();

    // when
    historyService.setAnnotationForOperationLogById(operationId, "anAnnotation");

    disableAuthorization();

    UserOperationLogEntry userOperationLogEntry = historyService.createUserOperationLogQuery()
        .entityType("Task")
        .singleResult();

    enableAuthorization();

    // then
    testRule.assertTextPresent(userOperationLogEntry.getAnnotation(), "anAnnotation");
  }

  @Test
  public void testUpdateEntryWithUpdateHistoryPermissionOnAnyProcessDefinition() {
    // given
    startProcessInstanceByKey(ONE_TASK_PROCESS_KEY);

    String taskId = selectSingleTask().getId();
    setAssignee(taskId, "demo");

    createGrantAuthorization(PROCESS_DEFINITION, ANY, userId, UPDATE_HISTORY);

    disableAuthorization();

    String operationId = historyService.createUserOperationLogQuery()
        .entityType("Task")
        .singleResult()
        .getOperationId();

    enableAuthorization();

    // when
    historyService.setAnnotationForOperationLogById(operationId, "anAnnotation");

    disableAuthorization();

    UserOperationLogEntry userOperationLogEntry = historyService.createUserOperationLogQuery()
        .entityType("Task")
        .singleResult();

    enableAuthorization();

    // then
    testRule.assertTextPresent(userOperationLogEntry.getAnnotation(), "anAnnotation");
  }


  @Test
  public void testUpdateEntryWithUpdateHistoryPermissionOnAnyProcessDefinition_Standalone() {
    // given
    createTask("aTaskId");

    createGrantAuthorization(PROCESS_DEFINITION, ANY, userId, UPDATE_HISTORY);

    disableAuthorization();

    String operationId = historyService.createUserOperationLogQuery()
        .entityType("Task")
        .singleResult()
        .getOperationId();

    enableAuthorization();

    try {
      // when
      historyService.setAnnotationForOperationLogById(operationId, "anAnnotation");

      fail("Exception expected: It should not be possible to update the user operation log");
    } catch (AuthorizationException e) {
      // then
      String message = e.getMessage();
      testRule.assertTextPresent(userId, message);
      testRule.assertTextPresent(UPDATE.getName(), message);
      testRule.assertTextPresent(OPERATION_LOG_CATEGORY.resourceName(), message);
      testRule.assertTextPresent(CATEGORY_TASK_WORKER, message);
    }

    // cleanup
    deleteTask("aTaskId", true);
  }

  @Test
  public void testUpdateEntryRelatedToProcessDefinitionWithUpdatePermissionOnCategory() {
    // given
    startProcessInstanceByKey(ONE_TASK_PROCESS_KEY);

    String taskId = selectSingleTask().getId();
    setAssignee(taskId, "demo");

    createGrantAuthorization(OPERATION_LOG_CATEGORY, CATEGORY_TASK_WORKER, userId, UserOperationLogCategoryPermissions.UPDATE);

    disableAuthorization();

    String operationId = historyService.createUserOperationLogQuery()
        .entityType("Task")
        .singleResult()
        .getOperationId();

    enableAuthorization();

    // when
    historyService.setAnnotationForOperationLogById(operationId, "anAnnotation");

    disableAuthorization();

    UserOperationLogEntry userOperationLogEntry = historyService.createUserOperationLogQuery()
        .entityType("Task")
        .singleResult();

    enableAuthorization();

    // then
    testRule.assertTextPresent(userOperationLogEntry.getAnnotation(), "anAnnotation");
  }

  @Test
  public void testUpdateEntryRelatedToProcessDefinitionWithUpdatePermissionOnAnyCategory() {
    // given
    startProcessInstanceByKey(ONE_TASK_PROCESS_KEY);

    String taskId = selectSingleTask().getId();
    setAssignee(taskId, "demo");

    createGrantAuthorization(OPERATION_LOG_CATEGORY, ANY, userId, UserOperationLogCategoryPermissions.UPDATE);

    disableAuthorization();

    String operationId = historyService.createUserOperationLogQuery()
        .entityType("Task")
        .singleResult()
        .getOperationId();

    enableAuthorization();

    // when
    historyService.setAnnotationForOperationLogById(operationId, "anAnnotation");

    disableAuthorization();

    UserOperationLogEntry userOperationLogEntry = historyService.createUserOperationLogQuery()
        .entityType("Task")
        .singleResult();

    enableAuthorization();

    // then
    testRule.assertTextPresent(userOperationLogEntry.getAnnotation(), "anAnnotation");
  }

  @Test
  public void testUpdateEntryWithoutAuthorization() {
    // given
    createTask("aTaskId");

    disableAuthorization();

    String operationId = historyService.createUserOperationLogQuery()
        .entityType("Task")
        .singleResult()
        .getOperationId();

    enableAuthorization();

    try {
      // when
      historyService.setAnnotationForOperationLogById(operationId, "anAnnotation");

      fail("Exception expected: It should not be possible to update the user operation log");
    } catch (AuthorizationException e) {
      // then
      String message = e.getMessage();
      testRule.assertTextPresent(userId, message);
      testRule.assertTextPresent(UPDATE.getName(), message);
      testRule.assertTextPresent(OPERATION_LOG_CATEGORY.resourceName(), message);
      testRule.assertTextPresent(CATEGORY_TASK_WORKER, message);
    }

    // cleanup
    deleteTask("aTaskId", true);
  }

  @Test
  public void testUpdateEntryWithUpdatePermissionOnCategory() {
    // given
    createTask("aTaskId");

    createGrantAuthorization(OPERATION_LOG_CATEGORY, CATEGORY_TASK_WORKER, userId, UserOperationLogCategoryPermissions.UPDATE);

    disableAuthorization();

    String operationId = historyService.createUserOperationLogQuery()
        .entityType("Task")
        .singleResult()
        .getOperationId();

    enableAuthorization();

    // when
    historyService.setAnnotationForOperationLogById(operationId, "anAnnotation");

    disableAuthorization();

    UserOperationLogEntry userOperationLogEntry = historyService.createUserOperationLogQuery()
        .entityType("Task")
        .singleResult();

    enableAuthorization();

    // then
    testRule.assertTextPresent(userOperationLogEntry.getAnnotation(), "anAnnotation");

    // cleanup
    deleteTask("aTaskId", true);
  }

  @Test
  public void testUpdateEntryWithUpdatePermissionOnAnyCategory() {
    // given
    createTask("aTaskId");

    createGrantAuthorization(OPERATION_LOG_CATEGORY, ANY, userId, UserOperationLogCategoryPermissions.UPDATE);

    disableAuthorization();

    String operationId = historyService.createUserOperationLogQuery()
        .entityType("Task")
        .singleResult()
        .getOperationId();

    enableAuthorization();

    // when
    historyService.setAnnotationForOperationLogById(operationId, "anAnnotation");

    disableAuthorization();

    UserOperationLogEntry userOperationLogEntry = historyService.createUserOperationLogQuery()
        .entityType("Task")
        .singleResult();

    enableAuthorization();

    // then
    testRule.assertTextPresent(userOperationLogEntry.getAnnotation(), "anAnnotation");

    // cleanup
    deleteTask("aTaskId", true);
  }

  // helper ////////////////////////////////////////////////////////

  protected void verifyQueryResults(UserOperationLogQuery query, int countExpected) {
    verifyQueryResults((AbstractQuery<?, ?>) query, countExpected);
  }

  protected Job selectSingleJob() {
    disableAuthorization();
    Job job = managementService.createJobQuery().singleResult();
    enableAuthorization();
    return job;
  }

  protected void clearDatabase() {
    CommandExecutor commandExecutor = processEngineConfiguration.getCommandExecutorTxRequired();
    commandExecutor.execute(new Command<Object>() {
      public Object execute(CommandContext commandContext) {
        commandContext.getHistoricJobLogManager().deleteHistoricJobLogsByHandlerType(TimerSuspendProcessDefinitionHandler.TYPE);
        List<HistoricIncident> incidents = Context.getProcessEngineConfiguration().getHistoryService().createHistoricIncidentQuery().list();
        for (HistoricIncident incident : incidents) {
          commandContext.getHistoricIncidentManager().delete((HistoricIncidentEntity) incident);
        }
        return null;
      }
    });
  }
}
