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

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.history.HistoricIncident;
import org.camunda.bpm.engine.impl.cmd.DeleteJobsCmd;
import org.camunda.bpm.engine.impl.context.Context;
import org.camunda.bpm.engine.impl.db.DbEntity;
import org.camunda.bpm.engine.impl.interceptor.Command;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.interceptor.CommandExecutor;
import org.camunda.bpm.engine.impl.persistence.entity.JobEntity;
import org.camunda.bpm.engine.impl.persistence.entity.JobManager;
import org.camunda.bpm.engine.impl.persistence.entity.MessageEntity;
import org.camunda.bpm.engine.impl.persistence.entity.TimerEntity;
import org.camunda.bpm.engine.impl.test.PluggableProcessEngineTestCase;
import org.camunda.bpm.engine.impl.test.TestHelper;
import org.camunda.bpm.engine.impl.util.ClockUtil;
import org.camunda.bpm.engine.management.JobDefinition;
import org.camunda.bpm.engine.repository.ProcessDefinition;
import org.camunda.bpm.engine.runtime.Job;
import org.camunda.bpm.engine.runtime.JobQuery;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.test.Deployment;


/**
 * @author Joram Barrez
 * @author Falko Menge
 */
public class JobQueryTest extends PluggableProcessEngineTestCase {

  private String deploymentId;
  private String messageId;
  private CommandExecutor commandExecutor;
  private TimerEntity timerEntity;

  private Date testStartTime;
  private Date timerOneFireTime;
  private Date timerTwoFireTime;
  private Date timerThreeFireTime;

  private String processInstanceIdOne;
  private String processInstanceIdTwo;
  private String processInstanceIdThree;

  private static final long ONE_HOUR = 60L * 60L * 1000L;
  private static final long ONE_SECOND = 1000L;
  private static final String EXCEPTION_MESSAGE = "java.lang.RuntimeException: This is an exception thrown from scriptTask";


  /**
   * Setup will create
   *   - 3 process instances, each with one timer, each firing at t1/t2/t3 + 1 hour (see process)
   *   - 1 message
   */
  protected void setUp() throws Exception {
    super.setUp();

    this.commandExecutor = processEngineConfiguration.getCommandExecutorTxRequired();

    deploymentId = repositoryService.createDeployment()
        .addClasspathResource("org/camunda/bpm/engine/test/api/mgmt/timerOnTask.bpmn20.xml")
        .deploy()
        .getId();

    // Create proc inst that has timer that will fire on t1 + 1 hour
    Calendar startTime = Calendar.getInstance();
    startTime.set(Calendar.MILLISECOND, 0);

    Date t1 = startTime.getTime();
    ClockUtil.setCurrentTime(t1);
    processInstanceIdOne = runtimeService.startProcessInstanceByKey("timerOnTask").getId();
    testStartTime = t1;
    timerOneFireTime = new Date(t1.getTime() + ONE_HOUR);

    // Create proc inst that has timer that will fire on t2 + 1 hour
    startTime.add(Calendar.HOUR_OF_DAY, 1);
    Date t2 = startTime.getTime();  // t2 = t1 + 1 hour
    ClockUtil.setCurrentTime(t2);
    processInstanceIdTwo = runtimeService.startProcessInstanceByKey("timerOnTask").getId();
    timerTwoFireTime = new Date(t2.getTime() + ONE_HOUR);

    // Create proc inst that has timer that will fire on t3 + 1 hour
    startTime.add(Calendar.HOUR_OF_DAY, 1);
    Date t3 = startTime.getTime(); // t3 = t2 + 1 hour
    ClockUtil.setCurrentTime(t3);
    processInstanceIdThree = runtimeService.startProcessInstanceByKey("timerOnTask").getId();
    timerThreeFireTime = new Date(t3.getTime() + ONE_HOUR);

    // Create one message
    messageId = commandExecutor.execute(new Command<String>() {
      public String execute(CommandContext commandContext) {
        MessageEntity message = new MessageEntity();
        commandContext.getJobManager().send(message);
        return message.getId();
      }
    });
  }

  @Override
  protected void tearDown() throws Exception {
    repositoryService.deleteDeployment(deploymentId, true);
    commandExecutor.execute(new DeleteJobsCmd(messageId, true));
    super.tearDown();
  }

  public void testQueryByNoCriteria() {
    JobQuery query = managementService.createJobQuery();
    verifyQueryResults(query, 4);
  }

  public void testQueryByActivityId(){
    JobDefinition jobDefinition = managementService.createJobDefinitionQuery().singleResult();

    JobQuery query = managementService.createJobQuery().activityId(jobDefinition.getActivityId());
    verifyQueryResults(query, 3);
  }

  public void testQueryByInvalidActivityId(){
    JobQuery query = managementService.createJobQuery().activityId("invalid");
    verifyQueryResults(query, 0);

    try {
      managementService.createJobQuery().activityId(null).list();
      fail();
    } catch (ProcessEngineException e) {}
  }

  public void testByJobDefinitionId() {
    JobDefinition jobDefinition = managementService.createJobDefinitionQuery().singleResult();

    JobQuery query = managementService.createJobQuery().jobDefinitionId(jobDefinition.getId());
    verifyQueryResults(query, 3);
  }

  public void testByInvalidJobDefinitionId() {
    JobQuery query = managementService.createJobQuery().jobDefinitionId("invalid");
    verifyQueryResults(query, 0);

    try {
      managementService.createJobQuery().jobDefinitionId(null).list();
      fail();
    } catch (ProcessEngineException e) {}
  }

  public void testQueryByProcessInstanceId() {
    JobQuery query = managementService.createJobQuery().processInstanceId(processInstanceIdOne);
    verifyQueryResults(query, 1);
  }

  public void testQueryByInvalidProcessInstanceId() {
    JobQuery query = managementService.createJobQuery().processInstanceId("invalid");
    verifyQueryResults(query, 0);

    try {
      managementService.createJobQuery().processInstanceId(null);
      fail();
    } catch (ProcessEngineException e) {}
  }

  public void testQueryByExecutionId() {
    Job job = managementService.createJobQuery().processInstanceId(processInstanceIdOne).singleResult();
    JobQuery query = managementService.createJobQuery().executionId(job.getExecutionId());
    assertEquals(query.singleResult().getId(), job.getId());
    verifyQueryResults(query, 1);
  }

  public void testQueryByInvalidExecutionId() {
    JobQuery query = managementService.createJobQuery().executionId("invalid");
    verifyQueryResults(query, 0);

    try {
      managementService.createJobQuery().executionId(null).list();
      fail();
    } catch (ProcessEngineException e) {}
  }

  public void testQueryByProcessDefinitionId() {
    ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery().list().get(0);

    JobQuery query = managementService.createJobQuery().processDefinitionId(processDefinition.getId());
    verifyQueryResults(query, 3);
  }

  public void testQueryByInvalidProcessDefinitionId() {
    JobQuery query = managementService.createJobQuery().processDefinitionId("invalid");
    verifyQueryResults(query, 0);

    try {
      managementService.createJobQuery().processDefinitionId(null).list();
      fail();
    } catch (ProcessEngineException e) {}
  }

  @Deployment
  public void testTimeCycleQueryByProcessDefinitionId() {
    String processDefinitionId = repositoryService
        .createProcessDefinitionQuery()
        .processDefinitionKey("process")
        .singleResult()
        .getId();

    JobQuery query = managementService.createJobQuery().processDefinitionId(processDefinitionId);

    verifyQueryResults(query, 1);

    String jobId = query.singleResult().getId();
    managementService.executeJob(jobId);

    verifyQueryResults(query, 1);

    String anotherJobId = query.singleResult().getId();
    assertFalse(jobId.equals(anotherJobId));
  }

  public void testQueryByProcessDefinitionKey() {
    JobQuery query = managementService.createJobQuery().processDefinitionKey("timerOnTask");
    verifyQueryResults(query, 3);
  }

  public void testQueryByInvalidProcessDefinitionKey() {
    JobQuery query = managementService.createJobQuery().processDefinitionKey("invalid");
    verifyQueryResults(query, 0);

    try {
      managementService.createJobQuery().processDefinitionKey(null).list();
      fail();
    } catch (ProcessEngineException e) {}
  }

  @Deployment(resources = {"org/camunda/bpm/engine/test/api/mgmt/JobQueryTest.testTimeCycleQueryByProcessDefinitionId.bpmn20.xml"})
  public void testTimeCycleQueryByProcessDefinitionKey() {
    JobQuery query = managementService.createJobQuery().processDefinitionKey("process");

    verifyQueryResults(query, 1);

    String jobId = query.singleResult().getId();
    managementService.executeJob(jobId);

    verifyQueryResults(query, 1);

    String anotherJobId = query.singleResult().getId();
    assertFalse(jobId.equals(anotherJobId));
  }

  public void testQueryByRetriesLeft() {
    JobQuery query = managementService.createJobQuery().withRetriesLeft();
    verifyQueryResults(query, 4);

    setRetries(processInstanceIdOne, 0);
    // Re-running the query should give only 3 jobs now, since one job has retries=0
    verifyQueryResults(query, 3);
  }

  public void testQueryByExecutable() {
    ClockUtil.setCurrentTime(new Date(timerThreeFireTime.getTime() + ONE_SECOND)); // all jobs should be executable at t3 + 1hour.1second
    JobQuery query = managementService.createJobQuery().executable();
    verifyQueryResults(query, 4);

    // Setting retries of one job to 0, makes it non-executable
    setRetries(processInstanceIdOne, 0);
    verifyQueryResults(query, 3);

    // Setting the clock before the start of the process instance, makes none of the jobs executable
    ClockUtil.setCurrentTime(testStartTime);
    verifyQueryResults(query, 1); // 1, since a message is always executable when retries > 0
  }

  public void testQueryByOnlyTimers() {
    JobQuery query = managementService.createJobQuery().timers();
    verifyQueryResults(query, 3);
  }

  public void testQueryByOnlyMessages() {
    JobQuery query = managementService.createJobQuery().messages();
    verifyQueryResults(query, 1);
  }

  public void testInvalidOnlyTimersUsage() {
    try {
      managementService.createJobQuery().timers().messages().list();
      fail();
    } catch (ProcessEngineException e) {
      assertTextPresent("Cannot combine onlyTimers() with onlyMessages() in the same query", e.getMessage());
    }
  }

  public void testQueryByDuedateLowerThen() {
    JobQuery query = managementService.createJobQuery().duedateLowerThen(testStartTime);
    verifyQueryResults(query, 0);

    query = managementService.createJobQuery().duedateLowerThen(new Date(timerOneFireTime.getTime() + ONE_SECOND));
    verifyQueryResults(query, 1);

    query = managementService.createJobQuery().duedateLowerThen(new Date(timerTwoFireTime.getTime() + ONE_SECOND));
    verifyQueryResults(query, 2);

    query = managementService.createJobQuery().duedateLowerThen(new Date(timerThreeFireTime.getTime() + ONE_SECOND));
    verifyQueryResults(query, 3);
  }

  public void testQueryByDuedateLowerThenOrEqual() {
    JobQuery query = managementService.createJobQuery().duedateLowerThenOrEquals(testStartTime);
    verifyQueryResults(query, 0);

    query = managementService.createJobQuery().duedateLowerThenOrEquals(timerOneFireTime);
    verifyQueryResults(query, 1);

    query = managementService.createJobQuery().duedateLowerThenOrEquals(timerTwoFireTime);
    verifyQueryResults(query, 2);

    query = managementService.createJobQuery().duedateLowerThenOrEquals(timerThreeFireTime);
    verifyQueryResults(query, 3);
  }

  public void testQueryByDuedateHigherThen() {
    JobQuery query = managementService.createJobQuery().duedateHigherThen(testStartTime);
    verifyQueryResults(query, 3);

    query = managementService.createJobQuery().duedateHigherThen(timerOneFireTime);
    verifyQueryResults(query, 2);

    query = managementService.createJobQuery().duedateHigherThen(timerTwoFireTime);
    verifyQueryResults(query, 1);

    query = managementService.createJobQuery().duedateHigherThen(timerThreeFireTime);
    verifyQueryResults(query, 0);
  }

  public void testQueryByDuedateHigherThenOrEqual() {
    JobQuery query = managementService.createJobQuery().duedateHigherThenOrEquals(testStartTime);
    verifyQueryResults(query, 3);

    query = managementService.createJobQuery().duedateHigherThenOrEquals(timerOneFireTime);
    verifyQueryResults(query, 3);

    query = managementService.createJobQuery().duedateHigherThenOrEquals(new Date(timerOneFireTime.getTime() + ONE_SECOND));
    verifyQueryResults(query, 2);

    query = managementService.createJobQuery().duedateHigherThenOrEquals(timerThreeFireTime);
    verifyQueryResults(query, 1);

    query = managementService.createJobQuery().duedateHigherThenOrEquals(new Date(timerThreeFireTime.getTime() + ONE_SECOND));
    verifyQueryResults(query, 0);
  }

  public void testQueryByDuedateCombinations() {
    JobQuery query = managementService.createJobQuery()
        .duedateHigherThan(testStartTime)
        .duedateLowerThan(new Date(timerThreeFireTime.getTime() + ONE_SECOND));
    verifyQueryResults(query, 3);

    query = managementService.createJobQuery()
        .duedateHigherThan(new Date(timerThreeFireTime.getTime() + ONE_SECOND))
        .duedateLowerThan(testStartTime);
    verifyQueryResults(query, 0);
  }

  @Deployment(resources = {"org/camunda/bpm/engine/test/api/mgmt/ManagementServiceTest.testGetJobExceptionStacktrace.bpmn20.xml"})
  public void testQueryByException() {
    JobQuery query = managementService.createJobQuery().withException();
    verifyQueryResults(query, 0);

    ProcessInstance processInstance = startProcessInstanceWithFailingJob();

    query = managementService.createJobQuery().withException();
    verifyFailedJob(query, processInstance);
  }

  @Deployment(resources = {"org/camunda/bpm/engine/test/api/mgmt/ManagementServiceTest.testGetJobExceptionStacktrace.bpmn20.xml"})
  public void testQueryByExceptionMessage() {
    JobQuery query = managementService.createJobQuery().exceptionMessage(EXCEPTION_MESSAGE);
    verifyQueryResults(query, 0);

    ProcessInstance processInstance = startProcessInstanceWithFailingJob();

    Job job = managementService.createJobQuery().processInstanceId(processInstance.getId()).singleResult();

    query = managementService.createJobQuery().exceptionMessage(job.getExceptionMessage());
    verifyFailedJob(query, processInstance);
  }

  @Deployment(resources = {"org/camunda/bpm/engine/test/api/mgmt/ManagementServiceTest.testGetJobExceptionStacktrace.bpmn20.xml"})
  public void testQueryByExceptionMessageEmpty() {
    JobQuery query = managementService.createJobQuery().exceptionMessage("");
    verifyQueryResults(query, 0);

    startProcessInstanceWithFailingJob();

    query = managementService.createJobQuery().exceptionMessage("");
    verifyQueryResults(query, 0);
  }

  public void testQueryByExceptionMessageNull() {
    try {
      managementService.createJobQuery().exceptionMessage(null);
      fail("ProcessEngineException expected");
    } catch (ProcessEngineException e) {
      assertEquals("Provided exception message is null", e.getMessage());
    }
  }

  public void testJobQueryWithExceptions() throws Throwable {

    createJobWithoutExceptionMsg();

    Job job = managementService.createJobQuery().jobId(timerEntity.getId()).singleResult();

    assertNotNull(job);

    List<Job> list = managementService.createJobQuery().withException().list();
    assertEquals(list.size(), 1);

    deleteJobInDatabase();

    createJobWithoutExceptionStacktrace();

    job = managementService.createJobQuery().jobId(timerEntity.getId()).singleResult();

    assertNotNull(job);

    list = managementService.createJobQuery().withException().list();
    assertEquals(list.size(), 1);

    deleteJobInDatabase();

  }

  public void testQueryByNoRetriesLeft() {
    JobQuery query = managementService.createJobQuery().noRetriesLeft();
    verifyQueryResults(query, 0);

    setRetries(processInstanceIdOne, 0);
    // Re-running the query should give only one jobs now, since three job has retries>0
    verifyQueryResults(query, 1);
  }

  public void testQueryByActive() {
    JobQuery query = managementService.createJobQuery().active();
    verifyQueryResults(query, 4);
  }

  public void testQueryBySuspended() {
    JobQuery query = managementService.createJobQuery().suspended();
    verifyQueryResults(query, 0);

    managementService.suspendJobDefinitionByProcessDefinitionKey("timerOnTask", true);
    verifyQueryResults(query, 3);
  }

  //sorting //////////////////////////////////////////

  public void testQuerySorting() {
    // asc
    assertEquals(4, managementService.createJobQuery().orderByJobId().asc().count());
    assertEquals(4, managementService.createJobQuery().orderByJobDuedate().asc().count());
    assertEquals(4, managementService.createJobQuery().orderByExecutionId().asc().count());
    assertEquals(4, managementService.createJobQuery().orderByProcessInstanceId().asc().count());
    assertEquals(4, managementService.createJobQuery().orderByJobRetries().asc().count());
    assertEquals(4, managementService.createJobQuery().orderByProcessDefinitionId().asc().count());
    assertEquals(4, managementService.createJobQuery().orderByProcessDefinitionKey().asc().count());

    // desc
    assertEquals(4, managementService.createJobQuery().orderByJobId().desc().count());
    assertEquals(4, managementService.createJobQuery().orderByJobDuedate().desc().count());
    assertEquals(4, managementService.createJobQuery().orderByExecutionId().desc().count());
    assertEquals(4, managementService.createJobQuery().orderByProcessInstanceId().desc().count());
    assertEquals(4, managementService.createJobQuery().orderByJobRetries().desc().count());
    assertEquals(4, managementService.createJobQuery().orderByProcessDefinitionId().desc().count());
    assertEquals(4, managementService.createJobQuery().orderByProcessDefinitionKey().desc().count());

    // sorting on multiple fields
    setRetries(processInstanceIdTwo, 2);
    ClockUtil.setCurrentTime(new Date(timerThreeFireTime.getTime() + ONE_SECOND)); // make sure all timers can fire

    JobQuery query = managementService.createJobQuery()
      .timers()
      .executable()
      .orderByJobRetries()
      .asc()
      .orderByJobDuedate()
      .desc();

    List<Job> jobs = query.list();
    assertEquals(3, jobs.size());

    assertEquals(2, jobs.get(0).getRetries());
    assertEquals(3, jobs.get(1).getRetries());
    assertEquals(3, jobs.get(2).getRetries());

    assertEquals(processInstanceIdTwo, jobs.get(0).getProcessInstanceId());
    assertEquals(processInstanceIdThree, jobs.get(1).getProcessInstanceId());
    assertEquals(processInstanceIdOne, jobs.get(2).getProcessInstanceId());
  }

  public void testQueryInvalidSortingUsage() {
    try {
      managementService.createJobQuery().orderByJobId().list();
      fail();
    } catch (ProcessEngineException e) {
      assertTextPresent("call asc() or desc() after using orderByXX()", e.getMessage());
    }

    try {
      managementService.createJobQuery().asc();
      fail();
    } catch (ProcessEngineException e) {
      assertTextPresent("You should call any of the orderBy methods first before specifying a direction", e.getMessage());
    }
  }

  //helper ////////////////////////////////////////////////////////////

  private void setRetries(final String processInstanceId, final int retries) {
    final Job job = managementService.createJobQuery().processInstanceId(processInstanceId).singleResult();
    commandExecutor.execute(new Command<Void>() {

      public Void execute(CommandContext commandContext) {
        JobEntity timer = commandContext.getDbEntityManager().selectById(JobEntity.class, job.getId());
        timer.setRetries(retries);
        return null;
      }

    });
  }

  private ProcessInstance startProcessInstanceWithFailingJob() {
    // start a process with a failing job
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("exceptionInJobExecution");

    // The execution is waiting in the first usertask. This contains a boundary
    // timer event which we will execute manual for testing purposes.
    Job timerJob = managementService.createJobQuery()
      .processInstanceId(processInstance.getId())
      .singleResult();

    assertNotNull("No job found for process instance", timerJob);

    try {
      managementService.executeJob(timerJob.getId());
      fail("RuntimeException from within the script task expected");
    } catch(RuntimeException re) {
      assertTextPresent(EXCEPTION_MESSAGE, re.getMessage());
    }
    return processInstance;
  }

  private void verifyFailedJob(JobQuery query, ProcessInstance processInstance) {
    verifyQueryResults(query, 1);

    Job failedJob = query.singleResult();
    assertNotNull(failedJob);
    assertEquals(processInstance.getId(), failedJob.getProcessInstanceId());
    assertNotNull(failedJob.getExceptionMessage());
    assertTextPresent(EXCEPTION_MESSAGE, failedJob.getExceptionMessage());
  }

  private void verifyQueryResults(JobQuery query, int countExpected) {
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

  private void verifySingleResultFails(JobQuery query) {
    try {
      query.singleResult();
      fail();
    } catch (ProcessEngineException e) {}
  }

  private void createJobWithoutExceptionMsg() {
    CommandExecutor commandExecutor = processEngineConfiguration.getCommandExecutorTxRequired();
    commandExecutor.execute(new Command<Void>() {
      public Void execute(CommandContext commandContext) {
        JobManager jobManager = commandContext.getJobManager();

        timerEntity = new TimerEntity();
        timerEntity.setLockOwner(UUID.randomUUID().toString());
        timerEntity.setDuedate(new Date());
        timerEntity.setRetries(0);

        StringWriter stringWriter = new StringWriter();
        NullPointerException exception = new NullPointerException();
        exception.printStackTrace(new PrintWriter(stringWriter));
        timerEntity.setExceptionStacktrace(stringWriter.toString());

        jobManager.insert(timerEntity);

        assertNotNull(timerEntity.getId());

        return null;

      }
    });

  }

  private void createJobWithoutExceptionStacktrace() {
    CommandExecutor commandExecutor = processEngineConfiguration.getCommandExecutorTxRequired();
    commandExecutor.execute(new Command<Void>() {
      public Void execute(CommandContext commandContext) {
        JobManager jobManager = commandContext.getJobManager();

        timerEntity = new TimerEntity();
        timerEntity.setLockOwner(UUID.randomUUID().toString());
        timerEntity.setDuedate(new Date());
        timerEntity.setRetries(0);
        timerEntity.setExceptionMessage("I'm supposed to fail");

        jobManager.insert(timerEntity);

        assertNotNull(timerEntity.getId());

        return null;

      }
    });

  }

  private void deleteJobInDatabase() {
      CommandExecutor commandExecutor = processEngineConfiguration.getCommandExecutorTxRequired();
      commandExecutor.execute(new Command<Void>() {
        public Void execute(CommandContext commandContext) {

          timerEntity.delete();

          commandContext.getHistoricJobLogManager().deleteHistoricJobLogByJobId(timerEntity.getId());

          List<HistoricIncident> historicIncidents = Context
              .getProcessEngineConfiguration()
              .getHistoryService()
              .createHistoricIncidentQuery()
              .list();

          for (HistoricIncident historicIncident : historicIncidents) {
            commandContext
              .getDbEntityManager()
              .delete((DbEntity) historicIncident);
          }

          return null;
        }
      });
  }

}
