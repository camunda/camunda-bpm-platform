package org.camunda.bpm.qa.upgrade.scenarios.multiinstance;

import static org.camunda.bpm.qa.upgrade.util.ActivityInstanceAssert.assertThat;
import static org.camunda.bpm.qa.upgrade.util.ActivityInstanceAssert.describeActivityInstanceTree;

import java.util.List;

import org.camunda.bpm.engine.runtime.ActivityInstance;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.task.Task;
import org.camunda.bpm.qa.upgrade.ScenarioUnderTest;
import org.camunda.bpm.qa.upgrade.UpgradeTestRule;
import org.camunda.bpm.qa.upgrade.util.ThrowBpmnErrorDelegate;
import org.camunda.bpm.qa.upgrade.util.ThrowBpmnErrorDelegate.ThrowBpmnErrorDelegateException;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;

@ScenarioUnderTest("ParallelMultiInstanceSubprocessScenario")
public class ParallelMultiInstanceScenarioTest {

  @Rule
  public UpgradeTestRule rule = new UpgradeTestRule();

  @Test
  @ScenarioUnderTest("initNonInterruptingBoundaryEvent.1")
  public void testInitNonInterruptingBoundaryEventCompletionCase1() {
    // given
    List<Task> subProcessTasks = rule.taskQuery().taskDefinitionKey("subProcessTask").list();
    Task afterBoundaryTask = rule.taskQuery().taskDefinitionKey("afterBoundaryTask").singleResult();

    // when all instances are completed
    for (Task subProcessTask : subProcessTasks) {
      rule.getTaskService().complete(subProcessTask.getId());
    }

    // and
    rule.getTaskService().complete(afterBoundaryTask.getId());

    // then
    rule.assertScenarioEnded();
  }

  @Test
  @ScenarioUnderTest("initNonInterruptingBoundaryEvent.2")
  public void testInitNonInterruptingBoundaryEventCompletionCase2() {
    // given
    List<Task> subProcessTasks = rule.taskQuery().taskDefinitionKey("subProcessTask").list();
    Task afterBoundaryTask = rule.taskQuery().taskDefinitionKey("afterBoundaryTask").singleResult();

    // when
    rule.getTaskService().complete(afterBoundaryTask.getId());

    for (Task subProcessTask : subProcessTasks) {
      rule.getTaskService().complete(subProcessTask.getId());
    }

    // then
    rule.assertScenarioEnded();
  }

  @Test
  @ScenarioUnderTest("initNonInterruptingBoundaryEvent.3")
  public void testInitNonInterruptingBoundaryEventCompletionCase3() {
    // given
    List<Task> subProcessTasks = rule.taskQuery().taskDefinitionKey("subProcessTask").list();
    Task afterBoundaryTask = rule.taskQuery().taskDefinitionKey("afterBoundaryTask").singleResult();

    // when the first instance and the other two instances are completed
    rule.getTaskService().complete(subProcessTasks.get(0).getId());

    rule.getTaskService().complete(afterBoundaryTask.getId());
    rule.getTaskService().complete(subProcessTasks.get(1).getId());
    rule.getTaskService().complete(subProcessTasks.get(2).getId());

    // then
    rule.assertScenarioEnded();
  }

  @Test
  @ScenarioUnderTest("initNonInterruptingBoundaryEvent.4")
  public void testInitNonInterruptingBoundaryEventActivityInstanceTree() {
    // given
    ProcessInstance instance = rule.processInstance();

    // when
    ActivityInstance activityInstance = rule.getRuntimeService().getActivityInstance(instance.getId());

    // then
    Assert.assertNotNull(activityInstance);
    assertThat(activityInstance).hasStructure(
        describeActivityInstanceTree(instance.getProcessDefinitionId())
          .activity("afterBoundaryTask")
          // the mi scope execution has the same activity instance id as the first child
          .beginScope("miSubProcess")
            .activity("subProcessTask")
            .beginScope("miSubProcess")
              .activity("subProcessTask")
            .endScope()
            .beginScope("miSubProcess")
              .activity("subProcessTask")
            .endScope()
        .done());
  }

  @Test
  @ScenarioUnderTest("initNonInterruptingBoundaryEvent.5")
  public void testInitNonInterruptingBoundaryEventDeletion() {
    // given
    ProcessInstance instance = rule.processInstance();

    // when
    rule.getRuntimeService().deleteProcessInstance(instance.getId(), null);

    // then
    rule.assertScenarioEnded();
  }

  @Test
  @ScenarioUnderTest("initNonInterruptingBoundaryEvent.6")
  public void testInitNonInterruptingBoundaryEventThrowError() {
    // given
    ProcessInstance instance = rule.processInstance();
    Task miSubprocessTask = rule.taskQuery().taskDefinitionKey("subProcessTask").list().get(0);
    Task afterBoundaryTask = rule.taskQuery().taskDefinitionKey("afterBoundaryTask").singleResult();

    // when
    rule.getRuntimeService().setVariable(instance.getId(), ThrowBpmnErrorDelegate.ERROR_INDICATOR_VARIABLE, true);
    rule.getTaskService().complete(miSubprocessTask.getId());

    // then
    Assert.assertEquals(2, rule.taskQuery().count());

    Task escalatedTask = rule.taskQuery().taskDefinitionKey("escalatedTask").singleResult();
    Assert.assertNotNull(escalatedTask);

    // and
    rule.getTaskService().complete(escalatedTask.getId());
    rule.getTaskService().complete(afterBoundaryTask.getId());
    rule.assertScenarioEnded();
  }

  @Test
  @ScenarioUnderTest("initNonInterruptingBoundaryEvent.7")
  public void testInitNonInterruptingBoundaryEventThrowUnhandledException() {
    // given
    ProcessInstance instance = rule.processInstance();
    Task miSubprocessTask = rule.taskQuery().taskDefinitionKey("subProcessTask").list().get(0);

    // when
    rule.getRuntimeService().setVariable(instance.getId(), ThrowBpmnErrorDelegate.EXCEPTION_INDICATOR_VARIABLE, true);
    rule.getRuntimeService().setVariable(instance.getId(), ThrowBpmnErrorDelegate.EXCEPTION_MESSAGE_VARIABLE, "unhandledException");

    // then
    try {
      rule.getTaskService().complete(miSubprocessTask.getId());
      Assert.fail("should throw a ThrowBpmnErrorDelegateException");

    } catch (ThrowBpmnErrorDelegateException e) {
      Assert.assertEquals("unhandledException", e.getMessage());
    }
  }

}
