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
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Date;

import org.camunda.bpm.engine.AuthorizationException;
import org.camunda.bpm.engine.impl.AbstractQuery;
import org.camunda.bpm.engine.impl.interceptor.CommandExecutor;
import org.camunda.bpm.engine.impl.jobexecutor.TimerSuspendJobDefinitionHandler;
import org.camunda.bpm.engine.impl.util.ClockUtil;
import org.camunda.bpm.engine.management.JobDefinition;
import org.camunda.bpm.engine.runtime.Job;
import org.camunda.bpm.engine.runtime.JobQuery;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Roman Smirnov
 *
 */
public class JobAuthorizationTest extends AuthorizationTest {

  protected static final String TIMER_START_PROCESS_KEY = "timerStartProcess";
  protected static final String TIMER_BOUNDARY_PROCESS_KEY = "timerBoundaryProcess";
  protected static final String ONE_INCIDENT_PROCESS_KEY = "process";

  @Before
  public void setUp() throws Exception {
    testRule.deploy(
        "org/camunda/bpm/engine/test/api/authorization/timerStartEventProcess.bpmn20.xml",
        "org/camunda/bpm/engine/test/api/authorization/timerBoundaryEventProcess.bpmn20.xml",
        "org/camunda/bpm/engine/test/api/authorization/oneIncidentProcess.bpmn20.xml");
    super.setUp();
  }

  @After
  public void tearDown() {
    super.tearDown();
    CommandExecutor commandExecutor = processEngineConfiguration.getCommandExecutorTxRequired();
    commandExecutor.execute(commandContext -> {
      commandContext.getHistoricJobLogManager().deleteHistoricJobLogsByHandlerType(TimerSuspendJobDefinitionHandler.TYPE);
      return null;
    });
  }

  // job query (jobs associated to a process) //////////////////////////////////////////////////

  @Test
  public void testQueryWithoutAuthorization() {
    // given
    startProcessInstanceByKey(TIMER_BOUNDARY_PROCESS_KEY);

    // when
    JobQuery query = managementService.createJobQuery();

    // then
    verifyQueryResults(query, 0);
  }

  @Test
  public void testQueryWithReadPermissionOnProcessInstance() {
    // given
    String processInstanceId = startProcessInstanceByKey(TIMER_BOUNDARY_PROCESS_KEY).getId();
    createGrantAuthorization(PROCESS_INSTANCE, processInstanceId, userId, READ);

    // when
    JobQuery query = managementService.createJobQuery();

    // then
    verifyQueryResults(query, 1);
  }

  @Test
  public void testQueryWithReadPermissionOnAnyProcessInstance() {
    // given
    startProcessInstanceByKey(TIMER_BOUNDARY_PROCESS_KEY);
    createGrantAuthorization(PROCESS_INSTANCE, ANY, userId, READ);

    // when
    JobQuery query = managementService.createJobQuery();

    // then
    verifyQueryResults(query, 2);
  }

  @Test
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

  @Test
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

  @Test
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

  @Test
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

  @Test
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

  @Test
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

  @Test
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

  @Test
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

  @Test
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

  @Test
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
      testRule.assertTextPresent(userId, message);
      testRule.assertTextPresent(READ.getName() + "' permission on resource '" + processInstanceId + "' of type '" + PROCESS_INSTANCE.resourceName() + "' or '", message);
      testRule.assertTextPresent(READ_INSTANCE.getName() + "' permission on resource '" + ONE_INCIDENT_PROCESS_KEY + "' of type '" + PROCESS_DEFINITION.resourceName() + "'", message);
    }
  }

  @Test
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

  @Test
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

  @Test
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

  @Test
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

  @Test
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


  @Test
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

  @Test
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
