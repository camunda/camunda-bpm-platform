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
package org.camunda.bpm.engine.test.api.authorization.history;

import static org.camunda.bpm.engine.authorization.Authorization.ANY;
import static org.camunda.bpm.engine.authorization.Permissions.DELETE_HISTORY;
import static org.camunda.bpm.engine.authorization.Permissions.READ_HISTORY;
import static org.camunda.bpm.engine.authorization.Resources.PROCESS_DEFINITION;

import java.util.Date;
import java.util.List;

import org.camunda.bpm.engine.AuthorizationException;
import org.camunda.bpm.engine.ProcessEngineConfiguration;
import org.camunda.bpm.engine.history.HistoricIncident;
import org.camunda.bpm.engine.history.HistoricProcessInstance;
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

  public void testQueryCreateStandaloneTaskUserOperationLog() {
    // given
    String taskId = "myTask";
    createTask(taskId);

    // when
    UserOperationLogQuery query = historyService.createUserOperationLogQuery();

    // then
    verifyQueryResults(query, 1);

    deleteTask(taskId, true);
  }

  public void testQuerySetAssigneeStandaloneTaskUserOperationLog() {
    // given
    String taskId = "myTask";
    createTask(taskId);
    setAssignee(taskId, "demo");

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

    createGrantAuthorization(PROCESS_DEFINITION, ONE_TASK_PROCESS_KEY, userId, READ_HISTORY);

    // when
    UserOperationLogQuery query = historyService.createUserOperationLogQuery();

    // then
    verifyQueryResults(query, 1);
  }

  public void testQuerySetAssigneeTaskUserOperationLogWithReadHistoryPermissionOnAnyProcessDefinition() {
    // given
    startProcessInstanceByKey(ONE_TASK_PROCESS_KEY);
    String taskId = selectSingleTask().getId();
    setAssignee(taskId, "demo");

    createGrantAuthorization(PROCESS_DEFINITION, ANY, userId, READ_HISTORY);

    // when
    UserOperationLogQuery query = historyService.createUserOperationLogQuery();

    // then
    verifyQueryResults(query, 1);
  }

  public void testQuerySetAssigneeTaskUserOperationLogWithMultiple() {
    // given
    startProcessInstanceByKey(ONE_TASK_PROCESS_KEY);
    String taskId = selectSingleTask().getId();
    setAssignee(taskId, "demo");

    createGrantAuthorization(PROCESS_DEFINITION, ANY, userId, READ_HISTORY);
    createGrantAuthorization(PROCESS_DEFINITION, ONE_TASK_PROCESS_KEY, userId, READ_HISTORY);

    // when
    UserOperationLogQuery query = historyService.createUserOperationLogQuery();

    // then
    verifyQueryResults(query, 1);
  }

  // (case) human task /////////////////////////////

  public void testQuerySetAssigneeHumanTaskUserOperationLog() {
    // given
    createCaseInstanceByKey(ONE_TASK_CASE_KEY);
    String taskId = selectSingleTask().getId();
    setAssignee(taskId, "demo");

    // when
    UserOperationLogQuery query = historyService.createUserOperationLogQuery();

    // then
    verifyQueryResults(query, 1);
  }

  // standalone job ///////////////////////////////

  public void testQuerySetStandaloneJobRetriesUserOperationLog() {
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
    verifyQueryResults(query, 1);

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

    createGrantAuthorization(PROCESS_DEFINITION, TIMER_BOUNDARY_PROCESS_KEY, userId, READ_HISTORY);

    // when
    UserOperationLogQuery query = historyService.createUserOperationLogQuery();

    // then
    verifyQueryResults(query, 1);
  }

  public void testQuerySetJobRetriesUserOperationLogWithReadHistoryPermissionOnAnyProcessDefinition() {
    // given
    startProcessInstanceByKey(TIMER_BOUNDARY_PROCESS_KEY);
    String jobId = selectSingleJob().getId();

    disableAuthorization();
    managementService.setJobRetries(jobId, 5);
    enableAuthorization();

    createGrantAuthorization(PROCESS_DEFINITION, ANY, userId, READ_HISTORY);

    // when
    UserOperationLogQuery query = historyService.createUserOperationLogQuery();

    // then
    verifyQueryResults(query, 1);
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
    createGrantAuthorization(PROCESS_DEFINITION, ONE_TASK_PROCESS_KEY, userId, READ_HISTORY);

    // when
    UserOperationLogQuery query = historyService.createUserOperationLogQuery();

    // then
    verifyQueryResults(query, 1);

    clearDatabase();
  }

  public void testQuerySuspendProcessDefinitionUserOperationLogWithReadHistoryPermissionOnAnyProcessDefinition() {
    // given
    suspendProcessDefinitionByKey(ONE_TASK_PROCESS_KEY);
    createGrantAuthorization(PROCESS_DEFINITION, ANY, userId, READ_HISTORY);

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

    createGrantAuthorization(PROCESS_DEFINITION, ONE_TASK_PROCESS_KEY, userId, READ_HISTORY);

    // when
    UserOperationLogQuery query = historyService.createUserOperationLogQuery();

    // then
    verifyQueryResults(query, 1);

    clearDatabase();
  }

  public void testQuerySuspendProcessInstanceUserOperationLogWithReadHistoryPermissionOnAnyProcessDefinition() {
    // given
    String processInstanceId = startProcessInstanceByKey(ONE_TASK_PROCESS_KEY).getId();
    suspendProcessInstanceById(processInstanceId);

    createGrantAuthorization(PROCESS_DEFINITION, ANY, userId, READ_HISTORY);

    // when
    UserOperationLogQuery query = historyService.createUserOperationLogQuery();

    // then
    verifyQueryResults(query, 1);

    clearDatabase();
  }

  // delete deployment (cascade = false)

  public void testQueryAfterDeletingDeployment() {
    // given
    startProcessInstanceByKey(ONE_TASK_PROCESS_KEY);
    String taskId = selectSingleTask().getId();
    setAssignee(taskId, "demo");
    createGrantAuthorization(PROCESS_DEFINITION, ONE_TASK_PROCESS_KEY, userId, READ_HISTORY);

    disableAuthorization();
    taskService.complete(taskId);
    enableAuthorization();

    deleteDeployment(deploymentId, false);

    // when
    UserOperationLogQuery query = historyService.createUserOperationLogQuery();

    // then
    verifyQueryResults(query, 2);

    disableAuthorization();
    List<HistoricProcessInstance> instances = historyService.createHistoricProcessInstanceQuery().list();
    for (HistoricProcessInstance instance : instances) {
      historyService.deleteHistoricProcessInstance(instance.getId());
    }
    enableAuthorization();
  }

  // delete user operation log (standalone) ////////////////////////

  public void testDeleteStandaloneEntry() {
    // given
    String taskId = "myTask";
    createTask(taskId);

    String entryId = historyService.createUserOperationLogQuery().singleResult().getId();

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
    String entryId = historyService.createUserOperationLogQuery().singleResult().getId();
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
    }
  }

  public void testDeleteEntryWithDeleteHistoryPermissionOnProcessDefinition() {
    // given
    startProcessInstanceByKey(ONE_TASK_PROCESS_KEY);
    String taskId = selectSingleTask().getId();
    setAssignee(taskId, "demo");
    createGrantAuthorization(PROCESS_DEFINITION, ONE_TASK_PROCESS_KEY, userId, DELETE_HISTORY);

    disableAuthorization();
    String entryId = historyService.createUserOperationLogQuery().singleResult().getId();
    enableAuthorization();

    // when
    historyService.deleteUserOperationLogEntry(entryId);

    // then
    disableAuthorization();
    assertNull(historyService.createUserOperationLogQuery().singleResult());
    enableAuthorization();
  }

  public void testDeleteEntryWithDeleteHistoryPermissionOnAnyProcessDefinition() {
    // given
    startProcessInstanceByKey(ONE_TASK_PROCESS_KEY);
    String taskId = selectSingleTask().getId();
    setAssignee(taskId, "demo");
    createGrantAuthorization(PROCESS_DEFINITION, ANY, userId, DELETE_HISTORY);

    disableAuthorization();
    String entryId = historyService.createUserOperationLogQuery().singleResult().getId();
    enableAuthorization();

    // when
    historyService.deleteUserOperationLogEntry(entryId);

    // then
    disableAuthorization();
    assertNull(historyService.createUserOperationLogQuery().singleResult());
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

    String entryId = historyService.createUserOperationLogQuery().singleResult().getId();

    // when
    historyService.deleteUserOperationLogEntry(entryId);

    // then
    disableAuthorization();
    assertNull(historyService.createUserOperationLogQuery().singleResult());
    enableAuthorization();

    disableAuthorization();
    historyService.deleteHistoricProcessInstance(processInstanceId);
    enableAuthorization();
  }

  // delete user operation log (case) //////////////////////////////

  public void testCaseDeleteEntry() {
    // given
    createCaseInstanceByKey(ONE_TASK_CASE_KEY);
    String taskId = selectSingleTask().getId();
    setAssignee(taskId, "demo");

    String entryId = historyService.createUserOperationLogQuery().singleResult().getId();

    // when
    historyService.deleteUserOperationLogEntry(entryId);

    // then
    assertNull(historyService.createUserOperationLogQuery().singleResult());
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
