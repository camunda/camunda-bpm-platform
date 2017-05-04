package org.camunda.bpm.engine.test.api.runtime;

import static org.camunda.bpm.engine.test.util.ActivityInstanceAssert.assertThat;
import static org.camunda.bpm.engine.test.util.ActivityInstanceAssert.describeActivityInstanceTree;
import static org.junit.Assert.*;
import static org.hamcrest.CoreMatchers.containsString;
import org.camunda.bpm.engine.BadUserRequestException;
import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.TaskService;
import org.camunda.bpm.engine.history.HistoricProcessInstanceQuery;
import org.camunda.bpm.engine.repository.ProcessDefinition;
import org.camunda.bpm.engine.runtime.ActivityInstance;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.runtime.VariableInstance;
import org.camunda.bpm.engine.task.Task;
import org.camunda.bpm.engine.test.ProcessEngineRule;
import org.camunda.bpm.engine.test.util.ProcessEngineTestRule;
import org.camunda.bpm.engine.test.util.ProvidedProcessEngineRule;
import org.camunda.bpm.model.bpmn.Bpmn;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;


/**
 * 
 * @author Anna Pazola
 *
 */
public class RestartProcessInstanceSyncTest {

  protected ProcessEngineRule engineRule = new ProvidedProcessEngineRule();
  protected ProcessEngineTestRule testRule = new ProcessEngineTestRule(engineRule);

  protected RuntimeService runtimeService;
  protected TaskService taskService;

  @Before
  public void init() {
    runtimeService = engineRule.getRuntimeService();
    taskService = engineRule.getTaskService();
  }

  @Rule
  public RuleChain ruleChain = RuleChain.outerRule(engineRule).around(testRule);

  @Test
  public void shouldRestartSimpleProcessInstance() {
    BpmnModelInstance instance = Bpmn.createExecutableProcess("process")
        .startEvent()
        .userTask("userTask")
        .endEvent()
        .done();
    ProcessDefinition processDefinition = testRule.deployAndGetDefinition(instance);
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("process");
    Task task = taskService.createTaskQuery().processInstanceId(processInstance.getId()).active().singleResult();
    runtimeService.deleteProcessInstance(processInstance.getId(), "test");
    runtimeService.restartProcessInstances(processDefinition.getId()).startBeforeActivity("userTask").processInstanceIds(processInstance.getId()).execute();
    ProcessInstance restartedProcessInstance = runtimeService.createProcessInstanceQuery().active().singleResult();
    Task restartedTask = engineRule.getTaskService().createTaskQuery().processInstanceId(restartedProcessInstance.getId()).active().singleResult();
    Assert.assertEquals(task.getTaskDefinitionKey(), restartedTask.getTaskDefinitionKey());
  }

  @Test
  public void shouldRestartProcessInstanceWithTwoTasks() {
    BpmnModelInstance instance = Bpmn.createExecutableProcess("process")
        .startEvent()
        .userTask("userTask1")
        .userTask("userTask2")
        .endEvent()
        .done();
    ProcessDefinition processDefinition = testRule.deployAndGetDefinition(instance);
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("process");
    Task userTask1 = taskService.createTaskQuery().processInstanceId(processInstance.getId()).active().singleResult();
    taskService.complete(userTask1.getId());
    Task userTask2 = taskService.createTaskQuery().processInstanceId(processInstance.getId()).active().singleResult();
    runtimeService.deleteProcessInstance(processInstance.getId(), "test");
    runtimeService.restartProcessInstances(processDefinition.getId()).startBeforeActivity("userTask2").processInstanceIds(processInstance.getId()).execute();
    ProcessInstance restartedProcessInstance = runtimeService.createProcessInstanceQuery().active().singleResult();
    Task restartedTask = taskService.createTaskQuery().processInstanceId(restartedProcessInstance.getId()).active().singleResult();
    Assert.assertEquals(userTask2.getTaskDefinitionKey(), restartedTask.getTaskDefinitionKey());
    
    ActivityInstance updatedTree = runtimeService.getActivityInstance(restartedProcessInstance.getId());
    assertNotNull(updatedTree);
    assertEquals(restartedProcessInstance.getId(), updatedTree.getProcessInstanceId());
    assertThat(updatedTree).hasStructure(
        describeActivityInstanceTree(
            processDefinition.getId())
        .activity("userTask2")
        .done());

  }

  @Test
  public void shouldRestartProcessInstanceWithParallelGateway() {
    BpmnModelInstance instance = Bpmn.createExecutableProcess("process1")
        .startEvent()
        .parallelGateway("gateway")
        .userTask("userTask1")
        .endEvent()
        .moveToNode("gateway")
        .userTask("userTask2")
        .endEvent()
        .done();
    ProcessDefinition processDefinition = testRule.deployAndGetDefinition(instance);
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("process1");
    runtimeService.deleteProcessInstance(processInstance.getId(), "test");
    
    runtimeService.restartProcessInstances(processDefinition.getId()).startBeforeActivity("userTask1").startBeforeActivity("userTask2").processInstanceIds(processInstance.getId()).execute();
    ProcessInstance restartedProcessInstance = runtimeService.createProcessInstanceQuery().active().singleResult();
    ActivityInstance updatedTree = runtimeService.getActivityInstance(restartedProcessInstance.getId());
    assertNotNull(updatedTree);
    assertEquals(restartedProcessInstance.getId(), updatedTree.getProcessInstanceId());
    assertThat(updatedTree).hasStructure(
        describeActivityInstanceTree(
            processDefinition.getId())
        .activity("userTask1")
        .activity("userTask2")
        .done());
    
  }

  @Test
  public void shouldRestartProcessInstanceWithSubProcess() {
    BpmnModelInstance instance = Bpmn.createExecutableProcess("process2")
        .startEvent()
        .subProcess("subProcess")
        .embeddedSubProcess()
        .startEvent()
        .userTask("innerUserTask")
        .endEvent()
        .subProcessDone()
        .endEvent()
        .done();
    ProcessDefinition processDefinition = testRule.deployAndGetDefinition(instance);
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("process2");
    runtimeService.deleteProcessInstance(processInstance.getId(), "test");
    runtimeService.restartProcessInstances(processDefinition.getId()).startBeforeActivity("subProcess").processInstanceIds(processInstance.getId()).execute();
    ProcessInstance restartedProcessInstance = runtimeService.createProcessInstanceQuery().active().singleResult();
    ActivityInstance updatedTree = runtimeService.getActivityInstance(restartedProcessInstance.getId());
    assertNotNull(updatedTree);
    assertEquals(restartedProcessInstance.getId(), updatedTree.getProcessInstanceId());
    assertThat(updatedTree).hasStructure(
        describeActivityInstanceTree(
            processDefinition.getId())
        .beginScope("subProcess")
        .activity("innerUserTask")
        .done());
  }
  
  @Test
  public void shouldRestartProcessInstanceWithSubProcessAndVariables() {

    BpmnModelInstance instance = Bpmn.createExecutableProcess("process2")
        .startEvent()
        .userTask()
        .parallelGateway()
        .subProcess("subProcess")
        .embeddedSubProcess()
        .startEvent()
        .userTask("innerUserTask")
        .endEvent()
        .subProcessDone()
        .moveToLastGateway()
        .userTask("user")
        .endEvent()
        .done();

    ProcessDefinition processDefinition = testRule.deployAndGetDefinition(instance);
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("process2");
    runtimeService.setVariable(processInstance.getId(), "foo", "bar");
    Task task = taskService.createTaskQuery().active().singleResult();
    taskService.complete(task.getId());
    runtimeService.deleteProcessInstance(processInstance.getId(), "test");
    runtimeService.restartProcessInstances(processDefinition.getId()).startBeforeActivity("innerUserTask").processInstanceIds(processInstance.getId()).execute();

    ProcessInstance restartedProcessInstance = runtimeService.createProcessInstanceQuery().active().singleResult();
    VariableInstance variableInstance = runtimeService.createVariableInstanceQuery().processInstanceIdIn(restartedProcessInstance.getId()).singleResult();
 
    assertEquals(variableInstance.getExecutionId(), restartedProcessInstance.getId());
    assertEquals("foo", variableInstance.getName());
    assertEquals("bar", variableInstance.getValue());
  }
  
  @Test
  public void shouldRestartProcessInstanceUsingHistoricProcessInstanceQuery() {
    BpmnModelInstance instance = Bpmn.createExecutableProcess("process")
        .startEvent()
        .userTask("userTask1")
        .userTask("userTask2")
        .endEvent()
        .done();
    
    ProcessDefinition processDefinition = testRule.deployAndGetDefinition(instance);
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("process");
    Task task = taskService.createTaskQuery().processInstanceId(processInstance.getId()).active().singleResult();

    runtimeService.deleteProcessInstance(processInstance.getId(), "test");
    HistoricProcessInstanceQuery historicProcessInstanceQuery = engineRule.getHistoryService().createHistoricProcessInstanceQuery().processDefinitionId(processDefinition.getId());
    runtimeService.restartProcessInstances(processDefinition.getId()).startBeforeActivity("userTask1").historicProcessInstanceQuery(historicProcessInstanceQuery).execute();
    ProcessInstance restartedProcessInstance = runtimeService.createProcessInstanceQuery().active().singleResult();
    Task restartedTask = taskService.createTaskQuery().processInstanceId(restartedProcessInstance.getId()).active().singleResult();
    Assert.assertEquals(task.getTaskDefinitionKey(), restartedTask.getTaskDefinitionKey());
    
    ActivityInstance updatedTree = runtimeService.getActivityInstance(restartedProcessInstance.getId());
    assertNotNull(updatedTree);
    assertEquals(restartedProcessInstance.getId(), updatedTree.getProcessInstanceId());
    assertThat(updatedTree).hasStructure(
        describeActivityInstanceTree(
            processDefinition.getId())
        .activity("userTask1")
        .done());
  }
  
  @Test
  public void restartProcessInstanceWithNullProcessDefinitionId() {
    try {
      runtimeService.restartProcessInstances(null).execute();
      fail("exception expected");
    } catch (BadUserRequestException e) {
      Assert.assertThat(e.getMessage(), containsString("processDefinitionId is null"));
    }
  }
  
  @Test
  public void restartProcessInstanceWithoutProcessInstanceIds() {
    try {
      runtimeService.restartProcessInstances("foo").startAfterActivity("bar").execute();
      fail("exception expected");
    } catch (BadUserRequestException e) {
      Assert.assertThat(e.getMessage(), containsString("processInstanceIds is empty"));
    }
  }

  @Test
  public void restartProcessInstanceWithNullProcessInstanceId() {
    BpmnModelInstance instance = Bpmn.createExecutableProcess().startEvent().userTask().endEvent().done();
    ProcessDefinition processDefinition = testRule.deployAndGetDefinition(instance);
    try {
      runtimeService.restartProcessInstances(processDefinition.getId()).startAfterActivity("bar").processInstanceIds((String) null).execute();
      fail("exception expected");
    } catch (BadUserRequestException e) {
      Assert.assertThat(e.getMessage(), containsString("historicProcessInstanceId is null"));
    }
  }
  
  @Test
  public void restartNotExistingProcessInstance() {
    BpmnModelInstance instance = Bpmn.createExecutableProcess().startEvent().userTask().endEvent().done();
    ProcessDefinition processDefinition = testRule.deployAndGetDefinition(instance);
    try {
      runtimeService.restartProcessInstances(processDefinition.getId()).startBeforeActivity("bar").processInstanceIds("aaa").execute();
      fail("exception expected");
    } catch (BadUserRequestException e) {
      Assert.assertThat(e.getMessage(), containsString("the historic process instance cannot be found"));
    }
  }
  
  @Test
  public void restartProcessInstanceWithNotMatchingProcessDefinition() {
    BpmnModelInstance instance = Bpmn.createExecutableProcess("process").startEvent().userTask("userTask").endEvent().done();
    ProcessDefinition processDefinition = testRule.deployAndGetDefinition(instance);
    BpmnModelInstance instance2 = Bpmn.createExecutableProcess().done();
    ProcessDefinition processDefinition2 = testRule.deployAndGetDefinition(instance2);
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("process");
    try {
      runtimeService.restartProcessInstances(processDefinition2.getId()).startBeforeActivity("userTask").processInstanceIds(processInstance.getId()).execute();
      fail("exception expected");
    } catch (ProcessEngineException e) {
      Assert.assertThat(e.getMessage(), containsString("Its process definition '" + processDefinition.getId() + "' does not match given process definition '" + processDefinition2.getId() +"'" ));
    }
  }
}
