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
package org.camunda.bpm.engine.test.jobexecutor;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.List;

import org.camunda.bpm.engine.history.HistoricIncident;
import org.camunda.bpm.engine.history.HistoricJobLog;
import org.camunda.bpm.engine.impl.cmd.DeleteJobCmd;
import org.camunda.bpm.engine.impl.db.DbEntity;
import org.camunda.bpm.engine.impl.interceptor.Command;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.persistence.entity.MessageEntity;
import org.camunda.bpm.engine.runtime.Job;
import org.camunda.bpm.engine.runtime.JobQuery;
import org.camunda.bpm.engine.test.Deployment;
import org.camunda.bpm.engine.test.util.PluggableProcessEngineTest;
import org.camunda.bpm.model.bpmn.Bpmn;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Tom Baeyens
 * @author Thorben Lindhauer
 */
public class JobExecutorCmdExceptionTest extends PluggableProcessEngineTest {

  protected TweetExceptionHandler tweetExceptionHandler = new TweetExceptionHandler();
  protected TweetNestedCommandExceptionHandler nestedCommandExceptionHandler = new TweetNestedCommandExceptionHandler();

  @Before
  public void setUp() throws Exception {
    processEngineConfiguration.getJobHandlers().put(tweetExceptionHandler.getType(), tweetExceptionHandler);
    processEngineConfiguration.getJobHandlers().put(nestedCommandExceptionHandler.getType(), nestedCommandExceptionHandler);
  }

  @After
  public void tearDown() throws Exception {
    processEngineConfiguration.getJobHandlers().remove(tweetExceptionHandler.getType());
    processEngineConfiguration.getJobHandlers().remove(nestedCommandExceptionHandler.getType());
    clearDatabase();
  }

  @Test
  public void testJobCommandsWith2Exceptions() {
    // create a job
    createJob(TweetExceptionHandler.TYPE);

    // execute the existing job
    testRule.executeAvailableJobs();

    // the job was successfully executed
    JobQuery query = managementService.createJobQuery().noRetriesLeft();
    assertEquals(0, query.count());
  }

  @Test
  public void testJobCommandsWith3Exceptions() {
    // set the execptionsRemaining to 3 so that
    // the created job will fail 3 times and a failed
    // job exists
    tweetExceptionHandler.setExceptionsRemaining(3);

    // create a job
    createJob(TweetExceptionHandler.TYPE);

    // execute the existing job
    testRule.executeAvailableJobs();

    // the job execution failed (job.retries = 0)
    Job job = managementService.createJobQuery().noRetriesLeft().singleResult();
    assertNotNull(job);
    assertEquals(0, job.getRetries());
  }

  @Test
  public void testMultipleFailingJobs() {
    // set the execptionsRemaining to 600 so that
    // each created job will fail 3 times and 40 failed
    // job exists
    tweetExceptionHandler.setExceptionsRemaining(600);

    // create 40 jobs
    for(int i = 0; i < 40; i++) {
      createJob(TweetExceptionHandler.TYPE);
    }

    // execute the existing jobs
    testRule.executeAvailableJobs();

    // now there are 40 jobs with retries = 0:
    List<Job> jobList = managementService.createJobQuery().list();
    assertEquals(40, jobList.size());

    for (Job job : jobList) {
      // all jobs have retries exhausted
      assertEquals(0, job.getRetries());
    }
  }

  @Test
  public void testJobCommandsWithNestedFailingCommand() {
    // create a job
    createJob(TweetNestedCommandExceptionHandler.TYPE);

    // execute the existing job
    Job job = managementService.createJobQuery().singleResult();

    assertEquals(3, job.getRetries());

    try {
      managementService.executeJob(job.getId());
      fail("Exception expected");
    } catch (Exception e) {
      // expected
    }

    job = managementService.createJobQuery().singleResult();
    assertEquals(2, job.getRetries());

    testRule.executeAvailableJobs();

    // the job execution failed (job.retries = 0)
    job = managementService.createJobQuery().noRetriesLeft().singleResult();
    assertNotNull(job);
    assertEquals(0, job.getRetries());
  }

  @Deployment(resources="org/camunda/bpm/engine/test/jobexecutor/jobFailingOnFlush.bpmn20.xml")
  @Test
  public void testJobRetriesDecrementedOnFailedFlush() {

    runtimeService.startProcessInstanceByKey("testProcess");

    // there should be 1 job created:
    Job job = managementService.createJobQuery().singleResult();
    assertNotNull(job);
    // with 3 retries
    assertEquals(3, job.getRetries());

    // if we execute the job
    testRule.waitForJobExecutorToProcessAllJobs(6000);

    // the job is still present
    job = managementService.createJobQuery().singleResult();
    assertNotNull(job);
    // but has no more retires
    assertEquals(0, job.getRetries());
  }

  @Test
  public void testFailingTransactionListener() {

   testRule.deploy(Bpmn.createExecutableProcess("testProcess")
        .startEvent()
        .serviceTask()
          .camundaClass(FailingTransactionListenerDelegate.class.getName())
          .camundaAsyncBefore()
        .endEvent()
        .done());

    runtimeService.startProcessInstanceByKey("testProcess");

    // there should be 1 job created:
    Job job = managementService.createJobQuery().singleResult();
    assertNotNull(job);
    // with 3 retries
    assertEquals(3, job.getRetries());

    // if we execute the job
    testRule.waitForJobExecutorToProcessAllJobs(6000);

    // the job is still present
    job = managementService.createJobQuery().singleResult();
    assertNotNull(job);
    // but has no more retires
    assertEquals(0, job.getRetries());
    assertEquals("exception in transaction listener", job.getExceptionMessage());

    String stacktrace = managementService.getJobExceptionStacktrace(job.getId());
    assertNotNull(stacktrace);
    assertTrue("unexpected stacktrace, was <" + stacktrace + ">", stacktrace.contains("java.lang.RuntimeException: exception in transaction listener"));
  }

  protected void createJob(final String handlerType) {
    processEngineConfiguration.getCommandExecutorTxRequired().execute(new Command<String>() {

      public String execute(CommandContext commandContext) {
        MessageEntity message = createMessage(handlerType);
        commandContext.getJobManager().send(message);
        return message.getId();
      }
    });
  }

  protected MessageEntity createMessage(String handlerType) {
    MessageEntity message = new MessageEntity();
    message.setJobHandlerType(handlerType);
    return message;
  }

  protected void clearDatabase() {
    processEngineConfiguration.getCommandExecutorTxRequired().execute(new Command<Void>() {
      public Void execute(CommandContext commandContext) {

        List<Job> jobs = processEngineConfiguration
            .getManagementService()
            .createJobQuery()
            .list();

        for (Job job : jobs) {
          new DeleteJobCmd(job.getId()).execute(commandContext);
          commandContext.getHistoricJobLogManager().deleteHistoricJobLogByJobId(job.getId());
        }

        List<HistoricIncident> historicIncidents = processEngineConfiguration
            .getHistoryService()
            .createHistoricIncidentQuery()
            .list();

        for (HistoricIncident historicIncident : historicIncidents) {
          commandContext
            .getDbEntityManager()
            .delete((DbEntity) historicIncident);
        }

        List<HistoricJobLog> historicJobLogs = processEngineConfiguration
            .getHistoryService()
            .createHistoricJobLogQuery()
            .list();

        for (HistoricJobLog historicJobLog : historicJobLogs) {
          commandContext
            .getHistoricJobLogManager()
            .deleteHistoricJobLogById(historicJobLog.getId());
        }

        return null;
      }
    });
  }

}
