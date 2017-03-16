package org.camunda.bpm.engine.test.api.runtime;

import static org.camunda.bpm.engine.test.util.ActivityInstanceAssert.assertThat;
import static org.camunda.bpm.engine.test.util.ActivityInstanceAssert.describeActivityInstanceTree;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.util.ArrayList;
import java.util.List;

import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.repository.DeploymentWithDefinitions;
import org.camunda.bpm.engine.repository.ProcessDefinition;
import org.camunda.bpm.engine.runtime.ActivityInstance;
import org.camunda.bpm.engine.task.Task;
import org.camunda.bpm.engine.test.ProcessEngineRule;
import org.camunda.bpm.engine.test.util.ProcessEngineTestRule;
import org.camunda.bpm.engine.test.util.ProvidedProcessEngineRule;
import org.camunda.bpm.model.bpmn.Bpmn;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;

public class ModificationExecutionSyncTest {

  protected ProcessEngineRule rule = new ProvidedProcessEngineRule();
  protected ProcessEngineTestRule testRule = new ProcessEngineTestRule(rule);
  protected BatchModificationHelper helper = new BatchModificationHelper(rule);

  @Rule
  public RuleChain ruleChain = RuleChain.outerRule(rule).around(testRule);

  protected RuntimeService runtimeService;
  protected BpmnModelInstance instance;

  @Before
  public void createBpmnModelInstance() {
    this.instance = Bpmn.createExecutableProcess("process1")
        .startEvent("start")
        .userTask("user1")
        .sequenceFlowId("seq")
        .userTask("user2")
        .endEvent("end")
        .done();
  }

  @Before
  public void initServices() {
    runtimeService = rule.getRuntimeService();
  }

  @After
  public void removeInstanceIds() {
    helper.currentProcessInstances = new ArrayList<String>();
  }

  @Test
  public void testStartBefore() {
    DeploymentWithDefinitions deployment = testRule.deploy(instance);
    ProcessDefinition definition = deployment.getDeployedProcessDefinitions().get(0);

    List<String> processInstanceIds = helper.startInstances("process1", 2);

    runtimeService.createModification(definition.getId()).startBeforeActivity("user2").processInstanceIds(processInstanceIds).execute();

    for (String processInstanceId : processInstanceIds) {
      ActivityInstance updatedTree = runtimeService.getActivityInstance(processInstanceId);
      assertNotNull(updatedTree);
      assertEquals(processInstanceId, updatedTree.getProcessInstanceId());

      assertThat(updatedTree).hasStructure(
          describeActivityInstanceTree(
              definition.getId())
          .activity("user1")
          .activity("user2")
          .done());
    }
  }

  @Test
  public void testStartAfter() {
    DeploymentWithDefinitions deployment = testRule.deploy(instance);
    ProcessDefinition definition = deployment.getDeployedProcessDefinitions().get(0);

    List<String> processInstanceIds = helper.startInstances("process1", 2);

    runtimeService.createModification(definition.getId()).startAfterActivity("user2").processInstanceIds(processInstanceIds).execute();

    for (String processInstanceId : processInstanceIds) {
      ActivityInstance updatedTree = runtimeService.getActivityInstance(processInstanceId);
      assertNotNull(updatedTree);
      assertEquals(processInstanceId, updatedTree.getProcessInstanceId());

      assertThat(updatedTree).hasStructure(describeActivityInstanceTree(definition.getId()).activity("user1").done());
    }
  }

  @Test
  public void testStartTransition() {
    DeploymentWithDefinitions deployment = testRule.deploy(instance);
    ProcessDefinition definition = deployment.getDeployedProcessDefinitions().get(0);

    List<String> processInstanceIds = helper.startInstances("process1", 2);

    runtimeService.createModification(definition.getId()).startTransition("seq").processInstanceIds(processInstanceIds).execute();

    for (String processInstanceId : processInstanceIds) {
      ActivityInstance updatedTree = runtimeService.getActivityInstance(processInstanceId);
      assertNotNull(updatedTree);
      assertEquals(processInstanceId, updatedTree.getProcessInstanceId());

      assertThat(updatedTree).hasStructure(describeActivityInstanceTree(definition.getId()).activity("user1").activity("user2").done());
    }
  }

  @Test
  public void testCancelAll() {
    ProcessDefinition processDefinition = testRule.deployAndGetDefinition(instance);
    List<String> processInstanceIds = helper.startInstances("process1", 2);

    runtimeService.createModification(processDefinition.getId()).cancelAllForActivity("user1").processInstanceIds(processInstanceIds).execute();

    for (String processInstanceId : processInstanceIds) {
      ActivityInstance updatedTree = runtimeService.getActivityInstance(processInstanceId);
      assertNull(updatedTree);
    }
  }

  @Test
  public void testStartBeforeAndCancelAll() {
    DeploymentWithDefinitions deployment = testRule.deploy(instance);
    ProcessDefinition definition = deployment.getDeployedProcessDefinitions().get(0);

    List<String> processInstanceIds = helper.startInstances("process1", 2);

    runtimeService.createModification(definition.getId()).cancelAllForActivity("user1").startBeforeActivity("user2").processInstanceIds(processInstanceIds).execute();

    for (String processInstanceId : processInstanceIds) {
      ActivityInstance updatedTree = runtimeService.getActivityInstance(processInstanceId);
      assertNotNull(updatedTree);
      assertEquals(processInstanceId, updatedTree.getProcessInstanceId());

      assertThat(updatedTree).hasStructure(describeActivityInstanceTree(definition.getId()).activity("user2").done());
    }
  }

  @Test
  public void testDifferentStates() {
    DeploymentWithDefinitions deployment = testRule.deploy(instance);
    ProcessDefinition definition = deployment.getDeployedProcessDefinitions().get(0);

    List<String> processInstanceIds = helper.startInstances("process1", 1);
    Task task = rule.getTaskService().createTaskQuery().singleResult();
    rule.getTaskService().complete(task.getId());

    List<String> anotherProcessInstanceIds = helper.startInstances("process1", 2);
    processInstanceIds.addAll(anotherProcessInstanceIds);

    runtimeService.createModification(definition.getId()).cancelAllForActivity("user1").processInstanceIds(processInstanceIds).execute();

    ActivityInstance updatedTree = null;
    String processInstanceId = processInstanceIds.get(0);
    updatedTree = runtimeService.getActivityInstance(processInstanceId);
    assertNotNull(updatedTree);
    assertEquals(processInstanceId, updatedTree.getProcessInstanceId());
    assertThat(updatedTree).hasStructure(describeActivityInstanceTree(definition.getId()).activity("user2").done());

    processInstanceId = processInstanceIds.get(1);
    updatedTree = runtimeService.getActivityInstance(processInstanceId);
    assertNull(updatedTree);

    processInstanceId = processInstanceIds.get(2);
    updatedTree = runtimeService.getActivityInstance(processInstanceId);
    assertNull(updatedTree);
  }
}
