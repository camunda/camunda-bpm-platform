/**
 *
 */
package org.camunda.bpm.engine.test.jobexecutor;

import java.util.List;

import org.camunda.bpm.engine.history.HistoricIncident;
import org.camunda.bpm.engine.impl.cmd.DeleteJobCmd;
import org.camunda.bpm.engine.impl.db.DbEntity;
import org.camunda.bpm.engine.impl.interceptor.Command;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.persistence.entity.MessageEntity;
import org.camunda.bpm.engine.impl.test.PluggableProcessEngineTestCase;
import org.camunda.bpm.engine.runtime.Job;
import org.camunda.bpm.engine.runtime.JobQuery;

/**
 * @author Tom Baeyens
 */
public class JobExecutorCmdExceptionTest extends PluggableProcessEngineTestCase {

  protected TweetExceptionHandler tweetExceptionHandler = new TweetExceptionHandler();

  public void setUp() throws Exception {
    processEngineConfiguration.getJobHandlers().put(tweetExceptionHandler.getType(), tweetExceptionHandler);
  }

  public void tearDown() throws Exception {
    processEngineConfiguration.getJobHandlers().remove(tweetExceptionHandler.getType());
    clearDatabase();
  }

  public void testJobCommandsWith2Exceptions() {
    // create a job
    createJob();

    // execute the existing job
    executeAvailableJobs();

    // the job was successfully executed
    JobQuery query = managementService.createJobQuery().noRetriesLeft();
    assertEquals(0, query.count());
  }

  public void testJobCommandsWith3Exceptions() {
    // set the execptionsRemaining to 3 so that
    // the created job will fail 3 times and a failed
    // job exists
    tweetExceptionHandler.setExceptionsRemaining(3);

    // create a job
    createJob();

    // execute the existing job
    executeAvailableJobs();

    // the job execution failed (job.retries = 0)
    Job job = managementService.createJobQuery().noRetriesLeft().singleResult();
    assertNotNull(job);
    assertEquals(0, job.getRetries());
  }

  public void testMultipleFailingJobs() {
    // set the execptionsRemaining to 600 so that
    // each created job will fail 3 times and 40 failed
    // job exists
    tweetExceptionHandler.setExceptionsRemaining(600);

    // create 40 jobs
    for(int i = 0; i < 40; i++) {
      createJob();
    }

    // execute the existing jobs
    executeAvailableJobs();

    // now there are 40 jobs with retries = 0:
    List<Job> jobList = managementService.createJobQuery().list();
    assertEquals(40, jobList.size());

    for (Job job : jobList) {
      // all jobs have retries exhausted
      assertEquals(0, job.getRetries());
    }
  }

  protected void createJob() {
    processEngineConfiguration.getCommandExecutorTxRequired().execute(new Command<String>() {

      public String execute(CommandContext commandContext) {
        MessageEntity message = createTweetExceptionMessage();
        commandContext.getJobManager().send(message);
        return message.getId();
      }
    });
  }

  protected MessageEntity createTweetExceptionMessage() {
    MessageEntity message = new MessageEntity();
    message.setJobHandlerType("tweet-exception");
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
        }

        List<HistoricIncident> historicIncidents = processEngineConfiguration
            .getHistoryService()
            .createHistoricIncidentQuery()
            .list();

        for (HistoricIncident historicIncident : historicIncidents) {
          commandContext
            .getDbSqlSession()
            .delete((DbEntity) historicIncident);
        }

        return null;
      }
    });
  }

}
