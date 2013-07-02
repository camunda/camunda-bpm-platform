/**
 * 
 */
package org.camunda.bpm.engine.test.jobexecutor;

import java.util.ArrayList;
import java.util.List;

import org.camunda.bpm.engine.impl.cmd.DeleteJobsCmd;
import org.camunda.bpm.engine.impl.interceptor.Command;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.interceptor.CommandExecutor;
import org.camunda.bpm.engine.impl.persistence.entity.MessageEntity;
import org.camunda.bpm.engine.impl.test.PluggableProcessEngineTestCase;
import org.camunda.bpm.engine.runtime.Job;

/**
 * @author Tom Baeyens
 */
public class JobExecutorCmdExceptionTest extends PluggableProcessEngineTestCase {

  protected TweetExceptionHandler tweetExceptionHandler = new TweetExceptionHandler();

  private CommandExecutor commandExecutor;

  public void setUp() throws Exception {
    processEngineConfiguration.getJobHandlers().put(tweetExceptionHandler.getType(), tweetExceptionHandler);
    this.commandExecutor = processEngineConfiguration.getCommandExecutorTxRequired();
  }

  public void tearDown() throws Exception {
    processEngineConfiguration.getJobHandlers().remove(tweetExceptionHandler.getType());
  }

  public void testJobCommandsWith2Exceptions() {
    commandExecutor.execute(new Command<String>() {

      public String execute(CommandContext commandContext) {
        MessageEntity message = createTweetExceptionMessage();
        commandContext.getJobManager().send(message);
        return message.getId();
      }
    });

    waitForJobExecutorToProcessAllJobs(15000L);
  }

  public void testJobCommandsWith3Exceptions() {
    tweetExceptionHandler.setExceptionsRemaining(3);

    String jobId = commandExecutor.execute(new Command<String>() {

      public String execute(CommandContext commandContext) {
        MessageEntity message = createTweetExceptionMessage();
        commandContext.getJobManager().send(message);
        return message.getId();
      }
    });

    waitForJobExecutorToProcessAllJobs(15000L);

    // TODO check if there is a failed job in the DLQ

    commandExecutor.execute(new DeleteJobsCmd(jobId));
  }
  
  public void testMultipleFailingJobs() {
    tweetExceptionHandler.setExceptionsRemaining(600);
    
    List<String> createdJobs = new ArrayList<String>();

    // create 40 jobs
    for(int i = 0; i < 40; i++) {
      createdJobs.add(commandExecutor.execute(new Command<String>() {
  
        public String execute(CommandContext commandContext) {
          MessageEntity message = createTweetExceptionMessage();
          commandContext.getJobManager().send(message);
          return message.getId();
        }
      }));
    }

    waitForJobExecutorToProcessAllJobs(15000L);
    
    // now there are 40 jobs with retries = 0:
    List<Job> jobList = managementService.createJobQuery().list();    
    assertEquals(40, jobList.size());
    
    for (Job job : jobList) {
      // all jobs have retries exhausted
      assertEquals(0, job.getRetries());      
    }

    for (String jobId : createdJobs) {
      commandExecutor.execute(new DeleteJobsCmd(jobId));            
    }
    
  }

  protected MessageEntity createTweetExceptionMessage() {
    MessageEntity message = new MessageEntity();
    message.setJobHandlerType("tweet-exception");
    return message;
  }
}
