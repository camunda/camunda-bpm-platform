package org.camunda.bpm.engine.test.history.useroperationlog;

import org.camunda.bpm.engine.EntityTypes;
import org.camunda.bpm.engine.history.UserOperationLogEntry;
import org.camunda.bpm.engine.history.UserOperationLogQuery;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.task.Attachment;
import org.camunda.bpm.engine.test.Deployment;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

/**
 * @author Askar Akhmerov
 */
public class UserOperationLogProcessInstanceTest extends AbstractUserOperationLogTest {
  protected ProcessInstance process;

  @Deployment(resources={
      "org/camunda/bpm/engine/test/api/oneTaskProcess.bpmn20.xml"})
  public void testCreateAttachment () {
    process = runtimeService.startProcessInstanceByKey("oneTaskProcess");

    Attachment result = runtimeService.createAttachment("testType", process.getId(), "testName", "aDescription", "aUrl");

    UserOperationLogQuery query = historyService
        .createUserOperationLogQuery()
        .processInstanceId(process.getId());
    assertThat(query.count(),is(1L));
    UserOperationLogEntry userOperationLogEntry = query.singleResult();
    assertThat(userOperationLogEntry.getEntityType(),is(EntityTypes.ATTACHMENT));
    assertThat(userOperationLogEntry.getProcessInstanceId(),is(process.getId()));
    assertThat(userOperationLogEntry.getProcessDefinitionId(),is(process.getProcessDefinitionId()));
    assertThat(userOperationLogEntry.getExecutionId(),is(
        runtimeService.createExecutionQuery().processInstanceId(process.getId()).singleResult().getId()));
    assertThat(userOperationLogEntry.getProcessDefinitionKey(),is(
        repositoryService.createProcessDefinitionQuery().singleResult().getKey()));
    assertThat(userOperationLogEntry.getDeploymentId(),is(
        repositoryService.createDeploymentQuery().singleResult().getId()));

    taskService.deleteAttachment(result.getId());

  }

  protected void completeTestProcess() {
    taskService.complete(taskService.createTaskQuery().singleResult().getId());
    assertProcessEnded(process.getId());
  }
}
