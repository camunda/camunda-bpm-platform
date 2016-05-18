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
package org.camunda.bpm.engine.test.api.authorization;

import static org.camunda.bpm.engine.authorization.Authorization.ANY;
import static org.camunda.bpm.engine.authorization.Permissions.READ;
import static org.camunda.bpm.engine.authorization.Permissions.READ_INSTANCE;
import static org.camunda.bpm.engine.authorization.Permissions.UPDATE;
import static org.camunda.bpm.engine.authorization.Permissions.UPDATE_INSTANCE;
import static org.camunda.bpm.engine.authorization.Resources.PROCESS_DEFINITION;
import static org.camunda.bpm.engine.authorization.Resources.PROCESS_INSTANCE;

import java.util.Date;

import org.camunda.bpm.engine.AuthorizationException;
import org.camunda.bpm.engine.impl.AbstractQuery;
import org.camunda.bpm.engine.impl.interceptor.Command;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.interceptor.CommandExecutor;
import org.camunda.bpm.engine.impl.jobexecutor.TimerSuspendJobDefinitionHandler;
import org.camunda.bpm.engine.impl.util.ClockUtil;
import org.camunda.bpm.engine.management.JobDefinition;
import org.camunda.bpm.engine.runtime.Job;
import org.camunda.bpm.engine.runtime.JobQuery;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.task.Task;

/**
 * @author Roman Smirnov
 *
 */
public class JobAuthorizationTest extends AuthorizationTest {

  protected static final String TIMER_START_PROCESS_KEY = "timerStartProcess";
  protected static final String TIMER_BOUNDARY_PROCESS_KEY = "timerBoundaryProcess";
  protected static final String ONE_INCIDENT_PROCESS_KEY = "process";

  protected String deploymentId;

  @Override
  public void setUp() throws Exception {
    deploymentId = createDeployment(null,
        "org/camunda/bpm/engine/test/api/authorization/timerStartEventProcess.bpmn20.xml",
        "org/camunda/bpm/engine/test/api/authorization/timerBoundaryEventProcess.bpmn20.xml",
        "org/camunda/bpm/engine/test/api/authorization/oneIncidentProcess.bpmn20.xml").getId();
    super.setUp();
  }

  @Override
  public void tearDown() {
    super.tearDown();
    CommandExecutor commandExecutor = processEngineConfiguration.getCommandExecutorTxRequired();
    commandExecutor.execute(new Command<Object>() {
      public Object execute(CommandContext commandContext) {
        commandContext.getHistoricJobLogManager().deleteHistoricJobLogsByHandlerType(TimerSuspendJobDefinitionHandler.TYPE);
        return null;
      }
    });
    deleteDeployment(deploymentId);
  }

  // job query (jobs associated to a process) //////////////////////////////////////////////////

  public void testQueryWithoutAuthorization() {
    // given
    startProcessInstanceByKey(TIMER_BOUNDARY_PROCESS_KEY);

    // when
    JobQuery query = managementService.createJobQuery();

    // then
    verifyQueryResults(query, 0);
  }

  public void testQueryWithReadPermissionOnProcessInstance() {
    // given
    String processInstanceId = startProcessInstanceByKey(TIMER_BOUNDARY_PROCESS_KEY).getId();
    createGrantAuthorization(PROCESS_INSTANCE, processInstanceId, userId, READ);

    // when
    JobQuery query = managementService.createJobQuery();

    // then
    verifyQueryResults(query, 1);
  }

  public void testQueryWithReadPermissionOnAnyProcessInstance() {
    // given
    startProcessInstanceByKey(TIMER_BOUNDARY_PROCESS_KEY);
    createGrantAuthorization(PROCESS_INSTANCE, ANY, userId, READ);

    // when
    JobQuery query = managementService.createJobQuery();

    // then
    verifyQueryResults(query, 2);
  }

  public void testQueryWithMultiple() {
    // given
    String processInstanceId = startProcessInstanceByKey(TIMER_BOUNDARY_PROCESS_KEY).getId();
    createGrantAuthorization(PROCESS_INSTANCE, ANY, userId, READ);
    createGrantAuthorization(PROCESS_INSTANCE, processInstanceId, userId, READ);

    // when
    JobQuery query = managementService.createJobQuery();

    // then
    verifyQueryResults(query, 2);
  }

  public void testQueryWithReadInstancePermissionOnTimerStartProcessDefinition() {
    // given
    startProcessInstanceByKey(TIMER_BOUNDARY_PROCESS_KEY);

    createGrantAuthorization(PROCESS_DEFINITION, TIMER_START_PROCESS_KEY, userId, READ_INSTANCE);

    // when
    JobQuery query = managementService.createJobQuery();

    // then
    verifyQueryResults(query, 1);

    Job job = query.singleResult();
    assertNull(job.getProcessInstanceId());
    assertEquals(TIMER_START_PROCESS_KEY, job.getProcessDefinitionKey());
  }

  public void testQueryWithReadInstancePermissionOnTimerBoundaryProcessDefinition() {
    // given
    String processInstanceId = startProcessInstanceByKey(TIMER_BOUNDARY_PROCESS_KEY).getId();
    createGrantAuthorization(PROCESS_DEFINITION, TIMER_BOUNDARY_PROCESS_KEY, userId, READ_INSTANCE);

    // when
    JobQuery query = managementService.createJobQuery();

    // then
    verifyQueryResults(query, 1);

    Job job = query.singleResult();
    assertEquals(processInstanceId, job.getProcessInstanceId());
    assertEquals(TIMER_BOUNDARY_PROCESS_KEY, job.getProcessDefinitionKey());
  }

  public void testQueryWithReadInstancePermissionOnAnyProcessDefinition() {
    // given
    startProcessInstanceByKey(TIMER_BOUNDARY_PROCESS_KEY);

    createGrantAuthorization(PROCESS_DEFINITION, ANY, userId, READ_INSTANCE);

    // when
    JobQuery query = managementService.createJobQuery();

    // then
    verifyQueryResults(query, 2);
  }

  // job query (standalone job) /////////////////////////////////

  public void testStandaloneJobQueryWithoutAuthorization() {
    // given
    Date startTime = new Date();
    ClockUtil.setCurrentTime(startTime);
    long oneWeekFromStartTime = startTime.getTime() + (7 * 24 * 60 * 60 * 1000);

    disableAuthorization();
    // creates a new "standalone" job
    managementService.suspendJobDefinitionByProcessDefinitionKey(TIMER_START_PROCESS_KEY, true, new Date(oneWeekFromStartTime));
    enableAuthorization();

    // when
    JobQuery query = managementService.createJobQuery();

    // then
    verifyQueryResults(query, 1);

    Job job = query.singleResult();
    assertNotNull(job);
    assertNull(job.getProcessInstanceId());
    assertNull(job.getProcessDefinitionKey());

    deleteJob(job.getId());
  }

  // execute job ////////////////////////////////////////////////

  public void testExecuteJobWithoutAuthorization() {
    // given
    Job job = selectAnyJob();
    String jobId = job.getId();

    try {
      // when
      managementService.executeJob(jobId);
      fail("Exception expected: It should not be possible to execute the job");
    } catch (AuthorizationException e) {
      // then
      String message = e.getMessage();
      assertTextPresent(userId, message);
      assertTextPresent(UPDATE.getName(), message);
      assertTextPresent(PROCESS_INSTANCE.resourceName(), message);
      assertTextPresent(UPDATE_INSTANCE.getName(), message);
      assertTextPresent(job.getProcessDefinitionKey(), message);
      assertTextPresent(PROCESS_DEFINITION.resourceName(), message);
    }
  }

  public void testExecuteJobWithUpdatePermissionOnProcessInstance() {
    // given
    String processInstanceId = startProcessInstanceByKey(TIMER_BOUNDARY_PROCESS_KEY).getId();
    createGrantAuthorization(PROCESS_INSTANCE, processInstanceId, userId, UPDATE);
    String jobId = selectJobByProcessInstanceId(processInstanceId).getId();

    // when
    managementService.executeJob(jobId);

    // then
    Task task = selectSingleTask();
    assertNotNull(task);
    assertEquals("taskAfterBoundaryEvent", task.getTaskDefinitionKey());
  }

  public void testExecuteJobWithUpdatePermissionOnAnyProcessInstance() {
    // given
    String processInstanceId = startProcessInstanceByKey(TIMER_BOUNDARY_PROCESS_KEY).getId();
    createGrantAuthorization(PROCESS_INSTANCE, ANY, userId, UPDATE);
    String jobId = selectJobByProcessInstanceId(processInstanceId).getId();

    // when
    managementService.executeJob(jobId);

    // then
    Task task = selectSingleTask();
    assertNotNull(task);
    assertEquals("taskAfterBoundaryEvent", task.getTaskDefinitionKey());
  }

  public void testExecuteJobWithUpdateInstancePermissionOnProcessDefinition() {
    // given
    String processInstanceId = startProcessInstanceByKey(TIMER_BOUNDARY_PROCESS_KEY).getId();
    createGrantAuthorization(PROCESS_DEFINITION, TIMER_BOUNDARY_PROCESS_KEY, userId, UPDATE_INSTANCE);
    String jobId = selectJobByProcessInstanceId(processInstanceId).getId();

    // when
    managementService.executeJob(jobId);

    // then
    Task task = selectSingleTask();
    assertNotNull(task);
    assertEquals("taskAfterBoundaryEvent", task.getTaskDefinitionKey());
  }

  public void testExecuteJobWithUpdateInstancePermissionOnAnyProcessDefinition() {
    // given
    String processInstanceId = startProcessInstanceByKey(TIMER_BOUNDARY_PROCESS_KEY).getId();
    createGrantAuthorization(PROCESS_DEFINITION, ANY, userId, UPDATE_INSTANCE);
    String jobId = selectJobByProcessInstanceId(processInstanceId).getId();

    // when
    managementService.executeJob(jobId);

    // then
    Task task = selectSingleTask();
    assertNotNull(task);
    assertEquals("taskAfterBoundaryEvent", task.getTaskDefinitionKey());
  }

  // execute job (standalone job) ////////////////////////////////

  public void testExecuteStandaloneJob() {
    // given
    createGrantAuthorization(PROCESS_DEFINITION, TIMER_START_PROCESS_KEY, userId, UPDATE);

    Date startTime = new Date();
    ClockUtil.setCurrentTime(startTime);
    long oneWeekFromStartTime = startTime.getTime() + (7 * 24 * 60 * 60 * 1000);

    disableAuthorization();
    // creates a new "standalone" job
    managementService.suspendJobDefinitionByProcessDefinitionKey(TIMER_START_PROCESS_KEY, false, new Date(oneWeekFromStartTime));
    enableAuthorization();

    String jobId = managementService.createJobQuery().singleResult().getId();

    // when
    managementService.executeJob(jobId);

    // then
    JobDefinition jobDefinition = selectJobDefinitionByProcessDefinitionKey(TIMER_START_PROCESS_KEY);
    assertTrue(jobDefinition.isSuspended());
  }

  // delete job ////////////////////////////////////////////////

  public void testDeleteJobWithoutAuthorization() {
    // given
    Job job = selectAnyJob();
    String jobId = job.getId();

    try {
      // when
      managementService.deleteJob(jobId);
      fail("Exception expected: It should not be possible to delete the job");
    } catch (AuthorizationException e) {
      // then
      String message = e.getMessage();
      assertTextPresent(userId, message);
      assertTextPresent(UPDATE.getName(), message);
      assertTextPresent(PROCESS_INSTANCE.resourceName(), message);
      assertTextPresent(UPDATE_INSTANCE.getName(), message);
      assertTextPresent(job.getProcessDefinitionKey(), message);
      assertTextPresent(PROCESS_DEFINITION.resourceName(), message);
    }
  }

  public void testDeleteJobWithUpdatePermissionOnProcessInstance() {
    // given
    String processInstanceId = startProcessInstanceByKey(TIMER_BOUNDARY_PROCESS_KEY).getId();
    createGrantAuthorization(PROCESS_INSTANCE, processInstanceId, userId, UPDATE);
    String jobId = selectJobByProcessInstanceId(processInstanceId).getId();

    // when
    managementService.deleteJob(jobId);

    // then
    Job job = selectJobByProcessInstanceId(processInstanceId);
    assertNull(job);
  }

  public void testDeleteJobWithUpdatePermissionOnAnyProcessInstance() {
    // given
    String processInstanceId = startProcessInstanceByKey(TIMER_BOUNDARY_PROCESS_KEY).getId();
    createGrantAuthorization(PROCESS_INSTANCE, ANY, userId, UPDATE);
    String jobId = selectJobByProcessInstanceId(processInstanceId).getId();

    // when
    managementService.deleteJob(jobId);

    // then
    Job job = selectJobByProcessInstanceId(processInstanceId);
    assertNull(job);
  }

  public void testDeleteJobWithUpdateInstancePermissionOnProcessDefinition() {
    // given
    String processInstanceId = startProcessInstanceByKey(TIMER_BOUNDARY_PROCESS_KEY).getId();
    createGrantAuthorization(PROCESS_DEFINITION, TIMER_BOUNDARY_PROCESS_KEY, userId, UPDATE_INSTANCE);
    String jobId = selectJobByProcessInstanceId(processInstanceId).getId();

    // when
    managementService.deleteJob(jobId);

    // then
    Job job = selectJobByProcessInstanceId(processInstanceId);
    assertNull(job);
  }

  public void testDeleteJobWithUpdateInstancePermissionOnAnyProcessDefinition() {
    // given
    String processInstanceId = startProcessInstanceByKey(TIMER_BOUNDARY_PROCESS_KEY).getId();
    createGrantAuthorization(PROCESS_DEFINITION, ANY, userId, UPDATE_INSTANCE);
    String jobId = selectJobByProcessInstanceId(processInstanceId).getId();
    // when
    managementService.deleteJob(jobId);

    // then
    Job job = selectJobByProcessInstanceId(processInstanceId);
    assertNull(job);
  }

  // delete standalone job ////////////////////////////////

  public void testDeleteStandaloneJob() {
    // given
    createGrantAuthorization(PROCESS_DEFINITION, TIMER_START_PROCESS_KEY, userId, UPDATE);

    Date startTime = new Date();
    ClockUtil.setCurrentTime(startTime);
    long oneWeekFromStartTime = startTime.getTime() + (7 * 24 * 60 * 60 * 1000);

    disableAuthorization();
    // creates a new "standalone" job
    managementService.suspendJobDefinitionByProcessDefinitionKey(TIMER_START_PROCESS_KEY, false, new Date(oneWeekFromStartTime));
    enableAuthorization();

    String jobId = managementService.createJobQuery().singleResult().getId();

    // when
    managementService.deleteJob(jobId);

    // then
    Job job = selectJobById(jobId);
    assertNull(job);
  }

  // set job retries ////////////////////////////////////////////////

  public void testSetJobRetriesWithoutAuthorization() {
    // given
    Job job = selectAnyJob();
    String jobId = job.getId();

    try {
      // when
      managementService.setJobRetries(jobId, 1);
      fail("Exception expected: It should not be possible to set job retries");
    } catch (AuthorizationException e) {
      // then
      String message = e.getMessage();
      assertTextPresent(userId, message);
      assertTextPresent(UPDATE.getName(), message);
      assertTextPresent(PROCESS_INSTANCE.resourceName(), message);
      assertTextPresent(UPDATE_INSTANCE.getName(), message);
      assertTextPresent(job.getProcessDefinitionKey(), message);
      assertTextPresent(PROCESS_DEFINITION.resourceName(), message);
    }
  }

  public void testSetJobRetriesWithUpdatePermissionOnProcessInstance() {
    // given
    String processInstanceId = startProcessInstanceByKey(TIMER_BOUNDARY_PROCESS_KEY).getId();
    createGrantAuthorization(PROCESS_INSTANCE, processInstanceId, userId, UPDATE);
    String jobId = selectJobByProcessInstanceId(processInstanceId).getId();

    // when
    managementService.setJobRetries(jobId, 1);

    // then
    Job job = selectJobById(jobId);
    assertNotNull(job);
    assertEquals(1, job.getRetries());
  }

  public void testSetJobRetriesWithUpdatePermissionOnAnyProcessInstance() {
    // given
    String processInstanceId = startProcessInstanceByKey(TIMER_BOUNDARY_PROCESS_KEY).getId();
    createGrantAuthorization(PROCESS_INSTANCE, ANY, userId, UPDATE);
    String jobId = selectJobByProcessInstanceId(processInstanceId).getId();

    // when
    managementService.setJobRetries(jobId, 1);

    // then
    Job job = selectJobById(jobId);
    assertNotNull(job);
    assertEquals(1, job.getRetries());
  }

  public void testSetJobRetriesWithUpdateInstancePermissionOnProcessDefinition() {
    // given
    String processInstanceId = startProcessInstanceByKey(TIMER_BOUNDARY_PROCESS_KEY).getId();
    createGrantAuthorization(PROCESS_DEFINITION, TIMER_BOUNDARY_PROCESS_KEY, userId, UPDATE_INSTANCE);
    String jobId = selectJobByProcessInstanceId(processInstanceId).getId();

    // when
    managementService.setJobRetries(jobId, 1);

    // then
    Job job = selectJobById(jobId);
    assertNotNull(job);
    assertEquals(1, job.getRetries());
  }

  public void testSetJobRetriesWithUpdateInstancePermissionOnAnyProcessDefinition() {
    // given
    String processInstanceId = startProcessInstanceByKey(TIMER_BOUNDARY_PROCESS_KEY).getId();
    createGrantAuthorization(PROCESS_DEFINITION, ANY, userId, UPDATE_INSTANCE);
    String jobId = selectJobByProcessInstanceId(processInstanceId).getId();

    // when
    managementService.setJobRetries(jobId, 1);

    // then
    Job job = selectJobById(jobId);
    assertNotNull(job);
    assertEquals(1, job.getRetries());
  }

  // set job retries (standalone) ////////////////////////////////

  public void testSetStandaloneJobRetries() {
    // given
    createGrantAuthorization(PROCESS_DEFINITION, TIMER_START_PROCESS_KEY, userId, UPDATE);

    Date startTime = new Date();
    ClockUtil.setCurrentTime(startTime);
    long oneWeekFromStartTime = startTime.getTime() + (7 * 24 * 60 * 60 * 1000);

    disableAuthorization();
    // creates a new "standalone" job
    managementService.suspendJobDefinitionByProcessDefinitionKey(TIMER_START_PROCESS_KEY, false, new Date(oneWeekFromStartTime));
    enableAuthorization();

    String jobId = managementService.createJobQuery().singleResult().getId();

    // when
    managementService.setJobRetries(jobId, 1);

    // then
    Job job = selectJobById(jobId);
    assertEquals(1, job.getRetries());

    deleteJob(jobId);
  }

  // set job retries by job definition id ///////////////////////

  public void testSetJobRetriesByJobDefinitionIdWithoutAuthorization() {
    // given
    disableAuthorization();
    JobDefinition jobDefinition = managementService.createJobDefinitionQuery().listPage(0, 1).get(0);
    enableAuthorization();

    String jobDefinitionId = jobDefinition.getId();

    try {
      // when
      managementService.setJobRetriesByJobDefinitionId(jobDefinitionId, 1);
      fail("Exception expected: It should not be possible to set job retries");
    } catch (AuthorizationException e) {
      // then
      String message = e.getMessage();
      assertTextPresent(userId, message);
      assertTextPresent(UPDATE.getName(), message);
      assertTextPresent(PROCESS_INSTANCE.resourceName(), message);
      assertTextPresent(UPDATE_INSTANCE.getName(), message);
      assertTextPresent(jobDefinition.getProcessDefinitionKey(), message);
      assertTextPresent(PROCESS_DEFINITION.resourceName(), message);
    }
  }

  public void testSetJobRetriesByJobDefinitionIdWithUpdatePermissionOnProcessInstance() {
    // given
    String processInstanceId = startProcessInstanceByKey(TIMER_BOUNDARY_PROCESS_KEY).getId();
    createGrantAuthorization(PROCESS_INSTANCE, processInstanceId, userId, UPDATE);
    String jobDefinitionId = selectJobDefinitionByProcessDefinitionKey(TIMER_BOUNDARY_PROCESS_KEY).getId();

    try {
      // when
      managementService.setJobRetriesByJobDefinitionId(jobDefinitionId, 1);
      fail("Exception expected: It should not be possible to set job retries");
    } catch (AuthorizationException e) {
      // then
      String message = e.getMessage();
      assertTextPresent(userId, message);
      assertTextPresent(UPDATE.getName(), message);
      assertTextPresent(PROCESS_INSTANCE.resourceName(), message);
      assertTextPresent(UPDATE_INSTANCE.getName(), message);
      assertTextPresent(TIMER_BOUNDARY_PROCESS_KEY, message);
      assertTextPresent(PROCESS_DEFINITION.resourceName(), message);
    }
  }

  public void testSetJobRetriesByJobDefinitionIdWithUpdatePermissionOnAnyProcessInstance() {
    // given
    String processInstanceId = startProcessInstanceByKey(TIMER_BOUNDARY_PROCESS_KEY).getId();
    createGrantAuthorization(PROCESS_INSTANCE, ANY, userId, UPDATE);
    String jobId = selectJobByProcessInstanceId(processInstanceId).getId();

    disableAuthorization();
    managementService.setJobRetries(jobId, 0);
    enableAuthorization();

    String jobDefinitionId = selectJobDefinitionByProcessDefinitionKey(TIMER_BOUNDARY_PROCESS_KEY).getId();

    // when
    managementService.setJobRetriesByJobDefinitionId(jobDefinitionId, 1);

    // then
    Job job = selectJobByProcessInstanceId(processInstanceId);
    assertNotNull(job);
    assertEquals(1, job.getRetries());
  }

  public void testSetJobRetriesByJobDefinitionIdWithUpdateInstancePermissionOnProcessDefinition() {
    // given
    String processInstanceId = startProcessInstanceByKey(TIMER_BOUNDARY_PROCESS_KEY).getId();
    createGrantAuthorization(PROCESS_DEFINITION, TIMER_BOUNDARY_PROCESS_KEY, userId, UPDATE_INSTANCE);
    String jobId = selectJobByProcessInstanceId(processInstanceId).getId();

    // when
    managementService.setJobRetries(jobId, 1);

    // then
    Job job = selectJobById(jobId);
    assertNotNull(job);
    assertEquals(1, job.getRetries());
  }

  public void testSetJobRetriesByJobDefinitionIdWithUpdateInstancePermissionOnAnyProcessDefinition() {
    // given
    String processInstanceId = startProcessInstanceByKey(TIMER_BOUNDARY_PROCESS_KEY).getId();
    createGrantAuthorization(PROCESS_DEFINITION, ANY, userId, UPDATE_INSTANCE);
    String jobId = selectJobByProcessInstanceId(processInstanceId).getId();

    // when
    managementService.setJobRetries(jobId, 1);

    // then
    Job job = selectJobById(jobId);
    assertNotNull(job);
    assertEquals(1, job.getRetries());
  }

  // set job due date ///////////////////////////////////////////

  public void testSetJobDueDateWithoutAuthorization() {
    // given
    Job job = selectAnyJob();
    String jobId = job.getId();

    try {
      // when
      managementService.setJobDuedate(jobId, new Date());
      fail("Exception expected: It should not be possible to set the job due date");
    } catch (AuthorizationException e) {
      // then
      // then
      String message = e.getMessage();
      assertTextPresent(userId, message);
      assertTextPresent(UPDATE.getName(), message);
      assertTextPresent(PROCESS_INSTANCE.resourceName(), message);
      assertTextPresent(UPDATE_INSTANCE.getName(), message);
      assertTextPresent(job.getProcessDefinitionKey(), message);
      assertTextPresent(PROCESS_DEFINITION.resourceName(), message);
    }
  }

  public void testSetJobDueDateWithUpdatePermissionOnProcessInstance() {
    // given
    String processInstanceId = startProcessInstanceByKey(TIMER_BOUNDARY_PROCESS_KEY).getId();
    createGrantAuthorization(PROCESS_INSTANCE, processInstanceId, userId, UPDATE);
    String jobId = selectJobByProcessInstanceId(processInstanceId).getId();

    // when
    managementService.setJobDuedate(jobId, null);

    // then
    Job job = selectJobById(jobId);
    assertNull(job.getDuedate());
  }

  public void testSetJobDueDateWithUpdatePermissionOnAnyProcessInstance() {
    // given
    String processInstanceId = startProcessInstanceByKey(TIMER_BOUNDARY_PROCESS_KEY).getId();
    createGrantAuthorization(PROCESS_INSTANCE, ANY, userId, UPDATE);
    String jobId = selectJobByProcessInstanceId(processInstanceId).getId();

    // when
    managementService.setJobDuedate(jobId, null);

    // then
    Job job = selectJobById(jobId);
    assertNull(job.getDuedate());
  }

  public void testSetJobDueDateWithUpdateInstancePermissionOnTimerBoundaryProcessDefinition() {
    // given
    String processInstanceId = startProcessInstanceByKey(TIMER_BOUNDARY_PROCESS_KEY).getId();
    createGrantAuthorization(PROCESS_DEFINITION, TIMER_BOUNDARY_PROCESS_KEY, userId, UPDATE_INSTANCE);
    String jobId = selectJobByProcessInstanceId(processInstanceId).getId();

    // when
    managementService.setJobDuedate(jobId, null);

    // then
    Job job = selectJobById(jobId);
    assertNull(job.getDuedate());
  }

  public void testSetJobDueDateWithUpdateInstancePermissionOnAnyProcessDefinition() {
    // given
    String processInstanceId = startProcessInstanceByKey(TIMER_BOUNDARY_PROCESS_KEY).getId();
    createGrantAuthorization(PROCESS_DEFINITION, ANY, userId, UPDATE_INSTANCE);
    String jobId = selectJobByProcessInstanceId(processInstanceId).getId();

    // when
    managementService.setJobDuedate(jobId, null);

    // then
    Job job = selectJobById(jobId);
    assertNull(job.getDuedate());
  }

  // set job retries (standalone) ////////////////////////////////

  public void testSetStandaloneJobDueDate() {
    // given
    createGrantAuthorization(PROCESS_DEFINITION, TIMER_START_PROCESS_KEY, userId, UPDATE);

    Date startTime = new Date();
    ClockUtil.setCurrentTime(startTime);
    long oneWeekFromStartTime = startTime.getTime() + (7 * 24 * 60 * 60 * 1000);

    disableAuthorization();
    // creates a new "standalone" job
    managementService.suspendJobDefinitionByProcessDefinitionKey(TIMER_START_PROCESS_KEY, false, new Date(oneWeekFromStartTime));
    enableAuthorization();

    String jobId = managementService.createJobQuery().singleResult().getId();

    // when
    managementService.setJobDuedate(jobId, null);

    // then
    Job job = selectJobById(jobId);
    assertNull(job.getDuedate());

    deleteJob(jobId);
  }

  // get exception stacktrace ///////////////////////////////////////////

  public void testGetExceptionStacktraceWithoutAuthorization() {
    // given
    String processInstanceId = startProcessAndExecuteJob(ONE_INCIDENT_PROCESS_KEY).getId();
    disableAuthorization();
    String jobId = selectJobByProcessInstanceId(processInstanceId).getId();

    try {
      // when
      managementService.getJobExceptionStacktrace(jobId);
      fail("Exception expected: It should not be possible to get the exception stacktrace");
    } catch (AuthorizationException e) {
      // then
      String message = e.getMessage();
      assertTextPresent(userId, message);
      assertTextPresent(READ.getName(), message);
      assertTextPresent(processInstanceId, message);
      assertTextPresent(PROCESS_INSTANCE.resourceName(), message);
      assertTextPresent(READ_INSTANCE.getName(), message);
      assertTextPresent(ONE_INCIDENT_PROCESS_KEY, message);
      assertTextPresent(PROCESS_DEFINITION.resourceName(), message);
    }
  }

  public void testGetExceptionStacktraceWithReadPermissionOnProcessInstance() {
    // given
    String processInstanceId = startProcessAndExecuteJob(ONE_INCIDENT_PROCESS_KEY).getId();
    String jobId = selectJobByProcessInstanceId(processInstanceId).getId();
    createGrantAuthorization(PROCESS_INSTANCE, processInstanceId, userId, READ);

    // when
    String jobExceptionStacktrace = managementService.getJobExceptionStacktrace(jobId);

    // then
    assertNotNull(jobExceptionStacktrace);
  }

  public void testGetExceptionStacktraceReadPermissionOnAnyProcessInstance() {
    // given
    String processInstanceId = startProcessAndExecuteJob(ONE_INCIDENT_PROCESS_KEY).getId();
    String jobId = selectJobByProcessInstanceId(processInstanceId).getId();

    createGrantAuthorization(PROCESS_INSTANCE, ANY, userId, READ);

    // when
    String jobExceptionStacktrace = managementService.getJobExceptionStacktrace(jobId);

    // then
    assertNotNull(jobExceptionStacktrace);
  }

  public void testGetExceptionStacktraceWithReadInstancePermissionOnTimerBoundaryProcessDefinition() {
    // given
    String processInstanceId = startProcessAndExecuteJob(ONE_INCIDENT_PROCESS_KEY).getId();
    String jobId = selectJobByProcessInstanceId(processInstanceId).getId();

    createGrantAuthorization(PROCESS_DEFINITION, ONE_INCIDENT_PROCESS_KEY, userId, READ_INSTANCE);

    // when
    String jobExceptionStacktrace = managementService.getJobExceptionStacktrace(jobId);

    // then
    assertNotNull(jobExceptionStacktrace);
  }

  public void testGetExceptionStacktraceWithReadInstancePermissionOnAnyProcessDefinition() {
    // given
    String processInstanceId = startProcessAndExecuteJob(ONE_INCIDENT_PROCESS_KEY).getId();
    String jobId = selectJobByProcessInstanceId(processInstanceId).getId();

    createGrantAuthorization(PROCESS_DEFINITION, ANY, userId, READ_INSTANCE);

    // when
    String jobExceptionStacktrace = managementService.getJobExceptionStacktrace(jobId);

    // then
    assertNotNull(jobExceptionStacktrace);
  }

  // get exception stacktrace (standalone) ////////////////////////////////

  public void testStandaloneJobGetExceptionStacktrace() {
    // given
    createGrantAuthorization(PROCESS_DEFINITION, TIMER_START_PROCESS_KEY, userId, UPDATE);

    Date startTime = new Date();
    ClockUtil.setCurrentTime(startTime);
    long oneWeekFromStartTime = startTime.getTime() + (7 * 24 * 60 * 60 * 1000);

    disableAuthorization();
    // creates a new "standalone" job
    managementService.suspendJobDefinitionByProcessDefinitionKey(TIMER_START_PROCESS_KEY, false, new Date(oneWeekFromStartTime));
    enableAuthorization();

    String jobId = managementService.createJobQuery().singleResult().getId();

    // when
    String jobExceptionStacktrace = managementService.getJobExceptionStacktrace(jobId);

    // then
    assertNull(jobExceptionStacktrace);

    deleteJob(jobId);
  }

  // suspend job by id //////////////////////////////////////////

  public void testSuspendJobByIdWihtoutAuthorization() {
    // given
    String processInstanceId = startProcessInstanceByKey(TIMER_BOUNDARY_PROCESS_KEY).getId();
    String jobId = selectJobByProcessInstanceId(processInstanceId).getId();

    try {
      // when
      managementService.suspendJobById(jobId);
      fail("Exception expected: It should not be possible to suspend a job");
    } catch (AuthorizationException e) {
      // then
      String message = e.getMessage();
      assertTextPresent(userId, message);
      assertTextPresent(UPDATE.getName(), message);
      assertTextPresent(processInstanceId, message);
      assertTextPresent(PROCESS_INSTANCE.resourceName(), message);
      assertTextPresent(UPDATE_INSTANCE.getName(), message);
      assertTextPresent(TIMER_BOUNDARY_PROCESS_KEY, message);
      assertTextPresent(PROCESS_DEFINITION.resourceName(), message);
    }
  }

  public void testSuspendJobByIdWihtUpdatePermissionOnProcessInstance() {
    // given
    String processInstanceId = startProcessInstanceByKey(TIMER_BOUNDARY_PROCESS_KEY).getId();
    String jobId = selectJobByProcessInstanceId(processInstanceId).getId();

    createGrantAuthorization(PROCESS_INSTANCE, processInstanceId, userId, UPDATE);

    // when
    managementService.suspendJobById(jobId);

    // then
    Job job = selectJobByProcessInstanceId(processInstanceId);
    assertNotNull(job);
    assertTrue(job.isSuspended());
  }

  public void testSuspendJobByIdWihtUpdatePermissionOnAnyProcessInstance() {
    // given
    String processInstanceId = startProcessInstanceByKey(TIMER_BOUNDARY_PROCESS_KEY).getId();
    String jobId = selectJobByProcessInstanceId(processInstanceId).getId();

    createGrantAuthorization(PROCESS_INSTANCE, ANY, userId, UPDATE);

    // when
    managementService.suspendJobById(jobId);

    // then
    Job job = selectJobByProcessInstanceId(processInstanceId);
    assertNotNull(job);
    assertTrue(job.isSuspended());
  }

  public void testSuspendJobByIdWihtUpdatePermissionOnProcessDefinition() {
    // given
    String processInstanceId = startProcessInstanceByKey(TIMER_BOUNDARY_PROCESS_KEY).getId();
    String jobId = selectJobByProcessInstanceId(processInstanceId).getId();

    createGrantAuthorization(PROCESS_DEFINITION, TIMER_BOUNDARY_PROCESS_KEY, userId, UPDATE_INSTANCE);

    // when
    managementService.suspendJobById(jobId);

    // then
    Job job = selectJobByProcessInstanceId(processInstanceId);
    assertNotNull(job);
    assertTrue(job.isSuspended());
  }

  public void testSuspendJobByIdWihtUpdatePermissionOnAnyProcessDefinition() {
    // given
    String processInstanceId = startProcessInstanceByKey(TIMER_BOUNDARY_PROCESS_KEY).getId();
    String jobId = selectJobByProcessInstanceId(processInstanceId).getId();

    createGrantAuthorization(PROCESS_DEFINITION, ANY, userId, UPDATE_INSTANCE);

    // when
    managementService.suspendJobById(jobId);

    // then
    Job job = selectJobByProcessInstanceId(processInstanceId);
    assertNotNull(job);
    assertTrue(job.isSuspended());
  }

  public void testSuspendStandaloneJobById() {
    // given
    createGrantAuthorization(PROCESS_DEFINITION, TIMER_START_PROCESS_KEY, userId, UPDATE);

    Date startTime = new Date();
    ClockUtil.setCurrentTime(startTime);
    long oneWeekFromStartTime = startTime.getTime() + (7 * 24 * 60 * 60 * 1000);

    disableAuthorization();
    // creates a new "standalone" job
    managementService.suspendJobDefinitionByProcessDefinitionKey(TIMER_START_PROCESS_KEY, false, new Date(oneWeekFromStartTime));
    enableAuthorization();

    String jobId = managementService.createJobQuery().singleResult().getId();

    // when
    managementService.suspendJobById(jobId);

    // then
    Job job = selectJobById(jobId);
    assertNotNull(job);
    assertTrue(job.isSuspended());

    deleteJob(jobId);
  }

  // activate job by id //////////////////////////////////////////

  public void testActivateJobByIdWihtoutAuthorization() {
    // given
    String processInstanceId = startProcessInstanceByKey(TIMER_BOUNDARY_PROCESS_KEY).getId();
    String jobId = selectJobByProcessInstanceId(processInstanceId).getId();
    suspendJobById(jobId);

    try {
      // when
      managementService.activateJobById(jobId);
      fail("Exception expected: It should not be possible to activate a job");
    } catch (AuthorizationException e) {
      // then
      String message = e.getMessage();
      assertTextPresent(userId, message);
      assertTextPresent(UPDATE.getName(), message);
      assertTextPresent(processInstanceId, message);
      assertTextPresent(PROCESS_INSTANCE.resourceName(), message);
      assertTextPresent(UPDATE_INSTANCE.getName(), message);
      assertTextPresent(TIMER_BOUNDARY_PROCESS_KEY, message);
      assertTextPresent(PROCESS_DEFINITION.resourceName(), message);
    }
  }

  public void testActivateJobByIdWihtUpdatePermissionOnProcessInstance() {
    // given
    String processInstanceId = startProcessInstanceByKey(TIMER_BOUNDARY_PROCESS_KEY).getId();
    String jobId = selectJobByProcessInstanceId(processInstanceId).getId();
    suspendJobById(jobId);

    createGrantAuthorization(PROCESS_INSTANCE, processInstanceId, userId, UPDATE);

    // when
    managementService.activateJobById(jobId);

    // then
    Job job = selectJobByProcessInstanceId(processInstanceId);
    assertNotNull(job);
    assertFalse(job.isSuspended());
  }

  public void testActivateJobByIdWihtUpdatePermissionOnAnyProcessInstance() {
    // given
    String processInstanceId = startProcessInstanceByKey(TIMER_BOUNDARY_PROCESS_KEY).getId();
    String jobId = selectJobByProcessInstanceId(processInstanceId).getId();
    suspendJobById(jobId);

    createGrantAuthorization(PROCESS_INSTANCE, ANY, userId, UPDATE);

    // when
    managementService.activateJobById(jobId);

    // then
    Job job = selectJobByProcessInstanceId(processInstanceId);
    assertNotNull(job);
    assertFalse(job.isSuspended());
  }

  public void testActivateJobByIdWihtUpdatePermissionOnProcessDefinition() {
    // given
    String processInstanceId = startProcessInstanceByKey(TIMER_BOUNDARY_PROCESS_KEY).getId();
    String jobId = selectJobByProcessInstanceId(processInstanceId).getId();
    suspendJobById(jobId);

    createGrantAuthorization(PROCESS_DEFINITION, TIMER_BOUNDARY_PROCESS_KEY, userId, UPDATE_INSTANCE);

    // when
    managementService.activateJobById(jobId);

    // then
    Job job = selectJobByProcessInstanceId(processInstanceId);
    assertNotNull(job);
    assertFalse(job.isSuspended());
  }

  public void testActivateJobByIdWihtUpdatePermissionOnAnyProcessDefinition() {
    // given
    String processInstanceId = startProcessInstanceByKey(TIMER_BOUNDARY_PROCESS_KEY).getId();
    String jobId = selectJobByProcessInstanceId(processInstanceId).getId();
    suspendJobById(jobId);

    createGrantAuthorization(PROCESS_DEFINITION, ANY, userId, UPDATE_INSTANCE);

    // when
    managementService.activateJobById(jobId);

    // then
    Job job = selectJobByProcessInstanceId(processInstanceId);
    assertNotNull(job);
    assertFalse(job.isSuspended());
  }

  public void testActivateStandaloneJobById() {
    // given
    createGrantAuthorization(PROCESS_DEFINITION, TIMER_START_PROCESS_KEY, userId, UPDATE);

    Date startTime = new Date();
    ClockUtil.setCurrentTime(startTime);
    long oneWeekFromStartTime = startTime.getTime() + (7 * 24 * 60 * 60 * 1000);

    disableAuthorization();
    // creates a new "standalone" job
    managementService.suspendJobDefinitionByProcessDefinitionKey(TIMER_START_PROCESS_KEY, false, new Date(oneWeekFromStartTime));
    enableAuthorization();

    String jobId = managementService.createJobQuery().singleResult().getId();
    suspendJobById(jobId);

    // when
    managementService.activateJobById(jobId);

    // then
    Job job = selectJobById(jobId);
    assertNotNull(job);
    assertFalse(job.isSuspended());

    deleteJob(jobId);
  }

  // suspend job by process instance id //////////////////////////////////////////

  public void testSuspendJobByProcessInstanceIdWihtoutAuthorization() {
    // given
    String processInstanceId = startProcessInstanceByKey(TIMER_BOUNDARY_PROCESS_KEY).getId();

    try {
      // when
      managementService.suspendJobByProcessInstanceId(processInstanceId);
      fail("Exception expected: It should not be possible to suspend a job");
    } catch (AuthorizationException e) {
      // then
      String message = e.getMessage();
      assertTextPresent(userId, message);
      assertTextPresent(UPDATE.getName(), message);
      assertTextPresent(processInstanceId, message);
      assertTextPresent(PROCESS_INSTANCE.resourceName(), message);
      assertTextPresent(UPDATE_INSTANCE.getName(), message);
      assertTextPresent(TIMER_BOUNDARY_PROCESS_KEY, message);
      assertTextPresent(PROCESS_DEFINITION.resourceName(), message);
    }
  }

  public void testSuspendJobByProcessInstanceIdWihtUpdatePermissionOnProcessInstance() {
    // given
    String processInstanceId = startProcessInstanceByKey(TIMER_BOUNDARY_PROCESS_KEY).getId();

    createGrantAuthorization(PROCESS_INSTANCE, processInstanceId, userId, UPDATE);

    // when
    managementService.suspendJobByProcessInstanceId(processInstanceId);

    // then
    Job job = selectJobByProcessInstanceId(processInstanceId);
    assertNotNull(job);
    assertTrue(job.isSuspended());
  }

  public void testSuspendJobByProcessInstanceIdWihtUpdatePermissionOnAnyProcessInstance() {
    // given
    String processInstanceId = startProcessInstanceByKey(TIMER_BOUNDARY_PROCESS_KEY).getId();

    createGrantAuthorization(PROCESS_INSTANCE, ANY, userId, UPDATE);

    // when
    managementService.suspendJobByProcessInstanceId(processInstanceId);

    // then
    Job job = selectJobByProcessInstanceId(processInstanceId);
    assertNotNull(job);
    assertTrue(job.isSuspended());
  }

  public void testSuspendJobByProcessInstanceIdWihtUpdatePermissionOnProcessDefinition() {
    // given
    String processInstanceId = startProcessInstanceByKey(TIMER_BOUNDARY_PROCESS_KEY).getId();

    createGrantAuthorization(PROCESS_DEFINITION, TIMER_BOUNDARY_PROCESS_KEY, userId, UPDATE_INSTANCE);

    // when
    managementService.suspendJobByProcessInstanceId(processInstanceId);

    // then
    Job job = selectJobByProcessInstanceId(processInstanceId);
    assertNotNull(job);
    assertTrue(job.isSuspended());
  }

  public void testSuspendJobByProcessInstanceIdWihtUpdatePermissionOnAnyProcessDefinition() {
    // given
    String processInstanceId = startProcessInstanceByKey(TIMER_BOUNDARY_PROCESS_KEY).getId();

    createGrantAuthorization(PROCESS_DEFINITION, ANY, userId, UPDATE_INSTANCE);

    // when
    managementService.suspendJobByProcessInstanceId(processInstanceId);

    // then
    Job job = selectJobByProcessInstanceId(processInstanceId);
    assertNotNull(job);
    assertTrue(job.isSuspended());
  }

  // activate job by process instance id //////////////////////////////////////////

  public void testActivateJobByProcessInstanceIdWihtoutAuthorization() {
    // given
    String processInstanceId = startProcessInstanceByKey(TIMER_BOUNDARY_PROCESS_KEY).getId();
    suspendJobByProcessInstanceId(processInstanceId);

    try {
      // when
      managementService.activateJobByProcessInstanceId(processInstanceId);
      fail("Exception expected: It should not be possible to activate a job");
    } catch (AuthorizationException e) {
      // then
      String message = e.getMessage();
      assertTextPresent(userId, message);
      assertTextPresent(UPDATE.getName(), message);
      assertTextPresent(processInstanceId, message);
      assertTextPresent(PROCESS_INSTANCE.resourceName(), message);
      assertTextPresent(UPDATE_INSTANCE.getName(), message);
      assertTextPresent(TIMER_BOUNDARY_PROCESS_KEY, message);
      assertTextPresent(PROCESS_DEFINITION.resourceName(), message);
    }
  }

  public void testActivateJobByProcessInstanceIdWihtUpdatePermissionOnProcessInstance() {
    // given
    String processInstanceId = startProcessInstanceByKey(TIMER_BOUNDARY_PROCESS_KEY).getId();
    suspendJobByProcessInstanceId(processInstanceId);

    createGrantAuthorization(PROCESS_INSTANCE, processInstanceId, userId, UPDATE);

    // when
    managementService.activateJobByProcessInstanceId(processInstanceId);

    // then
    Job job = selectJobByProcessInstanceId(processInstanceId);
    assertNotNull(job);
    assertFalse(job.isSuspended());
  }

  public void testActivateJobByProcessInstanceIdWihtUpdatePermissionOnAnyProcessInstance() {
    // given
    String processInstanceId = startProcessInstanceByKey(TIMER_BOUNDARY_PROCESS_KEY).getId();
    suspendJobByProcessInstanceId(processInstanceId);

    createGrantAuthorization(PROCESS_INSTANCE, ANY, userId, UPDATE);

    // when
    managementService.activateJobByProcessInstanceId(processInstanceId);

    // then
    Job job = selectJobByProcessInstanceId(processInstanceId);
    assertNotNull(job);
    assertFalse(job.isSuspended());
  }

  public void testActivateJobByProcessInstanceIdWihtUpdatePermissionOnProcessDefinition() {
    // given
    String processInstanceId = startProcessInstanceByKey(TIMER_BOUNDARY_PROCESS_KEY).getId();
    suspendJobByProcessInstanceId(processInstanceId);

    createGrantAuthorization(PROCESS_DEFINITION, TIMER_BOUNDARY_PROCESS_KEY, userId, UPDATE_INSTANCE);

    // when
    managementService.activateJobByProcessInstanceId(processInstanceId);

    // then
    Job job = selectJobByProcessInstanceId(processInstanceId);
    assertNotNull(job);
    assertFalse(job.isSuspended());
  }

  public void testActivateJobByProcessInstanceIdWihtUpdatePermissionOnAnyProcessDefinition() {
    // given
    String processInstanceId = startProcessInstanceByKey(TIMER_BOUNDARY_PROCESS_KEY).getId();
    suspendJobByProcessInstanceId(processInstanceId);

    createGrantAuthorization(PROCESS_DEFINITION, ANY, userId, UPDATE_INSTANCE);

    // when
    managementService.activateJobByProcessInstanceId(processInstanceId);

    // then
    Job job = selectJobByProcessInstanceId(processInstanceId);
    assertNotNull(job);
    assertFalse(job.isSuspended());
  }

  // suspend job by job definition id //////////////////////////////////////////

  public void testSuspendJobByJobDefinitionIdWihtoutAuthorization() {
    // given
    startProcessInstanceByKey(TIMER_BOUNDARY_PROCESS_KEY);
    String jobDefinitionId = selectJobDefinitionByProcessDefinitionKey(TIMER_BOUNDARY_PROCESS_KEY).getId();

    try {
      // when
      managementService.suspendJobByJobDefinitionId(jobDefinitionId);
      fail("Exception expected: It should not be possible to suspend a job");
    } catch (AuthorizationException e) {
      // then
      String message = e.getMessage();
      assertTextPresent(userId, message);
      assertTextPresent(UPDATE.getName(), message);
      assertTextPresent(PROCESS_INSTANCE.resourceName(), message);
      assertTextPresent(UPDATE_INSTANCE.getName(), message);
      assertTextPresent(TIMER_BOUNDARY_PROCESS_KEY, message);
      assertTextPresent(PROCESS_DEFINITION.resourceName(), message);
    }
  }

  public void testSuspendJobByJobDefinitionIdWihtUpdatePermissionOnProcessInstance() {
    // given
    String processInstanceId = startProcessInstanceByKey(TIMER_BOUNDARY_PROCESS_KEY).getId();
    String jobDefinitionId = selectJobDefinitionByProcessDefinitionKey(TIMER_BOUNDARY_PROCESS_KEY).getId();

    createGrantAuthorization(PROCESS_INSTANCE, processInstanceId, userId, UPDATE);

    try {
      // when
      managementService.suspendJobByJobDefinitionId(jobDefinitionId);
      fail("Exception expected: It should not be possible to suspend a job");
    } catch (AuthorizationException e) {
      // then
      String message = e.getMessage();
      assertTextPresent(userId, message);
      assertTextPresent(UPDATE.getName(), message);
      assertTextPresent(PROCESS_INSTANCE.resourceName(), message);
      assertTextPresent(UPDATE_INSTANCE.getName(), message);
      assertTextPresent(TIMER_BOUNDARY_PROCESS_KEY, message);
      assertTextPresent(PROCESS_DEFINITION.resourceName(), message);
    }
  }

  public void testSuspendJobByJobDefinitionIdWihtUpdatePermissionOnAnyProcessInstance() {
    // given
    String processInstanceId = startProcessInstanceByKey(TIMER_BOUNDARY_PROCESS_KEY).getId();
    String jobDefinitionId = selectJobDefinitionByProcessDefinitionKey(TIMER_BOUNDARY_PROCESS_KEY).getId();

    createGrantAuthorization(PROCESS_INSTANCE, ANY, userId, UPDATE);

    // when
    managementService.suspendJobByJobDefinitionId(jobDefinitionId);

    // then
    Job job = selectJobByProcessInstanceId(processInstanceId);
    assertNotNull(job);
    assertTrue(job.isSuspended());
  }

  public void testSuspendJobByJobDefinitionIdWihtUpdatePermissionOnProcessDefinition() {
    // given
    String processInstanceId = startProcessInstanceByKey(TIMER_BOUNDARY_PROCESS_KEY).getId();
    String jobDefinitionId = selectJobDefinitionByProcessDefinitionKey(TIMER_BOUNDARY_PROCESS_KEY).getId();

    createGrantAuthorization(PROCESS_DEFINITION, TIMER_BOUNDARY_PROCESS_KEY, userId, UPDATE_INSTANCE);

    // when
    managementService.suspendJobByJobDefinitionId(jobDefinitionId);

    // then
    Job job = selectJobByProcessInstanceId(processInstanceId);
    assertNotNull(job);
    assertTrue(job.isSuspended());
  }

  public void testSuspendJobByJobDefinitionIdWihtUpdatePermissionOnAnyProcessDefinition() {
    // given
    String processInstanceId = startProcessInstanceByKey(TIMER_BOUNDARY_PROCESS_KEY).getId();
    String jobDefinitionId = selectJobDefinitionByProcessDefinitionKey(TIMER_BOUNDARY_PROCESS_KEY).getId();

    createGrantAuthorization(PROCESS_DEFINITION, ANY, userId, UPDATE_INSTANCE);

    // when
    managementService.suspendJobByJobDefinitionId(jobDefinitionId);

    // then
    Job job = selectJobByProcessInstanceId(processInstanceId);
    assertNotNull(job);
    assertTrue(job.isSuspended());
  }

  // activate job by job definition id //////////////////////////////////////////

  public void testActivateJobByJobDefinitionIdWihtoutAuthorization() {
    // given
    startProcessInstanceByKey(TIMER_BOUNDARY_PROCESS_KEY);
    String jobDefinitionId = selectJobDefinitionByProcessDefinitionKey(TIMER_BOUNDARY_PROCESS_KEY).getId();
    suspendJobByJobDefinitionId(jobDefinitionId);

    try {
      // when
      managementService.activateJobByJobDefinitionId(jobDefinitionId);
      fail("Exception expected: It should not be possible to activate a job");
    } catch (AuthorizationException e) {
      // then
      String message = e.getMessage();
      assertTextPresent(userId, message);
      assertTextPresent(UPDATE.getName(), message);
      assertTextPresent(PROCESS_INSTANCE.resourceName(), message);
      assertTextPresent(UPDATE_INSTANCE.getName(), message);
      assertTextPresent(TIMER_BOUNDARY_PROCESS_KEY, message);
      assertTextPresent(PROCESS_DEFINITION.resourceName(), message);
    }
  }

  public void testActivateJobByJobDefinitionIdWihtUpdatePermissionOnProcessInstance() {
    // given
    String processInstanceId = startProcessInstanceByKey(TIMER_BOUNDARY_PROCESS_KEY).getId();
    String jobDefinitionId = selectJobDefinitionByProcessDefinitionKey(TIMER_BOUNDARY_PROCESS_KEY).getId();
    suspendJobByJobDefinitionId(jobDefinitionId);

    createGrantAuthorization(PROCESS_INSTANCE, processInstanceId, userId, UPDATE);

    try {
      // when
      managementService.activateJobByJobDefinitionId(jobDefinitionId);
      fail("Exception expected: It should not be possible to activate a job");
    } catch (AuthorizationException e) {
      // then
      String message = e.getMessage();
      assertTextPresent(userId, message);
      assertTextPresent(UPDATE.getName(), message);
      assertTextPresent(PROCESS_INSTANCE.resourceName(), message);
      assertTextPresent(UPDATE_INSTANCE.getName(), message);
      assertTextPresent(TIMER_BOUNDARY_PROCESS_KEY, message);
      assertTextPresent(PROCESS_DEFINITION.resourceName(), message);
    }
  }

  public void testActivateJobByJobDefinitionIdWihtUpdatePermissionOnAnyProcessInstance() {
    // given
    String processInstanceId = startProcessInstanceByKey(TIMER_BOUNDARY_PROCESS_KEY).getId();
    String jobDefinitionId = selectJobDefinitionByProcessDefinitionKey(TIMER_BOUNDARY_PROCESS_KEY).getId();
    suspendJobByJobDefinitionId(jobDefinitionId);

    createGrantAuthorization(PROCESS_INSTANCE, ANY, userId, UPDATE);

    // when
    managementService.activateJobByJobDefinitionId(jobDefinitionId);

    // then
    Job job = selectJobByProcessInstanceId(processInstanceId);
    assertNotNull(job);
    assertFalse(job.isSuspended());
  }

  public void testActivateJobByJobDefinitionIdWihtUpdatePermissionOnProcessDefinition() {
    // given
    String processInstanceId = startProcessInstanceByKey(TIMER_BOUNDARY_PROCESS_KEY).getId();
    String jobDefinitionId = selectJobDefinitionByProcessDefinitionKey(TIMER_BOUNDARY_PROCESS_KEY).getId();
    suspendJobByJobDefinitionId(jobDefinitionId);

    createGrantAuthorization(PROCESS_DEFINITION, TIMER_BOUNDARY_PROCESS_KEY, userId, UPDATE_INSTANCE);

    // when
    managementService.activateJobByJobDefinitionId(jobDefinitionId);

    // then
    Job job = selectJobByProcessInstanceId(processInstanceId);
    assertNotNull(job);
    assertFalse(job.isSuspended());
  }

  public void testActivateJobByJobDefinitionIdWihtUpdatePermissionOnAnyProcessDefinition() {
    // given
    String processInstanceId = startProcessInstanceByKey(TIMER_BOUNDARY_PROCESS_KEY).getId();
    String jobDefinitionId = selectJobDefinitionByProcessDefinitionKey(TIMER_BOUNDARY_PROCESS_KEY).getId();
    suspendJobByJobDefinitionId(jobDefinitionId);

    createGrantAuthorization(PROCESS_DEFINITION, ANY, userId, UPDATE_INSTANCE);

    // when
    managementService.activateJobByJobDefinitionId(jobDefinitionId);

    // then
    Job job = selectJobByProcessInstanceId(processInstanceId);
    assertNotNull(job);
    assertFalse(job.isSuspended());
  }

  // suspend job by process definition id //////////////////////////////////////////

  public void testSuspendJobByProcessDefinitionIdWihtoutAuthorization() {
    // given
    startProcessInstanceByKey(TIMER_BOUNDARY_PROCESS_KEY);
    String processDefinitionId = selectProcessDefinitionByKey(TIMER_BOUNDARY_PROCESS_KEY).getId();

    try {
      // when
      managementService.suspendJobByProcessDefinitionId(processDefinitionId);
      fail("Exception expected: It should not be possible to suspend a job");
    } catch (AuthorizationException e) {
      // then
      String message = e.getMessage();
      assertTextPresent(userId, message);
      assertTextPresent(UPDATE.getName(), message);
      assertTextPresent(PROCESS_INSTANCE.resourceName(), message);
      assertTextPresent(UPDATE_INSTANCE.getName(), message);
      assertTextPresent(TIMER_BOUNDARY_PROCESS_KEY, message);
      assertTextPresent(PROCESS_DEFINITION.resourceName(), message);
    }
  }

  public void testSuspendJobByProcessDefinitionIdWihtUpdatePermissionOnProcessInstance() {
    // given
    String processInstanceId = startProcessInstanceByKey(TIMER_BOUNDARY_PROCESS_KEY).getId();
    String processDefinitionId = selectProcessDefinitionByKey(TIMER_BOUNDARY_PROCESS_KEY).getId();

    createGrantAuthorization(PROCESS_INSTANCE, processInstanceId, userId, UPDATE);

    try {
      // when
      managementService.suspendJobByProcessDefinitionId(processDefinitionId);
      fail("Exception expected: It should not be possible to suspend a job");
    } catch (AuthorizationException e) {
      // then
      String message = e.getMessage();
      assertTextPresent(userId, message);
      assertTextPresent(UPDATE.getName(), message);
      assertTextPresent(PROCESS_INSTANCE.resourceName(), message);
      assertTextPresent(UPDATE_INSTANCE.getName(), message);
      assertTextPresent(TIMER_BOUNDARY_PROCESS_KEY, message);
      assertTextPresent(PROCESS_DEFINITION.resourceName(), message);
    }
  }

  public void testSuspendJobByProcessDefinitionIdWihtUpdatePermissionOnAnyProcessInstance() {
    // given
    String processInstanceId = startProcessInstanceByKey(TIMER_BOUNDARY_PROCESS_KEY).getId();
    String processDefinitionId = selectProcessDefinitionByKey(TIMER_BOUNDARY_PROCESS_KEY).getId();

    createGrantAuthorization(PROCESS_INSTANCE, ANY, userId, UPDATE);

    // when
    managementService.suspendJobByProcessDefinitionId(processDefinitionId);

    // then
    Job job = selectJobByProcessInstanceId(processInstanceId);
    assertNotNull(job);
    assertTrue(job.isSuspended());
  }

  public void testSuspendJobByProcessDefinitionIdWihtUpdatePermissionOnProcessDefinition() {
    // given
    String processInstanceId = startProcessInstanceByKey(TIMER_BOUNDARY_PROCESS_KEY).getId();
    String processDefinitionId = selectProcessDefinitionByKey(TIMER_BOUNDARY_PROCESS_KEY).getId();

    createGrantAuthorization(PROCESS_DEFINITION, TIMER_BOUNDARY_PROCESS_KEY, userId, UPDATE_INSTANCE);

    // when
    managementService.suspendJobByProcessDefinitionId(processDefinitionId);

    // then
    Job job = selectJobByProcessInstanceId(processInstanceId);
    assertNotNull(job);
    assertTrue(job.isSuspended());
  }

  public void testSuspendJobByProcessDefinitionIdWihtUpdatePermissionOnAnyProcessDefinition() {
    // given
    String processInstanceId = startProcessInstanceByKey(TIMER_BOUNDARY_PROCESS_KEY).getId();
    String processDefinitionId = selectProcessDefinitionByKey(TIMER_BOUNDARY_PROCESS_KEY).getId();

    createGrantAuthorization(PROCESS_DEFINITION, ANY, userId, UPDATE_INSTANCE);

    // when
    managementService.suspendJobByProcessDefinitionId(processDefinitionId);

    // then
    Job job = selectJobByProcessInstanceId(processInstanceId);
    assertNotNull(job);
    assertTrue(job.isSuspended());
  }

  // activate job by process definition id //////////////////////////////////////////

  public void testActivateJobByProcessDefinitionIdWihtoutAuthorization() {
    // given
    startProcessInstanceByKey(TIMER_BOUNDARY_PROCESS_KEY);
    String processDefinitionId = selectProcessDefinitionByKey(TIMER_BOUNDARY_PROCESS_KEY).getId();
    suspendJobByProcessDefinitionId(processDefinitionId);

    try {
      // when
      managementService.activateJobByProcessDefinitionId(processDefinitionId);
      fail("Exception expected: It should not be possible to activate a job");
    } catch (AuthorizationException e) {
      // then
      String message = e.getMessage();
      assertTextPresent(userId, message);
      assertTextPresent(UPDATE.getName(), message);
      assertTextPresent(PROCESS_INSTANCE.resourceName(), message);
      assertTextPresent(UPDATE_INSTANCE.getName(), message);
      assertTextPresent(TIMER_BOUNDARY_PROCESS_KEY, message);
      assertTextPresent(PROCESS_DEFINITION.resourceName(), message);
    }
  }

  public void testActivateJobByProcessDefinitionIdWihtUpdatePermissionOnProcessInstance() {
    // given
    String processInstanceId = startProcessInstanceByKey(TIMER_BOUNDARY_PROCESS_KEY).getId();
    String processDefinitionId = selectProcessDefinitionByKey(TIMER_BOUNDARY_PROCESS_KEY).getId();
    suspendJobByProcessDefinitionId(processDefinitionId);

    createGrantAuthorization(PROCESS_INSTANCE, processInstanceId, userId, UPDATE);

    try {
      // when
      managementService.activateJobByProcessDefinitionId(processDefinitionId);
      fail("Exception expected: It should not be possible to activate a job");
    } catch (AuthorizationException e) {
      // then
      String message = e.getMessage();
      assertTextPresent(userId, message);
      assertTextPresent(UPDATE.getName(), message);
      assertTextPresent(PROCESS_INSTANCE.resourceName(), message);
      assertTextPresent(UPDATE_INSTANCE.getName(), message);
      assertTextPresent(TIMER_BOUNDARY_PROCESS_KEY, message);
      assertTextPresent(PROCESS_DEFINITION.resourceName(), message);
    }
  }

  public void testActivateJobByProcessDefinitionIdWihtUpdatePermissionOnAnyProcessInstance() {
    // given
    String processInstanceId = startProcessInstanceByKey(TIMER_BOUNDARY_PROCESS_KEY).getId();
    String processDefinitionId = selectProcessDefinitionByKey(TIMER_BOUNDARY_PROCESS_KEY).getId();
    suspendJobByProcessDefinitionId(processDefinitionId);

    createGrantAuthorization(PROCESS_INSTANCE, ANY, userId, UPDATE);

    // when
    managementService.activateJobByProcessDefinitionId(processDefinitionId);

    // then
    Job job = selectJobByProcessInstanceId(processInstanceId);
    assertNotNull(job);
    assertFalse(job.isSuspended());
  }

  public void testActivateJobByProcessDefinitionIdWihtUpdatePermissionOnProcessDefinition() {
    // given
    String processInstanceId = startProcessInstanceByKey(TIMER_BOUNDARY_PROCESS_KEY).getId();
    String processDefinitionId = selectProcessDefinitionByKey(TIMER_BOUNDARY_PROCESS_KEY).getId();
    suspendJobByProcessDefinitionId(processDefinitionId);

    createGrantAuthorization(PROCESS_DEFINITION, TIMER_BOUNDARY_PROCESS_KEY, userId, UPDATE_INSTANCE);

    // when
    managementService.activateJobByProcessDefinitionId(processDefinitionId);

    // then
    Job job = selectJobByProcessInstanceId(processInstanceId);
    assertNotNull(job);
    assertFalse(job.isSuspended());
  }

  public void testActivateJobByProcessDefinitionIdWihtUpdatePermissionOnAnyProcessDefinition() {
    // given
    String processInstanceId = startProcessInstanceByKey(TIMER_BOUNDARY_PROCESS_KEY).getId();
    String processDefinitionId = selectProcessDefinitionByKey(TIMER_BOUNDARY_PROCESS_KEY).getId();
    suspendJobByProcessDefinitionId(processDefinitionId);

    createGrantAuthorization(PROCESS_DEFINITION, ANY, userId, UPDATE_INSTANCE);

    // when
    managementService.activateJobByProcessDefinitionId(processDefinitionId);

    // then
    Job job = selectJobByProcessInstanceId(processInstanceId);
    assertNotNull(job);
    assertFalse(job.isSuspended());
  }

  // suspend job by process definition key //////////////////////////////////////////

  public void testSuspendJobByProcessDefinitionKeyWihtoutAuthorization() {
    // given
    startProcessInstanceByKey(TIMER_BOUNDARY_PROCESS_KEY);

    try {
      // when
      managementService.suspendJobByProcessDefinitionKey(TIMER_BOUNDARY_PROCESS_KEY);
      fail("Exception expected: It should not be possible to suspend a job");
    } catch (AuthorizationException e) {
      // then
      String message = e.getMessage();
      assertTextPresent(userId, message);
      assertTextPresent(UPDATE.getName(), message);
      assertTextPresent(PROCESS_INSTANCE.resourceName(), message);
      assertTextPresent(UPDATE_INSTANCE.getName(), message);
      assertTextPresent(TIMER_BOUNDARY_PROCESS_KEY, message);
      assertTextPresent(PROCESS_DEFINITION.resourceName(), message);
    }
  }

  public void testSuspendJobByProcessDefinitionKeyWihtUpdatePermissionOnProcessInstance() {
    // given
    String processInstanceId = startProcessInstanceByKey(TIMER_BOUNDARY_PROCESS_KEY).getId();
    createGrantAuthorization(PROCESS_INSTANCE, processInstanceId, userId, UPDATE);

    try {
      // when
      managementService.suspendJobByProcessDefinitionKey(TIMER_BOUNDARY_PROCESS_KEY);
      fail("Exception expected: It should not be possible to suspend a job");
    } catch (AuthorizationException e) {
      // then
      String message = e.getMessage();
      assertTextPresent(userId, message);
      assertTextPresent(UPDATE.getName(), message);
      assertTextPresent(PROCESS_INSTANCE.resourceName(), message);
      assertTextPresent(UPDATE_INSTANCE.getName(), message);
      assertTextPresent(TIMER_BOUNDARY_PROCESS_KEY, message);
      assertTextPresent(PROCESS_DEFINITION.resourceName(), message);
    }
  }

  public void testSuspendJobByProcessDefinitionKeyWihtUpdatePermissionOnAnyProcessInstance() {
    // given
    String processInstanceId = startProcessInstanceByKey(TIMER_BOUNDARY_PROCESS_KEY).getId();
    createGrantAuthorization(PROCESS_INSTANCE, ANY, userId, UPDATE);

    // when
    managementService.suspendJobByProcessDefinitionKey(TIMER_BOUNDARY_PROCESS_KEY);

    // then
    Job job = selectJobByProcessInstanceId(processInstanceId);
    assertNotNull(job);
    assertTrue(job.isSuspended());
  }

  public void testSuspendJobByProcessDefinitionKeyWihtUpdatePermissionOnProcessDefinition() {
    // given
    String processInstanceId = startProcessInstanceByKey(TIMER_BOUNDARY_PROCESS_KEY).getId();
    createGrantAuthorization(PROCESS_DEFINITION, TIMER_BOUNDARY_PROCESS_KEY, userId, UPDATE_INSTANCE);

    // when
    managementService.suspendJobByProcessDefinitionKey(TIMER_BOUNDARY_PROCESS_KEY);

    // then
    Job job = selectJobByProcessInstanceId(processInstanceId);
    assertNotNull(job);
    assertTrue(job.isSuspended());
  }

  public void testSuspendJobByProcessDefinitionKeyWihtUpdatePermissionOnAnyProcessDefinition() {
    // given
    String processInstanceId = startProcessInstanceByKey(TIMER_BOUNDARY_PROCESS_KEY).getId();
    createGrantAuthorization(PROCESS_DEFINITION, ANY, userId, UPDATE_INSTANCE);

    // when
    managementService.suspendJobByProcessDefinitionKey(TIMER_BOUNDARY_PROCESS_KEY);

    // then
    Job job = selectJobByProcessInstanceId(processInstanceId);
    assertNotNull(job);
    assertTrue(job.isSuspended());
  }

  // activate job by process definition key //////////////////////////////////////////

  public void testActivateJobByProcessDefinitionKeyWihtoutAuthorization() {
    // given
    startProcessInstanceByKey(TIMER_BOUNDARY_PROCESS_KEY);
    suspendJobByProcessDefinitionKey(TIMER_BOUNDARY_PROCESS_KEY);

    try {
      // when
      managementService.activateJobByProcessDefinitionKey(TIMER_BOUNDARY_PROCESS_KEY);
      fail("Exception expected: It should not be possible to activate a job");
    } catch (AuthorizationException e) {
      // then
      String message = e.getMessage();
      assertTextPresent(userId, message);
      assertTextPresent(UPDATE.getName(), message);
      assertTextPresent(PROCESS_INSTANCE.resourceName(), message);
      assertTextPresent(UPDATE_INSTANCE.getName(), message);
      assertTextPresent(TIMER_BOUNDARY_PROCESS_KEY, message);
      assertTextPresent(PROCESS_DEFINITION.resourceName(), message);
    }
  }

  public void testActivateJobByProcessDefinitionKeyWihtUpdatePermissionOnProcessInstance() {
    // given
    String processInstanceId = startProcessInstanceByKey(TIMER_BOUNDARY_PROCESS_KEY).getId();
    suspendJobByProcessDefinitionKey(TIMER_BOUNDARY_PROCESS_KEY);
    createGrantAuthorization(PROCESS_INSTANCE, processInstanceId, userId, UPDATE);

    try {
      // when
      managementService.activateJobByProcessDefinitionKey(TIMER_BOUNDARY_PROCESS_KEY);
      fail("Exception expected: It should not be possible to activate a job");
    } catch (AuthorizationException e) {
      // then
      String message = e.getMessage();
      assertTextPresent(userId, message);
      assertTextPresent(UPDATE.getName(), message);
      assertTextPresent(PROCESS_INSTANCE.resourceName(), message);
      assertTextPresent(UPDATE_INSTANCE.getName(), message);
      assertTextPresent(TIMER_BOUNDARY_PROCESS_KEY, message);
      assertTextPresent(PROCESS_DEFINITION.resourceName(), message);
    }
  }

  public void testActivateJobByProcessDefinitionKeyWihtUpdatePermissionOnAnyProcessInstance() {
    // given
    String processInstanceId = startProcessInstanceByKey(TIMER_BOUNDARY_PROCESS_KEY).getId();
    suspendJobByProcessDefinitionKey(TIMER_BOUNDARY_PROCESS_KEY);
    createGrantAuthorization(PROCESS_INSTANCE, ANY, userId, UPDATE);

    // when
    managementService.activateJobByProcessDefinitionKey(TIMER_BOUNDARY_PROCESS_KEY);

    // then
    Job job = selectJobByProcessInstanceId(processInstanceId);
    assertNotNull(job);
    assertFalse(job.isSuspended());
  }

  public void testActivateJobByProcessDefinitionKeyWihtUpdatePermissionOnProcessDefinition() {
    // given
    String processInstanceId = startProcessInstanceByKey(TIMER_BOUNDARY_PROCESS_KEY).getId();
    suspendJobByProcessDefinitionKey(TIMER_BOUNDARY_PROCESS_KEY);
    createGrantAuthorization(PROCESS_DEFINITION, TIMER_BOUNDARY_PROCESS_KEY, userId, UPDATE_INSTANCE);

    // when
    managementService.activateJobByProcessDefinitionKey(TIMER_BOUNDARY_PROCESS_KEY);

    // then
    Job job = selectJobByProcessInstanceId(processInstanceId);
    assertNotNull(job);
    assertFalse(job.isSuspended());
  }

  public void testActivateJobByProcessDefinitionKeyWihtUpdatePermissionOnAnyProcessDefinition() {
    // given
    String processInstanceId = startProcessInstanceByKey(TIMER_BOUNDARY_PROCESS_KEY).getId();
    suspendJobByProcessDefinitionKey(TIMER_BOUNDARY_PROCESS_KEY);
    createGrantAuthorization(PROCESS_DEFINITION, ANY, userId, UPDATE_INSTANCE);

    // when
    managementService.activateJobByProcessDefinitionKey(TIMER_BOUNDARY_PROCESS_KEY);

    // then
    Job job = selectJobByProcessInstanceId(processInstanceId);
    assertNotNull(job);
    assertFalse(job.isSuspended());
  }

  // helper /////////////////////////////////////////////////////

  protected void verifyQueryResults(JobQuery query, int countExpected) {
    verifyQueryResults((AbstractQuery<?, ?>) query, countExpected);
  }

  protected Job selectAnyJob() {
    disableAuthorization();
    Job job = managementService.createJobQuery().listPage(0, 1).get(0);
    enableAuthorization();
    return job;
  }

  protected void deleteJob(String jobId) {
    disableAuthorization();
    managementService.deleteJob(jobId);
    enableAuthorization();
  }

  protected Job selectJobByProcessInstanceId(String processInstanceId) {
    disableAuthorization();
    Job job = managementService
        .createJobQuery()
        .processInstanceId(processInstanceId)
        .singleResult();
    enableAuthorization();
    return job;
  }

  protected Job selectJobById(String jobId) {
    disableAuthorization();
    Job job = managementService
        .createJobQuery()
        .jobId(jobId)
        .singleResult();
    enableAuthorization();
    return job;
  }

  protected JobDefinition selectJobDefinitionByProcessDefinitionKey(String processDefinitionKey) {
    disableAuthorization();
    JobDefinition jobDefinition = managementService
        .createJobDefinitionQuery()
        .processDefinitionKey(processDefinitionKey)
        .singleResult();
    enableAuthorization();
    return jobDefinition;
  }

}
