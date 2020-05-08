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
package org.camunda.bpm.engine.test.api.authorization;

import static org.camunda.bpm.engine.authorization.Authorization.ANY;
import static org.camunda.bpm.engine.authorization.Permissions.READ;
import static org.camunda.bpm.engine.authorization.Permissions.READ_INSTANCE;
import static org.camunda.bpm.engine.authorization.Permissions.UPDATE;
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
      assertTextPresent(READ.getName() + "' permission on resource '" + processInstanceId + "' of type '" + PROCESS_INSTANCE.resourceName() + "' or '", message);
      assertTextPresent(READ_INSTANCE.getName() + "' permission on resource '" + ONE_INCIDENT_PROCESS_KEY + "' of type '" + PROCESS_DEFINITION.resourceName() + "'", message);
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
