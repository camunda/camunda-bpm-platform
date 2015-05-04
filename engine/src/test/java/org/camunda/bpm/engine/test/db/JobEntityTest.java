package org.camunda.bpm.engine.test.db;

import java.util.List;

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

  @Deployment(resources={"org/camunda/bpm/engine/test/db/processWithGatewayAndTwoEndEvents.bpmn20.xml"})
  public void testGatewayWithTwoEndEventsLastJobReAssignedToParentExe() {
    String processKey = repositoryService.createProcessDefinitionQuery().singleResult().getKey();
    String processInstanceId = runtimeService.startProcessInstanceByKey(processKey).getId();

    List<Job> jobList = managementService.createJobQuery().processInstanceId(processInstanceId).list();

    // There should be two jobs
    assertNotNull(jobList);
    assertEquals(2, jobList.size());

    managementService.executeJob(jobList.get(0).getId());

    // There should be only one job left
    jobList = managementService.createJobQuery().list();
    assertEquals(1, jobList.size());

    // There should only be 1 execution left - the root execution
    assertEquals(1, runtimeService.createExecutionQuery().list().size());

    // root execution should be attached to the last job
    assertEquals(processInstanceId, jobList.get(0).getExecutionId());

    managementService.executeJob(jobList.get(0).getId());

    // There should be no more jobs
    jobList = managementService.createJobQuery().list();
    assertEquals(0, jobList.size());
  }

  @Deployment(resources={"org/camunda/bpm/engine/test/db/processGatewayAndTwoEndEventsPlusTimer.bpmn20.xml"})
  public void testGatewayWithTwoEndEventsLastTimerReAssignedToParentExe() {
    String processKey = repositoryService.createProcessDefinitionQuery().singleResult().getKey();
    String processInstanceId = runtimeService.startProcessInstanceByKey(processKey).getId();

    List<Job> jobList = managementService.createJobQuery().processInstanceId(processInstanceId).list();

    // There should be two jobs
    assertNotNull(jobList);
    assertEquals(2, jobList.size());

    // execute timer first
    String timerId = managementService.createJobQuery().timers().singleResult().getId();
    managementService.executeJob(timerId);

    // There should be only one job left
    jobList = managementService.createJobQuery().list();
    assertEquals(1, jobList.size());

    // There should only be 1 execution left - the root execution
    assertEquals(1, runtimeService.createExecutionQuery().list().size());

    // root execution should be attached to the last job
    assertEquals(processInstanceId, jobList.get(0).getExecutionId());

    // execute service task
    managementService.executeJob(jobList.get(0).getId());

    // There should be no more jobs
    jobList = managementService.createJobQuery().list();
    assertEquals(0, jobList.size());
  }

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

  @Deployment
  public void FAILING_testLongProcessDefinitionKey() {
    String key = "myrealrealrealrealrealrealrealrealrealrealreallongprocessdefinitionkeyawesome";
    String processInstanceId = runtimeService.startProcessInstanceByKey(key).getId();

    Job job = managementService.createJobQuery().processInstanceId(processInstanceId).singleResult();

    assertEquals(key, job.getProcessDefinitionKey());
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

}
