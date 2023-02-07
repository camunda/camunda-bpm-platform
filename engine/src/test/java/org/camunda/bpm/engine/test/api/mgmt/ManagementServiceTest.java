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
package org.camunda.bpm.engine.test.api.mgmt;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.exception.NotFoundException;
import org.camunda.bpm.engine.exception.NullValueException;
import org.camunda.bpm.engine.history.HistoricIncident;
import org.camunda.bpm.engine.impl.ProcessEngineImpl;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.impl.cmd.AcquireJobsCmd;
import org.camunda.bpm.engine.impl.interceptor.Command;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.interceptor.CommandExecutor;
import org.camunda.bpm.engine.impl.jobexecutor.JobExecutor;
import org.camunda.bpm.engine.impl.persistence.entity.HistoricIncidentEntity;
import org.camunda.bpm.engine.impl.persistence.entity.HistoricIncidentManager;
import org.camunda.bpm.engine.impl.persistence.entity.JobEntity;
import org.camunda.bpm.engine.impl.persistence.entity.JobManager;
import org.camunda.bpm.engine.impl.persistence.entity.MessageEntity;
import org.camunda.bpm.engine.impl.persistence.entity.PropertyEntity;
import org.camunda.bpm.engine.impl.util.ClockUtil;
import org.camunda.bpm.engine.management.JobDefinition;
import org.camunda.bpm.engine.management.TableMetaData;
import org.camunda.bpm.engine.management.TablePage;
import org.camunda.bpm.engine.runtime.Incident;
import org.camunda.bpm.engine.runtime.Job;
import org.camunda.bpm.engine.runtime.JobQuery;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.task.Task;
import org.camunda.bpm.engine.test.Deployment;
import org.camunda.bpm.engine.test.util.PluggableProcessEngineTest;
import org.junit.After;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;


/**
 * @author Frederik Heremans
 * @author Falko Menge
 * @author Saeid Mizaei
 * @author Joram Barrez
 */
public class ManagementServiceTest extends PluggableProcessEngineTest {

  protected boolean tearDownTelemetry;
  protected boolean tearDownEnsureJobDueDateNotNull;

  protected final Date TEST_DUE_DATE = new Date(1675752840000L);

  @After
  public void tearDown() {
    if (tearDownTelemetry) {
      managementService.toggleTelemetry(false);
    }
    if(tearDownEnsureJobDueDateNotNull) {
      processEngineConfiguration.setEnsureJobDueDateNotNull(false);
    }
  }

  @Test
  public void testGetMetaDataForUnexistingTable() {
    TableMetaData metaData = managementService.getTableMetaData("unexistingtable");
    assertNull(metaData);
  }

  @Test
  public void testGetMetaDataNullTableName() {
    try {
      managementService.getTableMetaData(null);
      fail("ProcessEngineException expected");
    } catch (ProcessEngineException re) {
      testRule.assertTextPresent("tableName is null", re.getMessage());
    }
  }

  @Test
  public void testExecuteJobNullJobId() {
    try {
      managementService.executeJob(null);
      fail("ProcessEngineException expected");
    } catch (ProcessEngineException re) {
      testRule.assertTextPresent("jobId is null", re.getMessage());
    }
  }

  @Test
  public void testExecuteJobUnexistingJob() {
    try {
      managementService.executeJob("unexistingjob");
      fail("ProcessEngineException expected");
    } catch (ProcessEngineException ae) {
      testRule.assertTextPresent("No job found with id", ae.getMessage());
    }
  }


  @Deployment
  @Test
  public void testGetJobExceptionStacktrace() {
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("exceptionInJobExecution");

    // The execution is waiting in the first usertask. This contains a boundry
    // timer event which we will execute manual for testing purposes.
    Job timerJob = managementService.createJobQuery()
        .processInstanceId(processInstance.getId())
        .singleResult();

    assertNotNull("No job found for process instance", timerJob);

    try {
      managementService.executeJob(timerJob.getId());
      fail("RuntimeException from within the script task expected");
    } catch (RuntimeException re) {
      testRule.assertTextPresent("This is an exception thrown from scriptTask", re.getMessage());
    }

    // Fetch the task to see that the exception that occurred is persisted
    timerJob = managementService.createJobQuery()
        .processInstanceId(processInstance.getId())
        .singleResult();

    Assert.assertNotNull(timerJob);
    Assert.assertNotNull(timerJob.getExceptionMessage());
    testRule.assertTextPresent("This is an exception thrown from scriptTask", timerJob.getExceptionMessage());

    // Get the full stacktrace using the managementService
    String exceptionStack = managementService.getJobExceptionStacktrace(timerJob.getId());
    Assert.assertNotNull(exceptionStack);
    testRule.assertTextPresent("This is an exception thrown from scriptTask", exceptionStack);
  }

  @Test
  public void testgetJobExceptionStacktraceUnexistingJobId() {
    try {
      managementService.getJobExceptionStacktrace("unexistingjob");
      fail("ProcessEngineException expected");
    } catch (ProcessEngineException re) {
      testRule.assertTextPresent("No job found with id unexistingjob", re.getMessage());
    }
  }

  @Test
  public void testgetJobExceptionStacktraceNullJobId() {
    try {
      managementService.getJobExceptionStacktrace(null);
      fail("ProcessEngineException expected");
    } catch (ProcessEngineException re) {
      testRule.assertTextPresent("jobId is null", re.getMessage());
    }
  }

  @Deployment(resources = {"org/camunda/bpm/engine/test/api/mgmt/ManagementServiceTest.testGetJobExceptionStacktrace.bpmn20.xml"})
  @Test
  public void testSetJobRetries() {
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("exceptionInJobExecution");

    // The execution is waiting in the first usertask. This contains a boundary
    // timer event.
    Job timerJob = managementService.createJobQuery()
        .processInstanceId(processInstance.getId())
        .singleResult();

    assertNotNull("No job found for process instance", timerJob);
    assertEquals(JobEntity.DEFAULT_RETRIES, timerJob.getRetries());

    managementService.setJobRetries(timerJob.getId(), 5);

    timerJob = managementService.createJobQuery()
        .processInstanceId(processInstance.getId())
        .singleResult();
    assertEquals(5, timerJob.getRetries());
  }

  @Deployment(resources = {"org/camunda/bpm/engine/test/api/mgmt/ManagementServiceTest.testGetJobExceptionStacktrace.bpmn20.xml"})
  @Test
  public void testSetMultipleJobRetries() {
    //given
    runtimeService.startProcessInstanceByKey("exceptionInJobExecution");
    runtimeService.startProcessInstanceByKey("exceptionInJobExecution");
    List<String> allJobIds = getAllJobIds();

    //when
    managementService.setJobRetries(allJobIds, 5);

    //then
    assertRetries(allJobIds, 5);
  }

  @Test
  public void shouldThrowExceptionOnSetJobRetriesWithNull() {
    assertThatThrownBy(() -> managementService.setJobRetries((List<String>) null, 5))
      .isInstanceOf(ProcessEngineException.class)
      .hasMessageContaining("job ids is null");
  }

  @Test
  public void shouldThrowExceptionOnSetJobRetriesWithNoJobReference() {
    // given

    // when/then
    assertThatThrownBy(() -> managementService.setJobRetries(5).execute())
      .isInstanceOf(ProcessEngineException.class)
      .hasMessageContaining("052")
      .hasMessageContaining("You must specify exactly one of jobId, jobIds or jobDefinitionId as parameter.");
  }

  @Deployment(resources = {"org/camunda/bpm/engine/test/api/mgmt/ManagementServiceTest.testGetJobExceptionStacktrace.bpmn20.xml"})
  @Test
  public void shouldSetJobRetriesWithDuedateByJobIds() {
    // given
    runtimeService.startProcessInstanceByKey("exceptionInJobExecution");

    List<String> jobIds = getAllJobIds();

    // when
    managementService.setJobRetries(5).jobIds(jobIds).dueDate(TEST_DUE_DATE).execute();

    // then
    List<Job> jobs = managementService.createJobQuery().list();
    for (Job job : jobs) {
      assertThat(job.getRetries()).isEqualTo(5);
      assertThat(job.getDuedate()).isEqualToIgnoringMillis(TEST_DUE_DATE);
    }
  }

  @Deployment(resources = {"org/camunda/bpm/engine/test/api/mgmt/ManagementServiceTest.testGetJobExceptionStacktrace.bpmn20.xml"})
  @Test
  public void shouldSetJobRetriesWithDuedateByJobId() {
    // given
    runtimeService.startProcessInstanceByKey("exceptionInJobExecution");

    List<String> jobIds = getAllJobIds();
    String jobId = jobIds.get(0);

    // when
    managementService.setJobRetries(5).jobId(jobId).dueDate(TEST_DUE_DATE).execute();

    // then
    Job job = managementService.createJobQuery().jobId(jobId).singleResult();
    assertThat(job.getRetries()).isEqualTo(5);
    assertThat(job.getDuedate()).isEqualToIgnoringMillis(TEST_DUE_DATE);
  }

  @Deployment(resources = {"org/camunda/bpm/engine/test/api/mgmt/ManagementServiceTest.testGetJobExceptionStacktrace.bpmn20.xml"})
  @Test
  public void shouldSetJobRetriesWithNullDuedateByJobId() {
    // given
    runtimeService.startProcessInstanceByKey("exceptionInJobExecution");

    List<Job> jobs = managementService.createJobQuery().list();
    Job job = jobs.get(0);
    String jobId = job.getId();

    // when
    managementService.setJobRetries(5).jobId(jobId).dueDate(null).execute();

    // then
    Job updatedJob = managementService.createJobQuery().jobId(jobId).singleResult();
    assertThat(updatedJob.getRetries()).isEqualTo(5);
    assertThat(updatedJob.getDuedate()).isNull();
  }

  @Deployment(resources = {"org/camunda/bpm/engine/test/api/mgmt/ManagementServiceTest.testGetJobExceptionStacktrace.bpmn20.xml"})
  @Test
  public void shouldSetJobRetriesWithDuedateByJobDefinitionId() {
    // given
    runtimeService.startProcessInstanceByKey("exceptionInJobExecution");

    List<Job> list = managementService.createJobQuery().list();
    Job job = list.get(0);
    managementService.setJobRetries(job.getId(), 0);

    // when
    managementService.setJobRetries(5).jobDefinitionId(job.getJobDefinitionId()).dueDate(TEST_DUE_DATE).execute();

    // then
    job = managementService.createJobQuery().jobDefinitionId(job.getJobDefinitionId()).singleResult();
    assertThat(job.getRetries()).isEqualTo(5);
    assertThat(job.getDuedate()).isEqualToIgnoringMillis(TEST_DUE_DATE);
  }

  @Deployment(resources = {"org/camunda/bpm/engine/test/api/mgmt/ManagementServiceTest.testGetJobExceptionStacktrace.bpmn20.xml"})
  @Test
  public void shouldSetJobRetriesWithNullDuedateByJobDefinitionId() {
    // given
    runtimeService.startProcessInstanceByKey("exceptionInJobExecution");

    List<Job> list = managementService.createJobQuery().list();
    Job job = list.get(0);
    managementService.setJobRetries(job.getId(), 0);

    // when
    managementService.setJobRetries(5).jobDefinitionId(job.getJobDefinitionId()).dueDate(null).execute();

    // then
    Job updatedJob = managementService.createJobQuery().jobDefinitionId(job.getJobDefinitionId()).singleResult();
    assertThat(updatedJob.getRetries()).isEqualTo(5);
    assertThat(updatedJob.getDuedate()).isNull();
  }

  @Deployment(resources = {"org/camunda/bpm/engine/test/api/mgmt/ManagementServiceTest.testGetJobExceptionStacktrace.bpmn20.xml"})
  @Test
  public void shouldSetDueDateOnSetJobRetriesWithNullDuedateWhenEnsureDueDateNotNull() {
    // given
    tearDownEnsureJobDueDateNotNull = true;
    processEngineConfiguration.setEnsureJobDueDateNotNull(true);
    runtimeService.startProcessInstanceByKey("exceptionInJobExecution");

    List<Job> list = managementService.createJobQuery().list();
    Job job = list.get(0);
    managementService.setJobRetries(job.getId(), 0);

    // when
    managementService.setJobRetries(5).jobDefinitionId(job.getJobDefinitionId()).dueDate(null).execute();

    // then
    job = managementService.createJobQuery().jobId(job.getId()).singleResult();
    assertThat(job.getDuedate()).isNotNull();
  }

  @Deployment(resources = {"org/camunda/bpm/engine/test/api/mgmt/ManagementServiceTest.testGetJobExceptionStacktrace.bpmn20.xml"})
  @Test
  public void shouldSetDueDateNullOnSetJobRetriesWithNullDuedateWhenNotEnsureDueDateNotNull() {
    // given
    tearDownEnsureJobDueDateNotNull = true;
    processEngineConfiguration.setEnsureJobDueDateNotNull(false);
    runtimeService.startProcessInstanceByKey("exceptionInJobExecution");

    List<Job> list = managementService.createJobQuery().list();
    Job job = list.get(0);
    managementService.setJobRetries(job.getId(), 0);

    // when
    managementService.setJobRetries(5).jobDefinitionId(job.getJobDefinitionId()).dueDate(null).execute();

    // then
    job = managementService.createJobQuery().jobId(job.getId()).singleResult();
    assertThat(job.getDuedate()).isNull();
  }

  @Test
  public void shouldThrowExceptionOnSetJobRetriesWithNegativeRetries() {
    assertThatThrownBy(() -> managementService.setJobRetries("aFake", -1))
      .isInstanceOf(ProcessEngineException.class)
      .hasMessageContaining("54")
      .hasMessageContaining("The number of job retries must be a non-negative Integer, but '-1' has been provided.");
  }

  @Deployment(resources = {"org/camunda/bpm/engine/test/api/mgmt/ManagementServiceTest.testGetJobExceptionStacktrace.bpmn20.xml"})
  @Test
  public void testSetJobRetriesWithFake() {
    //given
    runtimeService.startProcessInstanceByKey("exceptionInJobExecution");

    List<String> allJobIds = getAllJobIds();
    allJobIds.add("aFake");
    try {
      //when
      managementService.setJobRetries(allJobIds, 5);
      fail("exception expected");
      //then
    } catch (ProcessEngineException e) {
      //expected
    }

    assertRetries(getAllJobIds(), JobEntity.DEFAULT_RETRIES);
  }

  protected void assertRetries(List<String> allJobIds, int i) {
    for (String id : allJobIds) {
      assertThat(managementService.createJobQuery().jobId(id).singleResult().getRetries()).isEqualTo(i);
    }
  }

  protected List<String> getAllJobIds() {
    ArrayList<String> result = new ArrayList<>();
    for (Job job : managementService.createJobQuery().list()) {
      result.add(job.getId());
    }
    return result;
  }

  @Deployment(resources = {"org/camunda/bpm/engine/test/api/mgmt/ManagementServiceTest.testGetJobExceptionStacktrace.bpmn20.xml"})
  @Test
  public void testSetJobRetriesNullCreatesIncident() {

    // initially there is no incident
    assertEquals(0, runtimeService.createIncidentQuery().count());

    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("exceptionInJobExecution");

    // The execution is waiting in the first usertask. This contains a boundary
    // timer event.
    Job timerJob = managementService.createJobQuery()
        .processInstanceId(processInstance.getId())
        .singleResult();

    assertNotNull("No job found for process instance", timerJob);
    assertEquals(JobEntity.DEFAULT_RETRIES, timerJob.getRetries());

    managementService.setJobRetries(timerJob.getId(), 0);

    timerJob = managementService.createJobQuery()
        .processInstanceId(processInstance.getId())
        .singleResult();
    assertEquals(0, timerJob.getRetries());

    assertEquals(1, runtimeService.createIncidentQuery().count());

  }

  @Test
  public void shouldThrowExceptionOnSetJobRetriesWithUnexistingJobId() {
    assertThatThrownBy(() -> managementService.setJobRetries("unexistingjob", 5))
      .isInstanceOf(ProcessEngineException.class)
      .hasMessageContaining("053")
      .hasMessageContaining("No job found with id 'unexistingjob'.");
  }

  @Test
  public void shouldThrowExceptionOnSetJobRetriesWithEmptyJobId() {
    assertThatThrownBy(() -> managementService.setJobRetries("", 5))
      .isInstanceOf(ProcessEngineException.class)
      .hasMessageContaining("052")
      .hasMessageContaining("You must specify exactly one of jobId, jobIds or jobDefinitionId as parameter.");
  }

  @Test
  public void testSetJobRetriesJobIdNull() {
    assertThatThrownBy(() -> managementService.setJobRetries((String) null, 5))
      .isInstanceOf(ProcessEngineException.class)
      .hasMessageContaining("052")
      .hasMessageContaining("You must specify exactly one of jobId, jobIds or jobDefinitionId as parameter.");
  }

  @Deployment(resources = {"org/camunda/bpm/engine/test/api/mgmt/ManagementServiceTest.testGetJobExceptionStacktrace.bpmn20.xml"})
  @Test
  public void testSetJobRetriesByJobDefinitionId() {
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("exceptionInJobExecution");
    testRule.executeAvailableJobs();

    JobQuery query = managementService.createJobQuery()
        .processInstanceId(processInstance.getId());

    JobDefinition jobDefinition = managementService
        .createJobDefinitionQuery()
        .singleResult();

    Job timerJob = query.singleResult();

    assertNotNull("No job found for process instance", timerJob);
    assertEquals(0, timerJob.getRetries());

    managementService.setJobRetriesByJobDefinitionId(jobDefinition.getId(), 5);

    timerJob = query.singleResult();
    assertEquals(5, timerJob.getRetries());
  }

  @Test
  public void testSetJobRetriesByJobDefinitionIdEmptyJobDefinitionId() {
      assertThatThrownBy(() -> managementService.setJobRetriesByJobDefinitionId("", 5))
        .isInstanceOf(ProcessEngineException.class)
        .hasMessageContaining("052")
        .hasMessageContaining("You must specify exactly one of jobId, jobIds or jobDefinitionId as parameter.");
  }

  @Test
  public void testSetJobRetriesByJobDefinitionIdNull() {
    assertThatThrownBy(() -> managementService.setJobRetriesByJobDefinitionId(null, 5))
      .isInstanceOf(ProcessEngineException.class)
      .hasMessageContaining("052")
      .hasMessageContaining("You must specify exactly one of jobId, jobIds or jobDefinitionId as parameter.");
  }

  @Test
  public void testSetJobRetriesUnlocksInconsistentJob() {
    // case 1
    // given an inconsistent job that is never again picked up by a job executor
    createJob(0, "owner", ClockUtil.getCurrentTime());

    // when the job retries are reset
    JobEntity job = (JobEntity) managementService.createJobQuery().singleResult();
    managementService.setJobRetries(job.getId(), 3);

    // then the job can be picked up again
    job = (JobEntity) managementService.createJobQuery().singleResult();
    assertNotNull(job);
    assertNull(job.getLockOwner());
    assertNull(job.getLockExpirationTime());
    assertEquals(3, job.getRetries());

    deleteJobAndIncidents(job);

    // case 2
    // given an inconsistent job that is never again picked up by a job executor
    createJob(2, "owner", null);

    // when the job retries are reset
    job = (JobEntity) managementService.createJobQuery().singleResult();
    managementService.setJobRetries(job.getId(), 3);

    // then the job can be picked up again
    job = (JobEntity) managementService.createJobQuery().singleResult();
    assertNotNull(job);
    assertNull(job.getLockOwner());
    assertNull(job.getLockExpirationTime());
    assertEquals(3, job.getRetries());

    deleteJobAndIncidents(job);

    // case 3
    // given a consistent job
    createJob(2, "owner", ClockUtil.getCurrentTime());

    // when the job retries are reset
    job = (JobEntity) managementService.createJobQuery().singleResult();
    managementService.setJobRetries(job.getId(), 3);

    // then the lock owner and expiration should not change
    job = (JobEntity) managementService.createJobQuery().singleResult();
    assertNotNull(job);
    assertNotNull(job.getLockOwner());
    assertNotNull(job.getLockExpirationTime());
    assertEquals(3, job.getRetries());

    deleteJobAndIncidents(job);
  }

  protected void createJob(final int retries, final String owner, final Date lockExpirationTime) {
    CommandExecutor commandExecutor = processEngineConfiguration.getCommandExecutorTxRequired();
    commandExecutor.execute(new Command<Void>() {
      @Override
      public Void execute(CommandContext commandContext) {
        JobManager jobManager = commandContext.getJobManager();
        MessageEntity job = new MessageEntity();
        job.setJobHandlerType("any");
        job.setLockOwner(owner);
        job.setLockExpirationTime(lockExpirationTime);
        job.setRetries(retries);

        jobManager.send(job);
        return null;
      }
    });
  }

  @Deployment(resources = {"org/camunda/bpm/engine/test/api/mgmt/ManagementServiceTest.testGetJobExceptionStacktrace.bpmn20.xml"})
  @Test
  public void testSetJobRetriesByDefinitionUnlocksInconsistentJobs() {
    // given a job definition
    final JobDefinition jobDefinition = managementService.createJobDefinitionQuery().singleResult();

    // and an inconsistent job that is never again picked up by a job executor
    CommandExecutor commandExecutor = processEngineConfiguration.getCommandExecutorTxRequired();
    commandExecutor.execute(new Command<Void>() {
      @Override
      public Void execute(CommandContext commandContext) {
        JobManager jobManager = commandContext.getJobManager();
        MessageEntity job = new MessageEntity();
        job.setJobDefinitionId(jobDefinition.getId());
        job.setJobHandlerType("any");
        job.setLockOwner("owner");
        job.setLockExpirationTime(ClockUtil.getCurrentTime());
        job.setRetries(0);

        jobManager.send(job);
        return null;
      }
    });

    // when the job retries are reset
    managementService.setJobRetriesByJobDefinitionId(jobDefinition.getId(), 3);

    // then the job can be picked up again
    JobEntity job = (JobEntity) managementService.createJobQuery().singleResult();
    assertNotNull(job);
    assertNull(job.getLockOwner());
    assertNull(job.getLockExpirationTime());
    assertEquals(3, job.getRetries());

    deleteJobAndIncidents(job);
  }

  protected void deleteJobAndIncidents(final Job job) {
    final List<HistoricIncident> incidents =
        historyService.createHistoricIncidentQuery()
            .incidentType(Incident.FAILED_JOB_HANDLER_TYPE).list();

    CommandExecutor commandExecutor = processEngineConfiguration.getCommandExecutorTxRequired();
    commandExecutor.execute(new Command<Void>() {
      @Override
      public Void execute(CommandContext commandContext) {
        ((JobEntity) job).delete();

        HistoricIncidentManager historicIncidentManager = commandContext.getHistoricIncidentManager();
        for (HistoricIncident incident : incidents) {
          HistoricIncidentEntity incidentEntity = (HistoricIncidentEntity) incident;
          historicIncidentManager.delete(incidentEntity);
        }

        commandContext.getHistoricJobLogManager().deleteHistoricJobLogByJobId(job.getId());
        return null;
      }
    });
  }

  @Test
  public void testDeleteJobNullJobId() {
    try {
      managementService.deleteJob(null);
      fail("ProcessEngineException expected");
    } catch (ProcessEngineException re) {
      testRule.assertTextPresent("jobId is null", re.getMessage());
    }
  }

  @Test
  public void testDeleteJobUnexistingJob() {
    try {
      managementService.deleteJob("unexistingjob");
      fail("ProcessEngineException expected");
    } catch (ProcessEngineException ae) {
      testRule.assertTextPresent("No job found with id", ae.getMessage());
    }
  }

  @Deployment(resources = {"org/camunda/bpm/engine/test/api/mgmt/timerOnTask.bpmn20.xml"})
  @Test
  public void testDeleteJobDeletion() {
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("timerOnTask");
    Job timerJob = managementService.createJobQuery().processInstanceId(processInstance.getId()).singleResult();

    assertNotNull("Task timer should be there", timerJob);
    managementService.deleteJob(timerJob.getId());

    timerJob = managementService.createJobQuery().processInstanceId(processInstance.getId()).singleResult();
    assertNull("There should be no job now. It was deleted", timerJob);
  }

  @Deployment(resources = {"org/camunda/bpm/engine/test/api/mgmt/timerOnTask.bpmn20.xml"})
  @Test
  public void testDeleteJobThatWasAlreadyAcquired() {
    ClockUtil.setCurrentTime(new Date());

    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("timerOnTask");
    Job timerJob = managementService.createJobQuery().processInstanceId(processInstance.getId()).singleResult();

    // We need to move time at least one hour to make the timer executable
    ClockUtil.setCurrentTime(new Date(ClockUtil.getCurrentTime().getTime() + 7200000L));

    // Acquire job by running the acquire command manually
    ProcessEngineImpl processEngineImpl = (ProcessEngineImpl) processEngine;
    JobExecutor jobExecutor = processEngineImpl.getProcessEngineConfiguration().getJobExecutor();
    AcquireJobsCmd acquireJobsCmd = new AcquireJobsCmd(jobExecutor);
    CommandExecutor commandExecutor = processEngineImpl.getProcessEngineConfiguration().getCommandExecutorTxRequired();
    commandExecutor.execute(acquireJobsCmd);

    // Try to delete the job. This should fail.
    try {
      managementService.deleteJob(timerJob.getId());
      fail();
    } catch (ProcessEngineException e) {
      // Exception is expected
    }

    // Clean up
    managementService.executeJob(timerJob.getId());
  }

  @Deployment(resources = {"org/camunda/bpm/engine/test/api/mgmt/ManagementServiceTest.testGetJobExceptionStacktrace.bpmn20.xml"})
  @Test
  public void testSetJobDuedate() {
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("exceptionInJobExecution");

    // The execution is waiting in the first usertask. This contains a boundary
    // timer event.
    Job timerJob = managementService.createJobQuery()
        .processInstanceId(processInstance.getId())
        .singleResult();

    assertNotNull("No job found for process instance", timerJob);
    assertNotNull(timerJob.getDuedate());

    Calendar cal = Calendar.getInstance();
    cal.setTime(new Date());
    cal.add(Calendar.DATE, 3); // add 3 days on the actual date
    managementService.setJobDuedate(timerJob.getId(), cal.getTime());

    Job newTimerJob = managementService.createJobQuery()
        .processInstanceId(processInstance.getId())
        .singleResult();

    // normalize date for mysql dropping fractional seconds in time values
    int SECOND = 1000;
    assertEquals((cal.getTime().getTime() / SECOND) * SECOND,
        (newTimerJob.getDuedate().getTime() / SECOND) * SECOND);
  }

  @Deployment(resources = {"org/camunda/bpm/engine/test/api/mgmt/ManagementServiceTest.testGetJobExceptionStacktrace.bpmn20.xml"})
  @Test
  public void testSetJobDuedateDateNull() {
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("exceptionInJobExecution");

    // The execution is waiting in the first usertask. This contains a boundary
    // timer event.
    Job timerJob = managementService.createJobQuery()
        .processInstanceId(processInstance.getId())
        .singleResult();

    assertNotNull("No job found for process instance", timerJob);
    assertNotNull(timerJob.getDuedate());

    managementService.setJobDuedate(timerJob.getId(), null);

    timerJob = managementService.createJobQuery()
        .processInstanceId(processInstance.getId())
        .singleResult();

    assertNull(timerJob.getDuedate());
  }


  @Test
  public void testSetJobDuedateJobIdNull() {
    try {
      managementService.setJobDuedate(null, new Date());
      fail("ProcessEngineException expected");
    } catch (ProcessEngineException re) {
      testRule.assertTextPresent("The job id is mandatory, but 'null' has been provided.", re.getMessage());
    }
  }

  @Test
  public void testSetJobDuedateEmptyJobId() {
    try {
      managementService.setJobDuedate("", new Date());
      fail("ProcessEngineException expected");
    } catch (ProcessEngineException re) {
      testRule.assertTextPresent("The job id is mandatory, but '' has been provided.", re.getMessage());
    }
  }

  @Test
  public void testSetJobDuedateUnexistingJobId() {
    try {
      managementService.setJobDuedate("unexistingjob", new Date());
      fail("ProcessEngineException expected");
    } catch (ProcessEngineException re) {
      testRule.assertTextPresent("No job found with id 'unexistingjob'.", re.getMessage());
    }
  }

  @Deployment(resources = "org/camunda/bpm/engine/test/bpmn/job/oneTaskProcess.bpmn20.xml")
  @Test
  public void testSetJobDuedateNonTimerJob(){
    runtimeService.startProcessInstanceByKey("oneTaskProcess");
    Job job = managementService.createJobQuery().processDefinitionKey("oneTaskProcess").singleResult();
    assertNotNull(job);
    managementService.setJobDuedate(job.getId(), new Date());
    job = managementService.createJobQuery().processDefinitionKey("oneTaskProcess").singleResult();
    assertNotNull(job.getDuedate());
  }

  @Test
  public void testGetProperties() {
    Map<String, String> properties = managementService.getProperties();
    assertNotNull(properties);
    assertFalse(properties.isEmpty());
  }

  @Test
  public void testSetProperty() {
    final String name = "testProp";
    final String value = "testValue";
    managementService.setProperty(name, value);

    Map<String, String> properties = managementService.getProperties();
    assertTrue(properties.containsKey(name));
    String storedValue = properties.get(name);
    assertEquals(value, storedValue);

    managementService.deleteProperty(name);
  }

  @Test
  public void testDeleteProperty() {
    final String name = "testProp";
    final String value = "testValue";
    managementService.setProperty(name, value);

    Map<String, String> properties = managementService.getProperties();
    assertTrue(properties.containsKey(name));
    String storedValue = properties.get(name);
    assertEquals(value, storedValue);

    managementService.deleteProperty(name);
    properties = managementService.getProperties();
    assertFalse(properties.containsKey(name));

  }

  @Test
  public void testDeleteNonexistingProperty() {

    managementService.deleteProperty("non existing");

  }

  @Test
  public void testGetHistoryLevel() {
    int historyLevel = managementService.getHistoryLevel();
    assertEquals(processEngineConfiguration.getHistoryLevel().getId(), historyLevel);
  }

  @Deployment(resources = "org/camunda/bpm/engine/test/api/mgmt/asyncTaskProcess.bpmn20.xml")
  @Test
  public void testSetJobPriority() {
    // given
    runtimeService
        .createProcessInstanceByKey("asyncTaskProcess")
        .startBeforeActivity("task")
        .execute();

    Job job = managementService.createJobQuery().singleResult();

    // when
    managementService.setJobPriority(job.getId(), 42);

    // then
    job = managementService.createJobQuery().singleResult();

    assertEquals(42, job.getPriority());
  }

  @Test
  public void testSetJobPriorityForNonExistingJob() {
    try {
      managementService.setJobPriority("nonExistingJob", 42);
      fail("should not succeed");
    } catch (NotFoundException e) {
      testRule.assertTextPresentIgnoreCase("No job found with id 'nonExistingJob'", e.getMessage());
    }
  }

  @Test
  public void testSetJobPriorityForNullJob() {
    try {
      managementService.setJobPriority(null, 42);
      fail("should not succeed");
    } catch (NullValueException e) {
      testRule.assertTextPresentIgnoreCase("Job id must not be null", e.getMessage());
    }
  }

  @Deployment(resources = "org/camunda/bpm/engine/test/api/mgmt/asyncTaskProcess.bpmn20.xml")
  @Test
  public void testSetJobPriorityToExtremeValues() {
    runtimeService
        .createProcessInstanceByKey("asyncTaskProcess")
        .startBeforeActivity("task")
        .execute();

    Job job = managementService.createJobQuery().singleResult();

    // it is possible to set the max integer value
    managementService.setJobPriority(job.getId(), Long.MAX_VALUE);
    job = managementService.createJobQuery().singleResult();
    assertEquals(Long.MAX_VALUE, job.getPriority());

    // it is possible to set the min integer value
    managementService.setJobPriority(job.getId(), Long.MIN_VALUE + 1); // +1 for informix
    job = managementService.createJobQuery().singleResult();
    assertEquals(Long.MIN_VALUE + 1, job.getPriority());
  }

  @Ignore
  @Test
  public void testGetTableMetaData() {

    TableMetaData tableMetaData = managementService.getTableMetaData("ACT_RU_TASK");
    assertThat(tableMetaData.getColumnNames().size()).isEqualTo(tableMetaData.getColumnTypes().size());
    assertThat(tableMetaData.getColumnNames()).contains("ID_", "REV_","NAME_", "PARENT_TASK_ID_",
        "PRIORITY_", "CREATE_TIME_", "LAST_UPDATED_", "OWNER_", "ASSIGNEE_", "DELEGATION_", "EXECUTION_ID_",
        "PROC_DEF_ID_", "PROC_INST_ID_", "CASE_EXECUTION_ID_","CASE_INST_ID_", "CASE_DEF_ID_", "TASK_DEF_KEY_",
        "DESCRIPTION_", "DUE_DATE_", "FOLLOW_UP_DATE_", "SUSPENSION_STATE_", "TENANT_ID_");

    int assigneeIndex = tableMetaData.getColumnNames().indexOf("ASSIGNEE_");
    int createTimeIndex = tableMetaData.getColumnNames().indexOf("CREATE_TIME_");

    assertThat(assigneeIndex >= 0).isTrue();
    assertThat(createTimeIndex >= 0).isTrue();

    assertThat(tableMetaData.getColumnTypes().get(assigneeIndex)).isIn("CHARACTER VARYING", "VARCHAR", "NVARCHAR2", "nvarchar", "NVARCHAR");
    assertThat(tableMetaData.getColumnTypes().get(createTimeIndex)).isIn("TIMESTAMP", "TIMESTAMP(6)", "datetime", "DATETIME", "DATETIME2");
  }

  private void assertOneOf(String[] possibleValues, String currentValue) {
    for (String value : possibleValues) {
      if (currentValue.equals(value)) {
        return;
      }
    }
    fail("Value '" + currentValue + "' should be one of: " + Arrays.deepToString(possibleValues));
  }

  @Test
  public void testGetTablePage() {
    String tablePrefix = processEngineConfiguration.getDatabaseTablePrefix();
    List<String> taskIds = generateDummyTasks(20);

    TablePage tablePage = managementService.createTablePageQuery()
        .tableName(tablePrefix + "ACT_RU_TASK")
        .listPage(0, 5);

    assertEquals(0, tablePage.getFirstResult());
    assertEquals(5, tablePage.getSize());
    assertEquals(5, tablePage.getRows().size());
    assertEquals(20, tablePage.getTotal());

    tablePage = managementService.createTablePageQuery()
        .tableName(tablePrefix + "ACT_RU_TASK")
        .listPage(14, 10);

    assertEquals(14, tablePage.getFirstResult());
    assertEquals(6, tablePage.getSize());
    assertEquals(6, tablePage.getRows().size());
    assertEquals(20, tablePage.getTotal());

    taskService.deleteTasks(taskIds, true);
  }

  @Test
  public void testGetSortedTablePage() {
    String tablePrefix = processEngineConfiguration.getDatabaseTablePrefix();
    List<String> taskIds = generateDummyTasks(15);

    // With an ascending sort
    TablePage tablePage = managementService.createTablePageQuery()
        .tableName(tablePrefix + "ACT_RU_TASK")
        .orderAsc("NAME_")
        .listPage(1, 7);
    String[] expectedTaskNames = new String[]{"B", "C", "D", "E", "F", "G", "H"};
    verifyTaskNames(expectedTaskNames, tablePage.getRows());

    // With a descending sort
    tablePage = managementService.createTablePageQuery()
        .tableName(tablePrefix + "ACT_RU_TASK")
        .orderDesc("NAME_")
        .listPage(6, 8);
    expectedTaskNames = new String[]{"I", "H", "G", "F", "E", "D", "C", "B"};
    verifyTaskNames(expectedTaskNames, tablePage.getRows());

    taskService.deleteTasks(taskIds, true);
  }

  @Test
  public void testFetchTelemetryConfiguration() {
    // given default configuration
    boolean expectedTelemetryValue = Boolean.parseBoolean(getTelemetryProperty(processEngineConfiguration).getValue());

    // when
    boolean actualTelemetryValue = managementService.isTelemetryEnabled();

    // then
    assertThat(actualTelemetryValue).isEqualTo(expectedTelemetryValue);
  }

  @Test
  public void testTelemetryEnabled() {
    // given default configuration
    tearDownTelemetry = true;

    // when
    managementService.toggleTelemetry(true);

    // then
    assertThat(managementService.isTelemetryEnabled()).isTrue();
  }

  @Test
  public void testTelemetryDisabled() {
    // given
    tearDownTelemetry = true;

    managementService.toggleTelemetry(true);

    // when
    managementService.toggleTelemetry(false);

    // then
    assertThat(managementService.isTelemetryEnabled()).isFalse();
  }

  private void verifyTaskNames(String[] expectedTaskNames, List<Map<String, Object>> rowData) {
    assertEquals(expectedTaskNames.length, rowData.size());
    String columnKey = "NAME_";

    for (int i = 0; i < expectedTaskNames.length; i++) {
      Object o = rowData.get(i).get(columnKey);
      if (o == null) {
        o = rowData.get(i).get(columnKey.toLowerCase());
      }
      assertEquals(expectedTaskNames[i], o);
    }
  }

  private List<String> generateDummyTasks(int nrOfTasks) {
    ArrayList<String> taskIds = new ArrayList<>();
    for (int i = 0; i < nrOfTasks; i++) {
      Task task = taskService.newTask();
      task.setName(((char) ('A' + i)) + "");
      taskService.saveTask(task);
      taskIds.add(task.getId());
    }
    return taskIds;
  }


  protected PropertyEntity getTelemetryProperty(ProcessEngineConfigurationImpl configuration) {
      return configuration.getCommandExecutorTxRequired()
        .execute(new Command<PropertyEntity>() {
          public PropertyEntity execute(CommandContext commandContext) {
            return commandContext.getPropertyManager().findPropertyById("camunda.telemetry.enabled");
          }
        });
  }
}
