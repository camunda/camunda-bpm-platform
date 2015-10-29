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

package org.camunda.bpm.engine.test.api.mgmt;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.exception.NotFoundException;
import org.camunda.bpm.engine.exception.NullValueException;
import org.camunda.bpm.engine.history.HistoricIncident;
import org.camunda.bpm.engine.impl.ProcessEngineImpl;
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
import org.camunda.bpm.engine.impl.test.PluggableProcessEngineTestCase;
import org.camunda.bpm.engine.impl.util.ClockUtil;
import org.camunda.bpm.engine.management.JobDefinition;
import org.camunda.bpm.engine.management.TableMetaData;
import org.camunda.bpm.engine.runtime.Incident;
import org.camunda.bpm.engine.runtime.Job;
import org.camunda.bpm.engine.runtime.JobQuery;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.test.Deployment;
import org.junit.Assert;


/**
 * @author Frederik Heremans
 * @author Falko Menge
 * @author Saeid Mizaei
 * @author Joram Barrez
 */
public class ManagementServiceTest extends PluggableProcessEngineTestCase {

  public void testGetMetaDataForUnexistingTable() {
    TableMetaData metaData = managementService.getTableMetaData("unexistingtable");
    assertNull(metaData);
  }

  public void testGetMetaDataNullTableName() {
    try {
      managementService.getTableMetaData(null);
      fail("ProcessEngineException expected");
    } catch (ProcessEngineException re) {
      assertTextPresent("tableName is null", re.getMessage());
    }
  }

  public void testExecuteJobNullJobId() {
    try {
      managementService.executeJob(null);
      fail("ProcessEngineException expected");
    } catch (ProcessEngineException re) {
      assertTextPresent("jobId is null", re.getMessage());
    }
  }

  public void testExecuteJobUnexistingJob() {
    try {
      managementService.executeJob("unexistingjob");
      fail("ProcessEngineException expected");
    } catch (ProcessEngineException ae) {
      assertTextPresent("No job found with id", ae.getMessage());
    }
  }


  @Deployment
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
    } catch(RuntimeException re) {
      assertTextPresent("This is an exception thrown from scriptTask", re.getMessage());
    }

    // Fetch the task to see that the exception that occurred is persisted
    timerJob = managementService.createJobQuery()
    .processInstanceId(processInstance.getId())
    .singleResult();

    Assert.assertNotNull(timerJob);
    Assert.assertNotNull(timerJob.getExceptionMessage());
    assertTextPresent("This is an exception thrown from scriptTask", timerJob.getExceptionMessage());

    // Get the full stacktrace using the managementService
    String exceptionStack = managementService.getJobExceptionStacktrace(timerJob.getId());
    Assert.assertNotNull(exceptionStack);
    assertTextPresent("This is an exception thrown from scriptTask", exceptionStack);
  }

  public void testgetJobExceptionStacktraceUnexistingJobId() {
    try {
      managementService.getJobExceptionStacktrace("unexistingjob");
      fail("ProcessEngineException expected");
    } catch (ProcessEngineException re) {
      assertTextPresent("No job found with id unexistingjob", re.getMessage());
    }
  }

  public void testgetJobExceptionStacktraceNullJobId() {
    try {
      managementService.getJobExceptionStacktrace(null);
      fail("ProcessEngineException expected");
    } catch (ProcessEngineException re) {
      assertTextPresent("jobId is null", re.getMessage());
    }
  }

  @Deployment(resources = {"org/camunda/bpm/engine/test/api/mgmt/ManagementServiceTest.testGetJobExceptionStacktrace.bpmn20.xml"})
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

  public void testSetJobRetriesUnexistingJobId() {
    try {
      managementService.setJobRetries("unexistingjob", 5);
      fail("ProcessEngineException expected");
    } catch (ProcessEngineException re) {
      assertTextPresent("No job found with id 'unexistingjob'.", re.getMessage());
    }
  }

  public void testSetJobRetriesEmptyJobId() {
    try {
      managementService.setJobRetries("", 5);
      fail("ProcessEngineException expected");
    } catch (ProcessEngineException re) {
      assertTextPresent("Either job definition id or job id has to be provided as parameter.", re.getMessage());
    }
  }

  public void testSetJobRetriesJobIdNull() {
    try {
      managementService.setJobRetries(null, 5);
      fail("ProcessEngineException expected");
    } catch (ProcessEngineException re) {
      assertTextPresent("Either job definition id or job id has to be provided as parameter.", re.getMessage());
    }
  }

  public void testSetJobRetriesNegativeNumberOfRetries() {
    try {
      managementService.setJobRetries("unexistingjob", -1);
      fail("ProcessEngineException expected");
    } catch (ProcessEngineException re) {
      assertTextPresent("The number of job retries must be a non-negative Integer, but '-1' has been provided.", re.getMessage());
    }
  }

  @Deployment(resources = {"org/camunda/bpm/engine/test/api/mgmt/ManagementServiceTest.testGetJobExceptionStacktrace.bpmn20.xml"})
  public void testSetJobRetriesByJobDefinitionId() {
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("exceptionInJobExecution");
    executeAvailableJobs();

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

  public void testSetJobRetriesByJobDefinitionIdEmptyJobDefinitionId() {
    try {
      managementService.setJobRetriesByJobDefinitionId("", 5);
      fail("ProcessEngineException expected");
    } catch (ProcessEngineException re) {
      assertTextPresent("Either job definition id or job id has to be provided as parameter.", re.getMessage());
    }
  }

  public void testSetJobRetriesByJobDefinitionIdNull() {
    try {
      managementService.setJobRetriesByJobDefinitionId(null, 5);
      fail("ProcessEngineException expected");
    } catch (ProcessEngineException re) {
      assertTextPresent("Either job definition id or job id has to be provided as parameter.", re.getMessage());
    }
  }

  public void testSetJobRetriesByJobDefinitionIdNegativeNumberOfRetries() {
    try {
      managementService.setJobRetries("unexistingjob", -1);
      fail("ProcessEngineException expected");
    } catch (ProcessEngineException re) {
      assertTextPresent("The number of job retries must be a non-negative Integer, but '-1' has been provided.", re.getMessage());
    }
  }

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
  public void testSetJobRetriesByDefinitionUnlocksInconsistentJobs() {
    // given a job definition
    final JobDefinition jobDefinition = managementService.createJobDefinitionQuery().singleResult();

    // and an inconsistent job that is never again picked up by a job executor
    CommandExecutor commandExecutor = processEngineConfiguration.getCommandExecutorTxRequired();
    commandExecutor.execute(new Command<Void>() {
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

  public void testDeleteJobNullJobId() {
    try {
      managementService.deleteJob(null);
      fail("ProcessEngineException expected");
    } catch (ProcessEngineException re) {
      assertTextPresent("jobId is null", re.getMessage());
    }
  }

  public void testDeleteJobUnexistingJob() {
    try {
      managementService.deleteJob("unexistingjob");
      fail("ProcessEngineException expected");
    } catch (ProcessEngineException ae) {
      assertTextPresent("No job found with id", ae.getMessage());
    }
  }

  @Deployment(resources = { "org/camunda/bpm/engine/test/api/mgmt/timerOnTask.bpmn20.xml" })
  public void testDeleteJobDeletion() {
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("timerOnTask");
    Job timerJob = managementService.createJobQuery().processInstanceId(processInstance.getId()).singleResult();

    assertNotNull("Task timer should be there", timerJob);
    managementService.deleteJob(timerJob.getId());

    timerJob = managementService.createJobQuery().processInstanceId(processInstance.getId()).singleResult();
    assertNull("There should be no job now. It was deleted", timerJob);
  }

  @Deployment(resources = { "org/camunda/bpm/engine/test/api/mgmt/timerOnTask.bpmn20.xml" })
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


  public void testSetJobDuedateJobIdNull() {
    try {
      managementService.setJobDuedate(null, new Date());
      fail("ProcessEngineException expected");
    } catch (ProcessEngineException re) {
      assertTextPresent("The job id is mandatory, but 'null' has been provided.", re.getMessage());
    }
  }

  public void testSetJobDuedateEmptyJobId() {
    try {
      managementService.setJobDuedate("", new Date());
      fail("ProcessEngineException expected");
    } catch (ProcessEngineException re) {
      assertTextPresent("The job id is mandatory, but '' has been provided.", re.getMessage());
    }
  }

  public void testSetJobDuedateUnexistingJobId() {
    try {
      managementService.setJobDuedate("unexistingjob", new Date());
      fail("ProcessEngineException expected");
    } catch (ProcessEngineException re) {
      assertTextPresent("No job found with id 'unexistingjob'.", re.getMessage());
    }
  }

  public void testGetProperties() {
    Map<String, String> properties = managementService.getProperties();
    assertNotNull(properties);
    assertFalse(properties.isEmpty());
  }

  public void  testSetProperty() {
    final String name = "testProp";
    final String value = "testValue";
    managementService.setProperty(name, value);

    Map<String, String> properties = managementService.getProperties();
    assertTrue(properties.containsKey(name));
    String storedValue = properties.get(name);
    assertEquals(value, storedValue);

    managementService.deleteProperty(name);
  }

  public void  testDeleteProperty() {
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

  public void  testDeleteNonexistingProperty() {

    managementService.deleteProperty("non existing");

  }

  public void testGetHistoryLevel() {
    int historyLevel = managementService.getHistoryLevel();
    assertEquals(processEngineConfiguration.getHistoryLevel().getId(), historyLevel);
  }

  @Deployment(resources = "org/camunda/bpm/engine/test/api/mgmt/asyncTaskProcess.bpmn20.xml")
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

  public void testSetJobPriorityForNonExistingJob() {
    try {
      managementService.setJobPriority("nonExistingJob", 42);
      fail("should not succeed");
    } catch (NotFoundException e) {
      assertTextPresentIgnoreCase("No job found with id 'nonExistingJob'", e.getMessage());
    }
  }

  public void testSetJobPriorityForNullJob() {
    try {
      managementService.setJobPriority(null, 42);
      fail("should not succeed");
    } catch (NullValueException e) {
      assertTextPresentIgnoreCase("Job id must not be null", e.getMessage());
    }
  }

  @Deployment(resources = "org/camunda/bpm/engine/test/api/mgmt/asyncTaskProcess.bpmn20.xml")
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

}
