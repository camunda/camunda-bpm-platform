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

import static org.camunda.bpm.engine.EntityTypes.JOB;
import static org.camunda.bpm.engine.EntityTypes.JOB_DEFINITION;
import static org.camunda.bpm.engine.EntityTypes.PROCESS_DEFINITION;
import static org.camunda.bpm.engine.EntityTypes.PROCESS_INSTANCE;
import static org.camunda.bpm.engine.history.UserOperationLogEntry.ENTITY_TYPE_ATTACHMENT;
import static org.camunda.bpm.engine.history.UserOperationLogEntry.ENTITY_TYPE_IDENTITY_LINK;
import static org.camunda.bpm.engine.history.UserOperationLogEntry.ENTITY_TYPE_TASK;
import static org.camunda.bpm.engine.history.UserOperationLogEntry.OPERATION_TYPE_ACTIVATE;
import static org.camunda.bpm.engine.history.UserOperationLogEntry.OPERATION_TYPE_ACTIVATE_JOB;
import static org.camunda.bpm.engine.history.UserOperationLogEntry.OPERATION_TYPE_ACTIVATE_JOB_DEFINITION;
import static org.camunda.bpm.engine.history.UserOperationLogEntry.OPERATION_TYPE_ACTIVATE_PROCESS_DEFINITION;
import static org.camunda.bpm.engine.history.UserOperationLogEntry.OPERATION_TYPE_ADD_ATTACHMENT;
import static org.camunda.bpm.engine.history.UserOperationLogEntry.OPERATION_TYPE_ADD_GROUP_LINK;
import static org.camunda.bpm.engine.history.UserOperationLogEntry.OPERATION_TYPE_ADD_USER_LINK;
import static org.camunda.bpm.engine.history.UserOperationLogEntry.OPERATION_TYPE_CREATE;
import static org.camunda.bpm.engine.history.UserOperationLogEntry.OPERATION_TYPE_DELETE;
import static org.camunda.bpm.engine.history.UserOperationLogEntry.OPERATION_TYPE_DELETE_ATTACHMENT;
import static org.camunda.bpm.engine.history.UserOperationLogEntry.OPERATION_TYPE_DELETE_GROUP_LINK;
import static org.camunda.bpm.engine.history.UserOperationLogEntry.OPERATION_TYPE_DELETE_USER_LINK;
import static org.camunda.bpm.engine.history.UserOperationLogEntry.OPERATION_TYPE_SET_JOB_RETRIES;
import static org.camunda.bpm.engine.history.UserOperationLogEntry.OPERATION_TYPE_SET_PRIORITY;
import static org.camunda.bpm.engine.history.UserOperationLogEntry.OPERATION_TYPE_SUSPEND;
import static org.camunda.bpm.engine.history.UserOperationLogEntry.OPERATION_TYPE_SUSPEND_JOB;
import static org.camunda.bpm.engine.history.UserOperationLogEntry.OPERATION_TYPE_SUSPEND_JOB_DEFINITION;
import static org.camunda.bpm.engine.history.UserOperationLogEntry.OPERATION_TYPE_SUSPEND_PROCESS_DEFINITION;
import static org.camunda.bpm.engine.history.UserOperationLogEntry.OPERATION_TYPE_UPDATE;
import static org.camunda.bpm.engine.impl.persistence.entity.TaskEntity.ASSIGNEE;
import static org.camunda.bpm.engine.impl.persistence.entity.TaskEntity.OWNER;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.camunda.bpm.engine.EntityTypes;
import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.history.UserOperationLogEntry;
import org.camunda.bpm.engine.history.UserOperationLogQuery;
import org.camunda.bpm.engine.impl.RuntimeServiceImpl;
import org.camunda.bpm.engine.impl.TaskServiceImpl;
import org.camunda.bpm.engine.impl.interceptor.Command;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.interceptor.CommandExecutor;
import org.camunda.bpm.engine.impl.jobexecutor.TimerActivateJobDefinitionHandler;
import org.camunda.bpm.engine.impl.jobexecutor.TimerSuspendProcessDefinitionHandler;
import org.camunda.bpm.engine.impl.util.ClockUtil;
import org.camunda.bpm.engine.repository.ProcessDefinition;
import org.camunda.bpm.engine.runtime.Execution;
import org.camunda.bpm.engine.runtime.Job;
import org.camunda.bpm.engine.runtime.JobQuery;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.task.Attachment;
import org.camunda.bpm.engine.task.Task;
import org.camunda.bpm.engine.test.Deployment;

/**
 * @author Danny Gräf
 */
public class UserOperationLogQueryTest extends AbstractUserOperationLogTest {

  protected static final String ONE_TASK_PROCESS = "org/camunda/bpm/engine/test/history/oneTaskProcess.bpmn20.xml";
  protected static final String ONE_TASK_CASE = "org/camunda/bpm/engine/test/api/cmmn/oneTaskCase.cmmn";

  private ProcessInstance process;
  private Task userTask;
  private Execution execution;
  private String processTaskId;

  // normalize timestamps for databases which do not provide millisecond presision.
  private Date today = new Date((ClockUtil.getCurrentTime().getTime() / 1000) * 1000);
  private Date tomorrow = new Date(((ClockUtil.getCurrentTime().getTime() + 86400000) / 1000) * 1000);
  private Date yesterday = new Date(((ClockUtil.getCurrentTime().getTime() - 86400000) / 1000) * 1000);

  protected void tearDown() throws Exception {
    super.tearDown();

    if (userTask != null) {
      historyService.deleteHistoricTaskInstance(userTask.getId());
    }
  }

  @Deployment(resources = {ONE_TASK_PROCESS})
  public void testQuery() {
    createLogEntries();

    // expect: all entries can be fetched
    assertEquals(17, query().count());

    // entity type
    assertEquals(11, query().entityType(EntityTypes.TASK).count());
    assertEquals(4, query().entityType(EntityTypes.IDENTITY_LINK).count());
    assertEquals(2, query().entityType(EntityTypes.ATTACHMENT).count());
    assertEquals(0, query().entityType("unknown entity type").count());

    // operation type
    assertEquals(1, query().operationType(OPERATION_TYPE_CREATE).count());
    assertEquals(1, query().operationType(OPERATION_TYPE_SET_PRIORITY).count());
    assertEquals(4, query().operationType(OPERATION_TYPE_UPDATE).count());
    assertEquals(1, query().operationType(OPERATION_TYPE_ADD_USER_LINK).count());
    assertEquals(1, query().operationType(OPERATION_TYPE_DELETE_USER_LINK).count());
    assertEquals(1, query().operationType(OPERATION_TYPE_ADD_GROUP_LINK).count());
    assertEquals(1, query().operationType(OPERATION_TYPE_DELETE_GROUP_LINK).count());
    assertEquals(1, query().operationType(OPERATION_TYPE_ADD_ATTACHMENT).count());
    assertEquals(1, query().operationType(OPERATION_TYPE_DELETE_ATTACHMENT).count());

    // process and execution reference
    assertEquals(11, query().processDefinitionId(process.getProcessDefinitionId()).count());
    assertEquals(11, query().processInstanceId(process.getId()).count());
    assertEquals(11, query().executionId(execution.getId()).count());

    // task reference
    assertEquals(11, query().taskId(processTaskId).count());
    assertEquals(6, query().taskId(userTask.getId()).count());

    // user reference
    assertEquals(11, query().userId("icke").count()); // not includes the create operation called by the process
    assertEquals(6, query().userId("er").count());

    // operation ID
    UserOperationLogQuery updates = query().operationType(OPERATION_TYPE_UPDATE);
    String updateOperationId = updates.list().get(0).getOperationId();
    assertEquals(updates.count(), query().operationId(updateOperationId).count());

    // changed properties
    assertEquals(3, query().property(ASSIGNEE).count());
    assertEquals(2, query().property(OWNER).count());

    // ascending order results by time
    List<UserOperationLogEntry> ascLog = query().orderByTimestamp().asc().list();
    for (int i = 0; i < 4; i++) {
      assertTrue(yesterday.getTime()<=ascLog.get(i).getTimestamp().getTime());
    }
    for (int i = 4; i < 12; i++) {
      assertTrue(today.getTime()<=ascLog.get(i).getTimestamp().getTime());
    }
    for (int i = 12; i < 16; i++) {
      assertTrue(tomorrow.getTime()<=ascLog.get(i).getTimestamp().getTime());
    }

    // descending order results by time
    List<UserOperationLogEntry> descLog = query().orderByTimestamp().desc().list();
    for (int i = 0; i < 4; i++) {
      assertTrue(tomorrow.getTime()<=descLog.get(i).getTimestamp().getTime());
    }
    for (int i = 4; i < 11; i++) {
      assertTrue(today.getTime()<=descLog.get(i).getTimestamp().getTime());
    }
    for (int i = 11; i < 15; i++) {
      assertTrue(yesterday.getTime()<=descLog.get(i).getTimestamp().getTime());
    }

    // filter by time, created yesterday
    assertEquals(4, query().beforeTimestamp(today).count());
    // filter by time, created today and before
    assertEquals(12, query().beforeTimestamp(tomorrow).count());
    // filter by time, created today and later
    assertEquals(13, query().afterTimestamp(yesterday).count());
    // filter by time, created tomorrow
    assertEquals(5, query().afterTimestamp(today).count());
    assertEquals(0, query().afterTimestamp(today).beforeTimestamp(yesterday).count());
  }

  @Deployment(resources = {ONE_TASK_PROCESS})
  public void testQueryWithBackwardCompatibility() {
    createLogEntries();

    // expect: all entries can be fetched
    assertEquals(17, query().count());

    // entity type
    assertEquals(11, query().entityType(ENTITY_TYPE_TASK).count());
    assertEquals(4, query().entityType(ENTITY_TYPE_IDENTITY_LINK).count());
    assertEquals(2, query().entityType(ENTITY_TYPE_ATTACHMENT).count());
    assertEquals(0, query().entityType("unknown entity type").count());
  }

  @Deployment(resources = {ONE_TASK_PROCESS})
  public void testQueryProcessInstanceOperationsById() {
    // given
    process = runtimeService.startProcessInstanceByKey("oneTaskProcess");

    // when
    runtimeService.suspendProcessInstanceById(process.getId());
    runtimeService.activateProcessInstanceById(process.getId());

    runtimeService.deleteProcessInstance(process.getId(), "a delete reason");

    // then
    assertEquals(3, query().entityType(PROCESS_INSTANCE).count());

    UserOperationLogEntry deleteEntry = query()
        .entityType(PROCESS_INSTANCE)
        .processInstanceId(process.getId())
        .operationType(OPERATION_TYPE_DELETE)
        .singleResult();

    assertNotNull(deleteEntry);
    assertEquals(process.getId(), deleteEntry.getProcessInstanceId());
    assertNotNull(deleteEntry.getProcessDefinitionId());
    assertEquals("oneTaskProcess", deleteEntry.getProcessDefinitionKey());
    assertEquals(deploymentId, deleteEntry.getDeploymentId());

      UserOperationLogEntry suspendEntry = query()
        .entityType(PROCESS_INSTANCE)
        .processInstanceId(process.getId())
        .operationType(OPERATION_TYPE_SUSPEND)
        .singleResult();

    assertNotNull(suspendEntry);
    assertEquals(process.getId(), suspendEntry.getProcessInstanceId());
    assertNotNull(suspendEntry.getProcessDefinitionId());
    assertEquals("oneTaskProcess", suspendEntry.getProcessDefinitionKey());

    assertEquals("suspensionState", suspendEntry.getProperty());
    assertEquals("suspended", suspendEntry.getNewValue());
    assertNull(suspendEntry.getOrgValue());

    UserOperationLogEntry activateEntry = query()
        .entityType(PROCESS_INSTANCE)
        .processInstanceId(process.getId())
        .operationType(OPERATION_TYPE_ACTIVATE)
        .singleResult();

    assertNotNull(activateEntry);
    assertEquals(process.getId(), activateEntry.getProcessInstanceId());
    assertNotNull(activateEntry.getProcessDefinitionId());
    assertEquals("oneTaskProcess", activateEntry.getProcessDefinitionKey());
    assertEquals(deploymentId, activateEntry.getDeploymentId());

    assertEquals("suspensionState", activateEntry.getProperty());
    assertEquals("active", activateEntry.getNewValue());
    assertNull(activateEntry.getOrgValue());
  }

  @Deployment(resources = {ONE_TASK_PROCESS})
  public void testQueryProcessInstanceOperationsByProcessDefinitionId() {
    // given
    process = runtimeService.startProcessInstanceByKey("oneTaskProcess");

    // when
    runtimeService.suspendProcessInstanceByProcessDefinitionId(process.getProcessDefinitionId());
    runtimeService.activateProcessInstanceByProcessDefinitionId(process.getProcessDefinitionId());

    // then
    assertEquals(2, query().entityType(PROCESS_INSTANCE).count());

    UserOperationLogEntry suspendEntry = query()
        .entityType(PROCESS_INSTANCE)
        .processDefinitionId(process.getProcessDefinitionId())
        .operationType(OPERATION_TYPE_SUSPEND)
        .singleResult();

    assertNotNull(suspendEntry);
    assertEquals(process.getProcessDefinitionId(), suspendEntry.getProcessDefinitionId());
    assertNull(suspendEntry.getProcessInstanceId());
    assertEquals("oneTaskProcess", suspendEntry.getProcessDefinitionKey());
    assertEquals(deploymentId, suspendEntry.getDeploymentId());

    assertEquals("suspensionState", suspendEntry.getProperty());
    assertEquals("suspended", suspendEntry.getNewValue());
    assertNull(suspendEntry.getOrgValue());

    UserOperationLogEntry activateEntry = query()
        .entityType(PROCESS_INSTANCE)
        .processDefinitionId(process.getProcessDefinitionId())
        .operationType(OPERATION_TYPE_ACTIVATE)
        .singleResult();

    assertNotNull(activateEntry);
    assertNull(activateEntry.getProcessInstanceId());
    assertEquals("oneTaskProcess", activateEntry.getProcessDefinitionKey());
    assertEquals(process.getProcessDefinitionId(), activateEntry.getProcessDefinitionId());
    assertEquals(deploymentId, activateEntry.getDeploymentId());

    assertEquals("suspensionState", activateEntry.getProperty());
    assertEquals("active", activateEntry.getNewValue());
    assertNull(activateEntry.getOrgValue());
  }

  @Deployment(resources = {ONE_TASK_PROCESS})
  public void testQueryProcessInstanceOperationsByProcessDefinitionKey() {
    // given
    process = runtimeService.startProcessInstanceByKey("oneTaskProcess");

    // when
    runtimeService.suspendProcessInstanceByProcessDefinitionKey("oneTaskProcess");
    runtimeService.activateProcessInstanceByProcessDefinitionKey("oneTaskProcess");

    // then
    assertEquals(2, query().entityType(PROCESS_INSTANCE).count());

    UserOperationLogEntry suspendEntry = query()
        .entityType(PROCESS_INSTANCE)
        .processDefinitionKey("oneTaskProcess")
        .operationType(OPERATION_TYPE_SUSPEND)
        .singleResult();

    assertNotNull(suspendEntry);
    assertNull(suspendEntry.getProcessInstanceId());
    assertNull(suspendEntry.getProcessDefinitionId());
    assertEquals("oneTaskProcess", suspendEntry.getProcessDefinitionKey());
    assertNull(suspendEntry.getDeploymentId());

    assertEquals("suspensionState", suspendEntry.getProperty());
    assertEquals("suspended", suspendEntry.getNewValue());
    assertNull(suspendEntry.getOrgValue());

    UserOperationLogEntry activateEntry = query()
        .entityType(PROCESS_INSTANCE)
        .processDefinitionKey("oneTaskProcess")
        .operationType(OPERATION_TYPE_ACTIVATE)
        .singleResult();

    assertNotNull(activateEntry);
    assertNull(activateEntry.getProcessInstanceId());
    assertNull(activateEntry.getProcessDefinitionId());
    assertEquals("oneTaskProcess", activateEntry.getProcessDefinitionKey());
    assertNull(activateEntry.getDeploymentId());

    assertEquals("suspensionState", activateEntry.getProperty());
    assertEquals("active", activateEntry.getNewValue());
    assertNull(activateEntry.getOrgValue());
  }

  /**
   * CAM-1930: add assertions for additional op log entries here
   */
  @Deployment(resources = {ONE_TASK_PROCESS})
  public void testQueryProcessDefinitionOperationsById() {
    // given
    process = runtimeService.startProcessInstanceByKey("oneTaskProcess");

    // when
    repositoryService.suspendProcessDefinitionById(process.getProcessDefinitionId(), true, null);
    repositoryService.activateProcessDefinitionById(process.getProcessDefinitionId(), true, null);

    // then
    assertEquals(2, query().entityType(PROCESS_DEFINITION).count());

    // Process Definition Suspension
    UserOperationLogEntry suspendDefinitionEntry = query()
      .entityType(PROCESS_DEFINITION)
      .processDefinitionId(process.getProcessDefinitionId())
      .operationType(OPERATION_TYPE_SUSPEND_PROCESS_DEFINITION)
      .singleResult();

    assertNotNull(suspendDefinitionEntry);
    assertEquals(process.getProcessDefinitionId(), suspendDefinitionEntry.getProcessDefinitionId());
    assertEquals("oneTaskProcess", suspendDefinitionEntry.getProcessDefinitionKey());
    assertEquals(deploymentId, suspendDefinitionEntry.getDeploymentId());

    assertEquals("suspensionState", suspendDefinitionEntry.getProperty());
    assertEquals("suspended", suspendDefinitionEntry.getNewValue());
    assertNull(suspendDefinitionEntry.getOrgValue());

    UserOperationLogEntry activateDefinitionEntry = query()
      .entityType(PROCESS_DEFINITION)
      .processDefinitionId(process.getProcessDefinitionId())
      .operationType(OPERATION_TYPE_ACTIVATE_PROCESS_DEFINITION)
      .singleResult();

    assertNotNull(activateDefinitionEntry);
    assertEquals(process.getProcessDefinitionId(), activateDefinitionEntry.getProcessDefinitionId());
    assertEquals("oneTaskProcess", activateDefinitionEntry.getProcessDefinitionKey());
    assertEquals(deploymentId, activateDefinitionEntry.getDeploymentId());

    assertEquals("suspensionState", activateDefinitionEntry.getProperty());
    assertEquals("active", activateDefinitionEntry.getNewValue());
    assertNull(activateDefinitionEntry.getOrgValue());

  }

  /**
   * CAM-1930: add assertions for additional op log entries here
   */
  @Deployment(resources = {ONE_TASK_PROCESS})
  public void testQueryProcessDefinitionOperationsByKey() {
    // given
    process = runtimeService.startProcessInstanceByKey("oneTaskProcess");

    // when
    repositoryService.suspendProcessDefinitionByKey("oneTaskProcess", true, null);
    repositoryService.activateProcessDefinitionByKey("oneTaskProcess", true, null);

    // then
    assertEquals(2, query().entityType(PROCESS_DEFINITION).count());

    UserOperationLogEntry suspendDefinitionEntry = query()
      .entityType(PROCESS_DEFINITION)
      .processDefinitionKey("oneTaskProcess")
      .operationType(OPERATION_TYPE_SUSPEND_PROCESS_DEFINITION)
      .singleResult();

    assertNotNull(suspendDefinitionEntry);
    assertNull(suspendDefinitionEntry.getProcessDefinitionId());
    assertEquals("oneTaskProcess", suspendDefinitionEntry.getProcessDefinitionKey());
    assertNull(suspendDefinitionEntry.getDeploymentId());

    assertEquals("suspensionState", suspendDefinitionEntry.getProperty());
    assertEquals("suspended", suspendDefinitionEntry.getNewValue());
    assertNull(suspendDefinitionEntry.getOrgValue());

    UserOperationLogEntry activateDefinitionEntry = query()
      .entityType(PROCESS_DEFINITION)
      .processDefinitionKey("oneTaskProcess")
      .operationType(OPERATION_TYPE_ACTIVATE_PROCESS_DEFINITION)
      .singleResult();

    assertNotNull(activateDefinitionEntry);
    assertNull(activateDefinitionEntry.getProcessDefinitionId());
    assertEquals("oneTaskProcess", activateDefinitionEntry.getProcessDefinitionKey());
    assertNull(activateDefinitionEntry.getDeploymentId());

    assertEquals("suspensionState", activateDefinitionEntry.getProperty());
    assertEquals("active", activateDefinitionEntry.getNewValue());
    assertNull(activateDefinitionEntry.getOrgValue());
  }

  @Deployment(resources = {"org/camunda/bpm/engine/test/history/HistoricJobLogTest.testAsyncContinuation.bpmn20.xml"})
  public void testQueryJobOperations() {
    // given
    process = runtimeService.startProcessInstanceByKey("process");

    // when
    managementService.suspendJobDefinitionByProcessDefinitionId(process.getProcessDefinitionId());
    managementService.activateJobDefinitionByProcessDefinitionId(process.getProcessDefinitionId());
    managementService.suspendJobByProcessInstanceId(process.getId());
    managementService.activateJobByProcessInstanceId(process.getId());

    // then
    assertEquals(2, query().entityType(JOB_DEFINITION).count());
    assertEquals(2, query().entityType(JOB).count());

    // active job definition
    UserOperationLogEntry activeJobDefinitionEntry = query()
      .entityType(JOB_DEFINITION)
      .processDefinitionId(process.getProcessDefinitionId())
      .operationType(OPERATION_TYPE_ACTIVATE_JOB_DEFINITION)
      .singleResult();

    assertNotNull(activeJobDefinitionEntry);
    assertEquals(process.getProcessDefinitionId(), activeJobDefinitionEntry.getProcessDefinitionId());
    assertEquals(deploymentId, activeJobDefinitionEntry.getDeploymentId());

    assertEquals("suspensionState", activeJobDefinitionEntry.getProperty());
    assertEquals("active", activeJobDefinitionEntry.getNewValue());
    assertNull(activeJobDefinitionEntry.getOrgValue());

    // active job
    UserOperationLogEntry activateJobIdEntry = query()
      .entityType(JOB)
      .processInstanceId(process.getProcessInstanceId())
      .operationType(OPERATION_TYPE_ACTIVATE_JOB)
      .singleResult();

    assertNotNull(activateJobIdEntry);
    assertEquals(process.getProcessInstanceId(), activateJobIdEntry.getProcessInstanceId());
    assertEquals(deploymentId, activateJobIdEntry.getDeploymentId());

    assertEquals("suspensionState", activateJobIdEntry.getProperty());
    assertEquals("active", activateJobIdEntry.getNewValue());
    assertNull(activateJobIdEntry.getOrgValue());

    // suspended job definition
    UserOperationLogEntry suspendJobDefinitionEntry = query()
      .entityType(JOB_DEFINITION)
      .processDefinitionId(process.getProcessDefinitionId())
      .operationType(OPERATION_TYPE_SUSPEND_JOB_DEFINITION)
      .singleResult();

    assertNotNull(suspendJobDefinitionEntry);
    assertEquals(process.getProcessDefinitionId(), suspendJobDefinitionEntry.getProcessDefinitionId());
    assertEquals(deploymentId, suspendJobDefinitionEntry.getDeploymentId());

    assertEquals("suspensionState", suspendJobDefinitionEntry.getProperty());
    assertEquals("suspended", suspendJobDefinitionEntry.getNewValue());
    assertNull(suspendJobDefinitionEntry.getOrgValue());

    // suspended job
    UserOperationLogEntry suspendedJobEntry = query()
      .entityType(JOB)
      .processInstanceId(process.getProcessInstanceId())
      .operationType(OPERATION_TYPE_SUSPEND_JOB)
      .singleResult();

    assertNotNull(suspendedJobEntry);
    assertEquals(process.getProcessInstanceId(), suspendedJobEntry.getProcessInstanceId());
    assertEquals(deploymentId, suspendedJobEntry.getDeploymentId());

    assertEquals("suspensionState", suspendedJobEntry.getProperty());
    assertEquals("suspended", suspendedJobEntry.getNewValue());
    assertNull(suspendedJobEntry.getOrgValue());
  }

  @Deployment(resources = { "org/camunda/bpm/engine/test/bpmn/async/FoxJobRetryCmdTest.testFailedServiceTask.bpmn20.xml" })
  public void testQueryJobRetryOperationsById() {
    // given
    process = runtimeService.startProcessInstanceByKey("failedServiceTask");
    Job job = managementService.createJobQuery().processInstanceId(process.getProcessInstanceId()).singleResult();

    managementService.setJobRetries(job.getId(), 10);

    // then
    assertEquals(1, query().entityType(JOB).operationType(OPERATION_TYPE_SET_JOB_RETRIES).count());

    UserOperationLogEntry jobRetryEntry = query()
      .entityType(JOB)
      .jobId(job.getId())
      .operationType(OPERATION_TYPE_SET_JOB_RETRIES)
      .singleResult();

    assertNotNull(jobRetryEntry);
    assertEquals(job.getId(), jobRetryEntry.getJobId());

    assertEquals("3", jobRetryEntry.getOrgValue());
    assertEquals("10", jobRetryEntry.getNewValue());
    assertEquals("retries", jobRetryEntry.getProperty());
    assertEquals(job.getJobDefinitionId(), jobRetryEntry.getJobDefinitionId());
    assertEquals(job.getProcessInstanceId(), jobRetryEntry.getProcessInstanceId());
    assertEquals(job.getProcessDefinitionKey(), jobRetryEntry.getProcessDefinitionKey());
    assertEquals(job.getProcessDefinitionId(), jobRetryEntry.getProcessDefinitionId());
    assertEquals(deploymentId, jobRetryEntry.getDeploymentId());
  }

  @Deployment(resources = {ONE_TASK_PROCESS})
  public void testQueryJobDefinitionOperationWithDelayedJobDefinition() {
    // given
    // a running process instance
    ProcessInstance process = runtimeService.startProcessInstanceByKey("oneTaskProcess");

    // with a process definition id
    String processDefinitionId = process.getProcessDefinitionId();

    // ...which will be suspended with the corresponding jobs
    managementService.suspendJobDefinitionByProcessDefinitionId(processDefinitionId, true);

    // one week from now
    ClockUtil.setCurrentTime(today);
    long oneWeekFromStartTime = today.getTime() + (7 * 24 * 60 * 60 * 1000);

    // when
    // activate the job definition
    managementService.activateJobDefinitionByProcessDefinitionId(processDefinitionId, false, new Date(oneWeekFromStartTime));

    // then
    // there is a user log entry for the activation
    Long jobDefinitionEntryCount = query()
      .entityType(JOB_DEFINITION)
      .operationType(UserOperationLogEntry.OPERATION_TYPE_ACTIVATE_JOB_DEFINITION)
      .processDefinitionId(processDefinitionId)
      .count();

    assertEquals(1, jobDefinitionEntryCount.longValue());

    // there exists a job for the delayed activation execution
    JobQuery jobQuery = managementService.createJobQuery();

    Job delayedActivationJob = jobQuery.timers().active().singleResult();
    assertNotNull(delayedActivationJob);

    // execute job
    managementService.executeJob(delayedActivationJob.getId());

    jobDefinitionEntryCount = query()
      .entityType(JOB_DEFINITION)
      .operationType(UserOperationLogEntry.OPERATION_TYPE_ACTIVATE_JOB_DEFINITION)
      .processDefinitionId(processDefinitionId)
      .count();

    assertEquals(1, jobDefinitionEntryCount.longValue());

    // Clean up db
    CommandExecutor commandExecutor = processEngineConfiguration.getCommandExecutorTxRequired();
    commandExecutor.execute(new Command<Object>() {
      public Object execute(CommandContext commandContext) {
        commandContext.getHistoricJobLogManager().deleteHistoricJobLogsByHandlerType(TimerActivateJobDefinitionHandler.TYPE);
        return null;
      }
    });
  }

  @Deployment(resources = {"org/camunda/bpm/engine/test/api/repository/ProcessDefinitionSuspensionTest.testWithOneAsyncServiceTask.bpmn"})
  public void testQueryProcessDefinitionOperationWithDelayedProcessDefinition() {
    // given
    ClockUtil.setCurrentTime(today);
    final long hourInMs = 60 * 60 * 1000;

    String key = "oneFailingServiceTaskProcess";

    // a running process instance with a failed service task
    Map<String, Object> params = new HashMap<String, Object>();
    params.put("fail", Boolean.TRUE);
    runtimeService.startProcessInstanceByKey(key, params);

    // when
    // the process definition will be suspended
    repositoryService.suspendProcessDefinitionByKey(key, false, new Date(today.getTime() + (2 * hourInMs)));

    // then
    // there exists a timer job to suspend the process definition delayed
    Job timerToSuspendProcessDefinition = managementService.createJobQuery().timers().singleResult();
    assertNotNull(timerToSuspendProcessDefinition);

    // there is a user log entry for the activation
    Long processDefinitionEntryCount = query()
      .entityType(PROCESS_DEFINITION)
      .operationType(UserOperationLogEntry.OPERATION_TYPE_SUSPEND_PROCESS_DEFINITION)
      .processDefinitionKey(key)
      .count();

    assertEquals(1, processDefinitionEntryCount.longValue());

    // when
    // execute job
    managementService.executeJob(timerToSuspendProcessDefinition.getId());

    // then
    // there is a user log entry for the activation
    processDefinitionEntryCount = query()
      .entityType(PROCESS_DEFINITION)
      .operationType(UserOperationLogEntry.OPERATION_TYPE_SUSPEND_PROCESS_DEFINITION)
      .processDefinitionKey(key)
      .count();

    assertEquals(1, processDefinitionEntryCount.longValue());

    // clean up op log
    CommandExecutor commandExecutor = processEngineConfiguration.getCommandExecutorTxRequired();
    commandExecutor.execute(new Command<Object>() {
      public Object execute(CommandContext commandContext) {
        commandContext.getHistoricJobLogManager().deleteHistoricJobLogsByHandlerType(TimerSuspendProcessDefinitionHandler.TYPE);
        return null;
      }
    });
  }

  // ----- PROCESS INSTANCE MODIFICATION -----

  @Deployment(resources = {ONE_TASK_PROCESS})
  public void testQueryProcessInstanceModificationOperation() {
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("oneTaskProcess");
    String processInstanceId = processInstance.getId();

    ProcessDefinition definition = repositoryService.createProcessDefinitionQuery().singleResult();

    runtimeService
      .createProcessInstanceModification(processInstance.getId())
      .startBeforeActivity("theTask")
      .execute();

    UserOperationLogQuery logQuery = query()
      .entityType(EntityTypes.PROCESS_INSTANCE)
      .operationType(UserOperationLogEntry.OPERATION_TYPE_MODIFY_PROCESS_INSTANCE);

    assertEquals(1, logQuery.count());
    UserOperationLogEntry logEntry = logQuery.singleResult();

    assertEquals(processInstanceId, logEntry.getProcessInstanceId());
    assertEquals(processInstance.getProcessDefinitionId(), logEntry.getProcessDefinitionId());
    assertEquals(definition.getKey(), logEntry.getProcessDefinitionKey());
    assertEquals(deploymentId, logEntry.getDeploymentId());
    assertEquals(UserOperationLogEntry.OPERATION_TYPE_MODIFY_PROCESS_INSTANCE, logEntry.getOperationType());
    assertEquals(EntityTypes.PROCESS_INSTANCE, logEntry.getEntityType());
    assertNull(logEntry.getProperty());
    assertNull(logEntry.getOrgValue());
    assertNull(logEntry.getNewValue());
  }

  // ----- ADD VARIABLES -----

  @Deployment(resources = {ONE_TASK_PROCESS})
  public void testQueryAddExecutionVariableOperation() {
    // given
    process = runtimeService.startProcessInstanceByKey("oneTaskProcess");

    // when
    runtimeService.setVariable(process.getId(), "testVariable1", "THIS IS TESTVARIABLE!!!");

    // then
    verifyVariableOperationAsserts(1, UserOperationLogEntry.OPERATION_TYPE_SET_VARIABLE);
  }

  @Deployment(resources = {ONE_TASK_PROCESS})
  public void testQueryAddExecutionVariablesMapOperation() {
    // given
    process = runtimeService.startProcessInstanceByKey("oneTaskProcess");

    // when
    runtimeService.setVariables(process.getId(), createMapForVariableAddition());

    // then
    verifyVariableOperationAsserts(1, UserOperationLogEntry.OPERATION_TYPE_SET_VARIABLE);
  }

  @Deployment(resources = {ONE_TASK_PROCESS})
  public void testQueryAddExecutionVariablesSingleAndMapOperation() {
    // given
    process = runtimeService.startProcessInstanceByKey("oneTaskProcess");

    // when
    runtimeService.setVariable(process.getId(), "testVariable3", "foo");
    runtimeService.setVariables(process.getId(), createMapForVariableAddition());
    runtimeService.setVariable(process.getId(), "testVariable4", "bar");

    // then
    verifyVariableOperationAsserts(3, UserOperationLogEntry.OPERATION_TYPE_SET_VARIABLE);
  }

  @Deployment(resources = {ONE_TASK_PROCESS})
  public void testQueryAddTaskVariableOperation() {
    // given
    process = runtimeService.startProcessInstanceByKey("oneTaskProcess");
    processTaskId = taskService.createTaskQuery().singleResult().getId();

    // when
    taskService.setVariable(processTaskId, "testVariable1", "THIS IS TESTVARIABLE!!!");

    // then
    verifyVariableOperationAsserts(1, UserOperationLogEntry.OPERATION_TYPE_SET_VARIABLE);
  }

  @Deployment(resources = {ONE_TASK_PROCESS})
  public void testQueryAddTaskVariablesMapOperation() {
    // given
    process = runtimeService.startProcessInstanceByKey("oneTaskProcess");
    processTaskId = taskService.createTaskQuery().singleResult().getId();

    // when
    taskService.setVariables(processTaskId, createMapForVariableAddition());

    // then
    verifyVariableOperationAsserts(1, UserOperationLogEntry.OPERATION_TYPE_SET_VARIABLE);
  }

  @Deployment(resources = {ONE_TASK_PROCESS})
  public void testQueryAddTaskVariablesSingleAndMapOperation() {
    // given
    process = runtimeService.startProcessInstanceByKey("oneTaskProcess");
    processTaskId = taskService.createTaskQuery().singleResult().getId();

    // when
    taskService.setVariable(processTaskId, "testVariable3", "foo");
    taskService.setVariables(processTaskId, createMapForVariableAddition());
    taskService.setVariable(processTaskId, "testVariable4", "bar");

    // then
    verifyVariableOperationAsserts(3, UserOperationLogEntry.OPERATION_TYPE_SET_VARIABLE);
  }

  // ----- PATCH VARIABLES -----

  @Deployment(resources = {ONE_TASK_PROCESS})
  public void testQueryPatchExecutionVariablesOperation() {
    // given
    process = runtimeService.startProcessInstanceByKey("oneTaskProcess");

    // when
    ((RuntimeServiceImpl) runtimeService)
      .updateVariables(process.getId(), createMapForVariableAddition(), createCollectionForVariableDeletion());

    // then
   verifyVariableOperationAsserts(1, UserOperationLogEntry.OPERATION_TYPE_MODIFY_VARIABLE);
  }

  @Deployment(resources = {ONE_TASK_PROCESS})
  public void testQueryPatchTaskVariablesOperation() {
    // given
    process = runtimeService.startProcessInstanceByKey("oneTaskProcess");
    processTaskId = taskService.createTaskQuery().singleResult().getId();

    // when
    ((TaskServiceImpl) taskService)
      .updateVariablesLocal(processTaskId, createMapForVariableAddition(), createCollectionForVariableDeletion());

    // then
    verifyVariableOperationAsserts(1, UserOperationLogEntry.OPERATION_TYPE_MODIFY_VARIABLE);
  }

  // ----- REMOVE VARIABLES -----

  @Deployment(resources = {ONE_TASK_PROCESS})
  public void testQueryRemoveExecutionVariableOperation() {
    // given
    process = runtimeService.startProcessInstanceByKey("oneTaskProcess");

    // when
    runtimeService.removeVariable(process.getId(), "testVariable1");

    // then
    verifyVariableOperationAsserts(1, UserOperationLogEntry.OPERATION_TYPE_REMOVE_VARIABLE);
  }

  @Deployment(resources = {ONE_TASK_PROCESS})
  public void testQueryRemoveExecutionVariablesMapOperation() {
    // given
    process = runtimeService.startProcessInstanceByKey("oneTaskProcess");

    // when
    runtimeService.removeVariables(process.getId(), createCollectionForVariableDeletion());

    // then
    verifyVariableOperationAsserts(1, UserOperationLogEntry.OPERATION_TYPE_REMOVE_VARIABLE);
  }

  @Deployment(resources = {ONE_TASK_PROCESS})
  public void testQueryRemoveExecutionVariablesSingleAndMapOperation() {
    // given
    process = runtimeService.startProcessInstanceByKey("oneTaskProcess");

    // when
    runtimeService.removeVariable(process.getId(), "testVariable1");
    runtimeService.removeVariables(process.getId(), createCollectionForVariableDeletion());
    runtimeService.removeVariable(process.getId(), "testVariable2");

    // then
    verifyVariableOperationAsserts(3, UserOperationLogEntry.OPERATION_TYPE_REMOVE_VARIABLE);
  }

  @Deployment(resources = {ONE_TASK_PROCESS})
  public void testQueryRemoveTaskVariableOperation() {
    // given
    process = runtimeService.startProcessInstanceByKey("oneTaskProcess");
    processTaskId = taskService.createTaskQuery().singleResult().getId();

    // when
    taskService.removeVariable(processTaskId, "testVariable1");

    // then
    verifyVariableOperationAsserts(1, UserOperationLogEntry.OPERATION_TYPE_REMOVE_VARIABLE);
  }

  @Deployment(resources = {ONE_TASK_PROCESS})
  public void testQueryRemoveTaskVariablesMapOperation() {
    // given
    process = runtimeService.startProcessInstanceByKey("oneTaskProcess");
    processTaskId = taskService.createTaskQuery().singleResult().getId();

    // when
    taskService.removeVariables(processTaskId, createCollectionForVariableDeletion());

    // then
    verifyVariableOperationAsserts(1, UserOperationLogEntry.OPERATION_TYPE_REMOVE_VARIABLE);
  }

  @Deployment(resources = {ONE_TASK_PROCESS})
  public void testQueryRemoveTaskVariablesSingleAndMapOperation() {
    // given
    process = runtimeService.startProcessInstanceByKey("oneTaskProcess");
    processTaskId = taskService.createTaskQuery().singleResult().getId();

    // when
    taskService.removeVariable(processTaskId, "testVariable3");
    taskService.removeVariables(processTaskId, createCollectionForVariableDeletion());
    taskService.removeVariable(processTaskId, "testVariable4");

    // then
    verifyVariableOperationAsserts(3, UserOperationLogEntry.OPERATION_TYPE_REMOVE_VARIABLE);
  }

  @Deployment(resources = {ONE_TASK_PROCESS})
  public void testQueryByEntityTypes() {
    // given
    process = runtimeService.startProcessInstanceByKey("oneTaskProcess");
    processTaskId = taskService.createTaskQuery().singleResult().getId();

    // when
    taskService.setAssignee(processTaskId, "foo");
    taskService.setVariable(processTaskId, "foo", "bar");

    // then
    UserOperationLogQuery query = historyService
        .createUserOperationLogQuery()
        .entityTypeIn(EntityTypes.TASK, EntityTypes.VARIABLE);

    verifyQueryResults(query, 2);
  }

  public void testQueryByInvalidEntityTypes() {
    UserOperationLogQuery query = historyService
        .createUserOperationLogQuery()
        .entityTypeIn("foo");

    verifyQueryResults(query, 0);

    try {
      query.entityTypeIn(null);
      fail();
    } catch (ProcessEngineException e) {
      // expected
    }

    try {
      query.entityTypeIn(EntityTypes.TASK, null, EntityTypes.VARIABLE);
      fail();
    } catch (ProcessEngineException e) {
      // expected
    }
  }

  // --------------- CMMN --------------------

  @Deployment(resources={ONE_TASK_CASE})
  public void testQueryByCaseDefinitionId() {
    // given:
    // a deployed case definition
    String caseDefinitionId = repositoryService
        .createCaseDefinitionQuery()
        .singleResult()
        .getId();

    // an active case instance
    caseService
       .withCaseDefinition(caseDefinitionId)
       .create();

    Task task = taskService.createTaskQuery().singleResult();

    assertNotNull(task);

    // when
    taskService.setAssignee(task.getId(), "demo");

    // then

    UserOperationLogQuery query = historyService
      .createUserOperationLogQuery()
      .caseDefinitionId(caseDefinitionId);

    verifyQueryResults(query, 1);
  }

  @Deployment(resources={ONE_TASK_CASE})
  public void testQueryByCaseInstanceId() {
    // given:
    // a deployed case definition
    String caseDefinitionId = repositoryService
        .createCaseDefinitionQuery()
        .singleResult()
        .getId();

    // an active case instance
    String caseInstanceId = caseService
       .withCaseDefinition(caseDefinitionId)
       .create()
       .getId();

    Task task = taskService.createTaskQuery().singleResult();

    assertNotNull(task);

    // when
    taskService.setAssignee(task.getId(), "demo");

    // then

    UserOperationLogQuery query = historyService
      .createUserOperationLogQuery()
      .caseInstanceId(caseInstanceId);

    verifyQueryResults(query, 1);
  }

  @Deployment(resources={ONE_TASK_CASE})
  public void testQueryByCaseExecutionId() {
    // given:
    // a deployed case definition
    String caseDefinitionId = repositoryService
        .createCaseDefinitionQuery()
        .singleResult()
        .getId();

    // an active case instance
    caseService
       .withCaseDefinition(caseDefinitionId)
       .create();

    String caseExecutionId = caseService
        .createCaseExecutionQuery()
        .activityId("PI_HumanTask_1")
        .singleResult()
        .getId();

    Task task = taskService.createTaskQuery().singleResult();

    assertNotNull(task);

    // when
    taskService.setAssignee(task.getId(), "demo");

    // then

    UserOperationLogQuery query = historyService
      .createUserOperationLogQuery()
      .caseExecutionId(caseExecutionId);

    verifyQueryResults(query, 1);
  }

  public void testQueryByDeploymentId() {
    // given
    String deploymentId = repositoryService
        .createDeployment()
        .addClasspathResource(ONE_TASK_PROCESS)
        .deploy()
        .getId();

    // when
    UserOperationLogQuery query = historyService
        .createUserOperationLogQuery()
        .deploymentId(deploymentId);

    // then
    verifyQueryResults(query, 1);

    repositoryService.deleteDeployment(deploymentId, true);
  }

  public void testQueryByInvalidDeploymentId() {
    UserOperationLogQuery query = historyService
        .createUserOperationLogQuery()
        .deploymentId("invalid");

    verifyQueryResults(query, 0);

    try {
      query.deploymentId(null);
      fail();
    } catch (ProcessEngineException e) {
      // expected
    }
  }

  private void verifyQueryResults(UserOperationLogQuery query, int countExpected) {
    assertEquals(countExpected, query.list().size());
    assertEquals(countExpected, query.count());

    if (countExpected == 1) {
      assertNotNull(query.singleResult());
    } else if (countExpected > 1){
      verifySingleResultFails(query);
    } else if (countExpected == 0) {
      assertNull(query.singleResult());
    }
  }

  private void verifySingleResultFails(UserOperationLogQuery query) {
    try {
      query.singleResult();
      fail();
    } catch (ProcessEngineException e) {}
  }

  private Map<String, Object> createMapForVariableAddition() {
    Map<String, Object> variables =  new HashMap<String, Object>();
    variables.put("testVariable1", "THIS IS TESTVARIABLE!!!");
    variables.put("testVariable2", "OVER 9000!");

    return variables;
  }

  private Collection<String> createCollectionForVariableDeletion() {
    Collection<String> variables = new ArrayList<String>();
    variables.add("testVariable3");
    variables.add("testVariable4");

    return variables;
  }

  private void verifyVariableOperationAsserts(int countAssertValue, String operationType) {
    UserOperationLogQuery logQuery = query().entityType(EntityTypes.VARIABLE).operationType(operationType);
    assertEquals(countAssertValue, logQuery.count());

    if(countAssertValue > 1) {
      List<UserOperationLogEntry> logEntryList = logQuery.list();

      for (UserOperationLogEntry logEntry : logEntryList) {
        assertEquals(process.getProcessDefinitionId(), logEntry.getProcessDefinitionId());
        assertEquals(process.getProcessInstanceId(), logEntry.getProcessInstanceId());
        assertEquals(deploymentId, logEntry.getDeploymentId());
      }
    } else {
      UserOperationLogEntry logEntry = logQuery.singleResult();
      assertEquals(process.getProcessDefinitionId(), logEntry.getProcessDefinitionId());
      assertEquals(process.getProcessInstanceId(), logEntry.getProcessInstanceId());
      assertEquals(deploymentId, logEntry.getDeploymentId());
    }
  }

  private UserOperationLogQuery query() {
    return historyService.createUserOperationLogQuery();
  }

  /**
   * start process and operate on userTask to create some log entries for the query tests
   */
  private void createLogEntries() {
    ClockUtil.setCurrentTime(yesterday);

    // create a process with a userTask and work with it
    process = runtimeService.startProcessInstanceByKey("oneTaskProcess");
    execution = processEngine.getRuntimeService().createExecutionQuery().processInstanceId(process.getId()).singleResult();
    processTaskId = taskService.createTaskQuery().singleResult().getId();

    // user "icke" works on the process userTask
    identityService.setAuthenticatedUserId("icke");

    // create and remove some links
    taskService.addCandidateUser(processTaskId, "er");
    taskService.deleteCandidateUser(processTaskId, "er");
    taskService.addCandidateGroup(processTaskId, "wir");
    taskService.deleteCandidateGroup(processTaskId, "wir");

    // assign and reassign the userTask
    ClockUtil.setCurrentTime(today);
    taskService.setOwner(processTaskId, "icke");
    taskService.claim(processTaskId, "icke");
    taskService.setAssignee(processTaskId, "er");

    // change priority of task
    taskService.setPriority(processTaskId, 10);

    // add and delete an attachment
    Attachment attachment = taskService.createAttachment("image/ico", processTaskId, process.getId(), "favicon.ico", "favicon", "http://camunda.com/favicon.ico");
    taskService.deleteAttachment(attachment.getId());

    // complete the userTask to finish the process
    taskService.complete(processTaskId);
    assertProcessEnded(process.getId());

    // user "er" works on the process userTask
    identityService.setAuthenticatedUserId("er");

    // create a standalone userTask
    userTask = taskService.newTask();
    userTask.setName("to do");
    taskService.saveTask(userTask);

    // change some properties manually to create an update event
    ClockUtil.setCurrentTime(tomorrow);
    userTask.setDescription("desc");
    userTask.setOwner("icke");
    userTask.setAssignee("er");
    userTask.setDueDate(new Date());
    taskService.saveTask(userTask);

    // complete the userTask
    taskService.complete(userTask.getId());
  }

}
