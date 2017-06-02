package org.camunda.bpm.engine.test.api.history;

import static org.junit.Assert.assertEquals;

import java.util.List;

import org.camunda.bpm.engine.HistoryService;
import org.camunda.bpm.engine.ProcessEngineConfiguration;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.TaskService;
import org.camunda.bpm.engine.history.HistoricVariableInstance;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.task.Task;
import org.camunda.bpm.engine.test.ProcessEngineRule;
import org.camunda.bpm.engine.test.RequiredHistoryLevel;
import org.camunda.bpm.engine.test.api.runtime.migration.models.ProcessModels;
import org.camunda.bpm.engine.test.util.ProcessEngineTestRule;
import org.camunda.bpm.engine.variable.Variables;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;

@RequiredHistoryLevel(ProcessEngineConfiguration.HISTORY_AUDIT)
public class VariableStateTest {

  public ProcessEngineRule engineRule = new ProcessEngineRule(true);
  public ProcessEngineTestRule testRule = new ProcessEngineTestRule(engineRule);

  private HistoryService historyService;
  private TaskService taskService;
  private RuntimeService runtimeService;

  @Rule
  public RuleChain ruleChain = RuleChain.outerRule(engineRule).around(testRule);

  @Before
  public void init() {
    runtimeService = engineRule.getRuntimeService();
    historyService = engineRule.getHistoryService();
    taskService = engineRule.getTaskService();
  }

  @Test
  public void shouldSetCreatedState() {
    //given
    testRule.deployAndGetDefinition(ProcessModels.TWO_TASKS_PROCESS);
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("Process");

    //when
    runtimeService.setVariables(processInstance.getId(), Variables.createVariables().putValue("bar", "abc"));

    //then
    List<HistoricVariableInstance> variable = historyService.createHistoricVariableInstanceQuery().list();
    assertEquals("CREATED", variable.get(0).getState());
  }

  @Test
  public void shouldSetDeletedState() {
    //given
    testRule.deployAndGetDefinition(ProcessModels.TWO_TASKS_PROCESS);
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("Process");
    List<Task> taskList = taskService.createTaskQuery().list();
    taskService.setVariables(taskList.get(0).getId(), Variables.createVariables().putValue("bar", "abc"));
    taskService.complete(taskList.get(0).getId());

    //when
    runtimeService.removeVariable(processInstance.getId(), "bar");

    //then
    HistoricVariableInstance instance = historyService.createHistoricVariableInstanceQuery().includeDeleted().singleResult();
    assertEquals("DELETED", instance.getState());
  }

  @Test
  public void shouldSetDifferentStates() {
    //given
    testRule.deployAndGetDefinition(ProcessModels.TWO_TASKS_PROCESS);
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("Process", Variables.createVariables().putValue("initial", "foo"));
    List<Task> taskList = taskService.createTaskQuery().list();
    taskService.setVariables(taskList.get(0).getId(), Variables.createVariables().putValue("bar", "abc"));
    taskService.complete(taskList.get(0).getId());

    //when
    runtimeService.removeVariable(processInstance.getId(), "bar");

    //then
    List<HistoricVariableInstance> variables = historyService.createHistoricVariableInstanceQuery().includeDeleted().list();
    Assert.assertNotEquals(variables.get(0).getState(), variables.get(1).getState());
  }

  @Test
  public void shouldNotIncludeDeleted() {
    //given
    testRule.deployAndGetDefinition(ProcessModels.TWO_TASKS_PROCESS);
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("Process", Variables.createVariables().putValue("initial", "foo"));
    List<Task> taskList = taskService.createTaskQuery().list();
    System.out.println(taskList.get(0));
    taskService.setVariables(taskList.get(0).getId(), Variables.createVariables().putValue("bar", "abc"));
    taskService.complete(taskList.get(0).getId());

    //when
    runtimeService.removeVariable(processInstance.getId(), "bar");

    //then
    HistoricVariableInstance variable = historyService.createHistoricVariableInstanceQuery().singleResult();
    assertEquals("CREATED", variable.getState());
    assertEquals("initial", variable.getName());
    assertEquals("foo", variable.getValue());
  }
}
