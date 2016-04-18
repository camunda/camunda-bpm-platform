package org.camunda.bpm.engine.test.standalone.entity;

import org.camunda.bpm.engine.impl.interceptor.Command;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.persistence.entity.JobEntity;
import org.camunda.bpm.engine.impl.persistence.entity.MessageEntity;
import org.camunda.bpm.engine.impl.test.PluggableProcessEngineTestCase;
import org.camunda.bpm.engine.runtime.Job;
import org.camunda.bpm.engine.test.Deployment;

/**
 *
 * @author Clint Manning
 */
public class JobEntityTest extends PluggableProcessEngineTestCase {

  /**
   * Note: This does not test a message with 4-byte Unicode supplementary
   * characters for two reasons:
   * - MySQL 5.1 does not support 4-byte supplementary characters (support from 5.5.3 onwards)
   * - {@link String#length()} counts these characters twice (since they are represented by two
   * chars), so essentially the cutoff would be half the actual cutoff for such a string
   */
  public void testInsertJobWithExceptionMessage() {
    String fittingThreeByteMessage = repeatCharacter("\u9faf", JobEntity.MAX_EXCEPTION_MESSAGE_LENGTH);

    JobEntity threeByteJobEntity = new MessageEntity();
    threeByteJobEntity.setExceptionMessage(fittingThreeByteMessage);

    // should not fail
    insertJob(threeByteJobEntity);

    deleteJob(threeByteJobEntity);
  }

  public void testJobExceptionMessageCutoff() {
    JobEntity threeByteJobEntity = new MessageEntity();

    String message = repeatCharacter("a", JobEntity.MAX_EXCEPTION_MESSAGE_LENGTH * 2);
    threeByteJobEntity.setExceptionMessage(message);
    assertEquals(JobEntity.MAX_EXCEPTION_MESSAGE_LENGTH, threeByteJobEntity.getExceptionMessage().length());
  }

  protected void insertJob(final JobEntity jobEntity) {
    processEngineConfiguration.getCommandExecutorTxRequired().execute(new Command<Void>() {

      @Override
      public Void execute(CommandContext commandContext) {
        commandContext.getJobManager().insert(jobEntity);
        return null;
      }
    });
  }

  protected void deleteJob(final JobEntity jobEntity) {
    processEngineConfiguration.getCommandExecutorTxRequired().execute(new Command<Void>() {

      @Override
      public Void execute(CommandContext commandContext) {
        commandContext.getJobManager().delete(jobEntity);
        return null;
      }
    });
  }

  protected String repeatCharacter(String encodedCharacter, int numCharacters) {
    StringBuilder sb = new StringBuilder();

    for (int i = 0; i < numCharacters; i++) {
      sb.append(encodedCharacter);
    }

    return sb.toString();
  }

  
   @Deployment
  public void testLongProcessDefinitionKey() {
    String key = "myrealrealrealrealrealrealrealrealrealrealreallongprocessdefinitionkeyawesome";
    String processInstanceId = runtimeService.startProcessInstanceByKey(key).getId();

    Job job = managementService.createJobQuery().processInstanceId(processInstanceId).singleResult();

    assertEquals(key, job.getProcessDefinitionKey());
  }

}
