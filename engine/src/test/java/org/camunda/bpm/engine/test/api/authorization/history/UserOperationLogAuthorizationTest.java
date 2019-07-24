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

import static org.camunda.bpm.engine.authorization.Authorization.ANY;
import static org.camunda.bpm.engine.authorization.Permissions.DELETE_HISTORY;
import static org.camunda.bpm.engine.authorization.Permissions.READ_HISTORY;
import static org.camunda.bpm.engine.authorization.Permissions.UPDATE;
import static org.camunda.bpm.engine.authorization.ProcessDefinitionPermissions.UPDATE_HISTORY;
import static org.camunda.bpm.engine.authorization.Resources.OPERATION_LOG_CATEGORY;
import static org.camunda.bpm.engine.authorization.Resources.PROCESS_DEFINITION;
import static org.camunda.bpm.engine.authorization.UserOperationLogCategoryPermissions.DELETE;
import static org.camunda.bpm.engine.authorization.UserOperationLogCategoryPermissions.READ;
import static org.camunda.bpm.engine.history.UserOperationLogEntry.CATEGORY_OPERATOR;
import static org.camunda.bpm.engine.history.UserOperationLogEntry.CATEGORY_TASK_WORKER;
import java.util.Date;
import java.util.List;

import org.camunda.bpm.engine.AuthorizationException;
import org.camunda.bpm.engine.ProcessEngineConfiguration;
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
import org.junit.Ignore;

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

  @Override
  public void setUp() throws Exception {
    deploymentId = createDeployment(null,
        "org/camunda/bpm/engine/test/api/oneTaskProcess.bpmn20.xml",
        "org/camunda/bpm/engine/test/api/authorization/oneTaskCase.cmmn",
        "org/camunda/bpm/engine/test/api/authorization/timerBoundaryEventProcess.bpmn20.xml").getId();
    super.setUp();
  }

  @Override
  public void tearDown() {
    super.tearDown();
    deleteDeployment(deploymentId);
  }

  // standalone task ///////////////////////////////

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

  public void testQuerySetAssigneeHumanTaskUserOperationLogWithoutAuthorization() {
    // given
    createCaseInstanceByKey(ONE_TASK_CASE_KEY);
    String taskId = selectSingleTask().getId();
    setAssignee(taskId, "demo");

    // when
    UserOperationLogQuery query = historyService.createUserOperationLogQuery();

    // then
    verifyQueryResults(query, 0);
  }
  
  public void testQuerySetAssigneeHumanTaskUserOperationLogWithReadHistoryPermissionOnProcessDefinition() {
    // given
    createCaseInstanceByKey(ONE_TASK_CASE_KEY);
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
    createCaseInstanceByKey(ONE_TASK_CASE_KEY);
    String taskId = selectSingleTask().getId();
    setAssignee(taskId, "demo");

    createGrantAuthorizationWithoutAuthentication(PROCESS_DEFINITION, ANY, userId, READ_HISTORY);
    
    // when
    UserOperationLogQuery query = historyService.createUserOperationLogQuery();

    // then
    verifyQueryResults(query, 0);
  }
  
  public void testQuerySetAssigneeHumanTaskUserOperationLogWithReadPermissionOnCategory() {
    // given
    createCaseInstanceByKey(ONE_TASK_CASE_KEY);
    String taskId = selectSingleTask().getId();
    setAssignee(taskId, "demo");

    createGrantAuthorizationWithoutAuthentication(OPERATION_LOG_CATEGORY, CATEGORY_TASK_WORKER, userId, READ);
    
    // when
    UserOperationLogQuery query = historyService.createUserOperationLogQuery();

    // then
    verifyQueryResults(query, 1);
  }
  
  public void testQuerySetAssigneeHumanTaskUserOperationLogWithReadPermissionOnAnyCategory() {
    // given
    createCaseInstanceByKey(ONE_TASK_CASE_KEY);
    String taskId = selectSingleTask().getId();
    setAssignee(taskId, "demo");

    createGrantAuthorizationWithoutAuthentication(OPERATION_LOG_CATEGORY, ANY, userId, READ);
    
    // when
    UserOperationLogQuery query = historyService.createUserOperationLogQuery();

    // then
    verifyQueryResults(query, 1);
  }

  // standalone job ///////////////////////////////

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
    
    // then only user operation log of non standalone jobs are visible
    verifyQueryResults(query, 1);
    assertEquals(ONE_TASK_PROCESS_KEY, query.singleResult().getProcessDefinitionKey());
    
    disableAuthorization();
    managementService.deleteJob(jobId);
    enableAuthorization();
    
    clearDatabase();
  }

  @Ignore("CAM-9888")
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

    // then expect 2 entries (due to necessary permission on 'Operator' category, the definition suspension can be seen as well)
    verifyQueryResults(query, 2);

    disableAuthorization();
    managementService.deleteJob(jobId);
    enableAuthorization();

    clearDatabase();
  }
  
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
    verifyQueryResults(query, 2);

    disableAuthorization();
    managementService.deleteJob(jobId);
    enableAuthorization();

    clearDatabase();
  }

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

  public void testQuerySuspendProcessDefinitionUserOperationLogWithoutAuthorization() {
    // given
    suspendProcessDefinitionByKey(ONE_TASK_PROCESS_KEY);

    // when
    UserOperationLogQuery query = historyService.createUserOperationLogQuery();

    // then
    verifyQueryResults(query, 0);

    clearDatabase();
  }

  public void testQuerySuspendProcessDefinitionUserOperationLogWithReadHistoryPermissionOnProcessDefinition() {
    // given
    suspendProcessDefinitionByKey(ONE_TASK_PROCESS_KEY);
    createGrantAuthorizationWithoutAuthentication(PROCESS_DEFINITION, ONE_TASK_PROCESS_KEY, userId, READ_HISTORY);

    // when
    UserOperationLogQuery query = historyService.createUserOperationLogQuery();

    // then
    verifyQueryResults(query, 1);

    clearDatabase();
  }

  public void testQuerySuspendProcessDefinitionUserOperationLogWithReadHistoryPermissionOnAnyProcessDefinition() {
    // given
    suspendProcessDefinitionByKey(ONE_TASK_PROCESS_KEY);
    createGrantAuthorizationWithoutAuthentication(PROCESS_DEFINITION, ANY, userId, READ_HISTORY);

    // when
    UserOperationLogQuery query = historyService.createUserOperationLogQuery();

    // then
    verifyQueryResults(query, 1);

    clearDatabase();
  }
  
  public void testQuerySuspendProcessDefinitionUserOperationLogWithReadHPermissionOnCategory() {
    // given
    suspendProcessDefinitionByKey(ONE_TASK_PROCESS_KEY);
    createGrantAuthorizationWithoutAuthentication(OPERATION_LOG_CATEGORY, CATEGORY_OPERATOR, userId, READ);

    // when
    UserOperationLogQuery query = historyService.createUserOperationLogQuery();

    // then
    verifyQueryResults(query, 1);

    clearDatabase();
  }
  
  public void testQuerySuspendProcessDefinitionUserOperationLogWithReadHPermissionOnAnyCategory() {
    // given
    suspendProcessDefinitionByKey(ONE_TASK_PROCESS_KEY);
    createGrantAuthorizationWithoutAuthentication(OPERATION_LOG_CATEGORY, ANY, userId, READ);

    // when
    UserOperationLogQuery query = historyService.createUserOperationLogQuery();

    // then
    verifyQueryResults(query, 1);

    clearDatabase();
  }

  // process instance //////////////////////////////////////////////

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
      assertTextPresent(userId, message);
      assertTextPresent(DELETE.getName(), message);
      assertTextPresent(OPERATION_LOG_CATEGORY.resourceName(), message);
      assertTextPresent(CATEGORY_TASK_WORKER, message);
    }
    
    deleteTask(taskId, true);
  }
  
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
      assertTextPresent(userId, message);
      assertTextPresent(DELETE.getName(), message);
      assertTextPresent(OPERATION_LOG_CATEGORY.resourceName(), message);
      assertTextPresent(CATEGORY_TASK_WORKER, message);
    }
    
    deleteTask(taskId, true);
  }
  
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
      assertTextPresent(userId, message);
      assertTextPresent(DELETE.getName(), message);
      assertTextPresent(OPERATION_LOG_CATEGORY.resourceName(), message);
      assertTextPresent(CATEGORY_TASK_WORKER, message);
    }
    
    deleteTask(taskId, true);
  }
  
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
      assertTextPresent(userId, message);
      assertTextPresent(DELETE_HISTORY.getName(), message);
      assertTextPresent(ONE_TASK_PROCESS_KEY, message);
      assertTextPresent(PROCESS_DEFINITION.resourceName(), message);
      assertTextPresent(DELETE.getName(), message);
      assertTextPresent(OPERATION_LOG_CATEGORY.resourceName(), message);
      assertTextPresent(CATEGORY_TASK_WORKER, message);
    }
  }

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

  public void testCaseDeleteEntryWithoutAuthorization() {
    // given
    createCaseInstanceByKey(ONE_TASK_CASE_KEY);
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
      assertTextPresent(userId, message);
      assertTextPresent(DELETE.getName(), message);
      assertTextPresent(OPERATION_LOG_CATEGORY.resourceName(), message);
      assertTextPresent(CATEGORY_TASK_WORKER, message);
    }
  }
  
  public void testCaseDeleteEntryWithDeleteHistoryPermissionOnProcessDefinition() {
    // given
    createCaseInstanceByKey(ONE_TASK_CASE_KEY);
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
      assertTextPresent(userId, message);
      assertTextPresent(DELETE.getName(), message);
      assertTextPresent(OPERATION_LOG_CATEGORY.resourceName(), message);
      assertTextPresent(CATEGORY_TASK_WORKER, message);
    }
  }
  
  public void testCaseDeleteEntryWithDeleteHistoryPermissionOnAnyProcessDefinition() {
    // given
    createCaseInstanceByKey(ONE_TASK_CASE_KEY);
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
      assertTextPresent(userId, message);
      assertTextPresent(DELETE.getName(), message);
      assertTextPresent(OPERATION_LOG_CATEGORY.resourceName(), message);
      assertTextPresent(CATEGORY_TASK_WORKER, message);
    }
  }
  
  public void testCaseDeleteEntryWithDeletePermissionOnCategory() {
    // given
    createCaseInstanceByKey(ONE_TASK_CASE_KEY);
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
  
  public void testCaseDeleteEntryWithDeletePermissionOnAnyCategory() {
    // given
    createCaseInstanceByKey(ONE_TASK_CASE_KEY);
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
    assertTextPresent(userOperationLogEntry.getAnnotation(), "anAnnotation");
  }

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
    assertTextPresent(userOperationLogEntry.getAnnotation(), "anAnnotation");
  }


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
      assertTextPresent(userId, message);
      assertTextPresent(UPDATE.getName(), message);
      assertTextPresent(OPERATION_LOG_CATEGORY.resourceName(), message);
      assertTextPresent(CATEGORY_TASK_WORKER, message);
    }

    // cleanup
    deleteTask("aTaskId", true);
  }

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
    assertTextPresent(userOperationLogEntry.getAnnotation(), "anAnnotation");
  }

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
    assertTextPresent(userOperationLogEntry.getAnnotation(), "anAnnotation");
  }

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
      assertTextPresent(userId, message);
      assertTextPresent(UPDATE.getName(), message);
      assertTextPresent(OPERATION_LOG_CATEGORY.resourceName(), message);
      assertTextPresent(CATEGORY_TASK_WORKER, message);
    }

    // cleanup
    deleteTask("aTaskId", true);
  }

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
    assertTextPresent(userOperationLogEntry.getAnnotation(), "anAnnotation");

    // cleanup
    deleteTask("aTaskId", true);
  }

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
    assertTextPresent(userOperationLogEntry.getAnnotation(), "anAnnotation");

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
