/**
 * 
 */
package org.camunda.bpm.engine.test.jobexecutor;

import org.camunda.bpm.engine.impl.cmd.DeleteJobsCmd;
import org.camunda.bpm.engine.impl.interceptor.Command;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.interceptor.CommandExecutor;
import org.camunda.bpm.engine.impl.persistence.entity.MessageEntity;
import org.camunda.bpm.engine.impl.test.PluggableProcessEngineTestCase;

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

    waitForJobExecutorToProcessAllJobs(15000L, 50L);
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

    waitForJobExecutorToProcessAllJobs(15000L, 50L);

    // TODO check if there is a failed job in the DLQ

    commandExecutor.execute(new DeleteJobsCmd(jobId));
  }

  protected MessageEntity createTweetExceptionMessage() {
    MessageEntity message = new MessageEntity();
    message.setJobHandlerType("tweet-exception");
    return message;
  }
}
