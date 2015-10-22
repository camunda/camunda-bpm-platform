package org.camunda.bpm.qa.upgrade.scenarios.multiinstance;

import static org.camunda.bpm.qa.upgrade.util.ActivityInstanceAssert.assertThat;
import static org.camunda.bpm.qa.upgrade.util.ActivityInstanceAssert.describeActivityInstanceTree;

import org.camunda.bpm.engine.runtime.ActivityInstance;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.qa.upgrade.ScenarioUnderTest;
import org.camunda.bpm.qa.upgrade.UpgradeTestRule;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;

@ScenarioUnderTest("MultiInstanceReceiveTaskScenario")
public class MultiInstanceReceiveTaskScenarioTest {

  @Rule
  public UpgradeTestRule rule = new UpgradeTestRule();

  @Test
  @ScenarioUnderTest("initParallel.1")
  public void testInitParallelCompletion() {
    // when the receive task messages are correlated
    rule.messageCorrelation("Message").correlateAll();

    // then
    rule.assertScenarioEnded();
  }

  @Test
  @ScenarioUnderTest("initParallel.2")
  public void testInitParallelActivityInstanceTree() {
    // given
    ProcessInstance instance = rule.processInstance();

    // when
    ActivityInstance activityInstance = rule.getRuntimeService().getActivityInstance(instance.getId());

    // then
    Assert.assertNotNull(activityInstance);
    assertThat(activityInstance).hasStructure(
      describeActivityInstanceTree(instance.getProcessDefinitionId())
        // the mi scope execution has the same activity instance id as the first child
        .beginScope("miReceiveTask")
          .activity("miReceiveTask")
          .activity("miReceiveTask")
      .done());
  }

  @Test
  @ScenarioUnderTest("initParallel.3")
  public void testInitParallelDeletion() {
    // given
    ProcessInstance instance = rule.processInstance();

    // when
    rule.getRuntimeService().deleteProcessInstance(instance.getId(), null);

    // then
    rule.assertScenarioEnded();
  }

  @Test
  @ScenarioUnderTest("initSequential.1")
  public void testInitSequentialCompletion() {
    // when the receive task messages are correlated
    for (int i = 0; i < 3; i++) {
      rule.messageCorrelation("Message").correlate();
    }

    // then
    rule.assertScenarioEnded();
  }

  @Test
  @ScenarioUnderTest("initSequential.2")
  public void testInitSequentialActivityInstanceTree() {
    // given
    ProcessInstance instance = rule.processInstance();

    // when
    ActivityInstance activityInstance = rule.getRuntimeService().getActivityInstance(instance.getId());

    // then
    Assert.assertNotNull(activityInstance);
    assertThat(activityInstance).hasStructure(
      describeActivityInstanceTree(instance.getProcessDefinitionId())
        .activity("miReceiveTask")
      .done());
  }

  @Test
  @ScenarioUnderTest("initSequential.3")
  public void testInitSequentialDeletion() {
    // given
    ProcessInstance instance = rule.processInstance();

    // when
    rule.getRuntimeService().deleteProcessInstance(instance.getId(), null);

    // then
    rule.assertScenarioEnded();
  }

}
