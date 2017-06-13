package org.camunda.bpm.engine.test.api.variables;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.Arrays;
import java.util.List;

import org.camunda.bpm.engine.ProcessEngineConfiguration;
import org.camunda.bpm.engine.history.HistoricVariableInstance;
import org.camunda.bpm.engine.repository.ProcessDefinition;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.runtime.VariableInstance;
import org.camunda.bpm.engine.task.Task;
import org.camunda.bpm.engine.test.ProcessEngineRule;
import org.camunda.bpm.engine.test.RequiredHistoryLevel;
import org.camunda.bpm.engine.test.util.ProcessEngineTestRule;
import org.camunda.bpm.engine.variable.VariableMap;
import org.camunda.bpm.engine.variable.Variables;
import org.camunda.bpm.model.bpmn.Bpmn;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.junit.Rule;
import org.junit.Test;

public class ModifyVariableInSameTransactionTest {
  @Rule
  public ProcessEngineRule engineRule = new ProcessEngineRule();
  @Rule
  public ProcessEngineTestRule testHelper = new ProcessEngineTestRule(engineRule);

  @Test
  @RequiredHistoryLevel(ProcessEngineConfiguration.HISTORY_AUDIT)
  public void testDeleteAndInsertTheSameVariableByteArray() {
    BpmnModelInstance bpmnModel =
        Bpmn.createExecutableProcess("serviceTaskProcess")
        .startEvent()
        .userTask("userTask")
        .serviceTask("service")
          .camundaClass(DeleteAndInsertVariableDelegate.class)
        .userTask("userTask1")
        .endEvent()
        .done();
    ProcessDefinition processDefinition = testHelper.deployAndGetDefinition(bpmnModel);
    VariableMap variables = Variables.createVariables().putValue("listVar", Arrays.asList(new int[] { 1, 2, 3 }));
    ProcessInstance instance = engineRule.getRuntimeService().startProcessInstanceById(processDefinition.getId(), variables);

    Task task = engineRule.getTaskService().createTaskQuery().singleResult();
    engineRule.getTaskService().complete(task.getId());

    VariableInstance variable = engineRule.getRuntimeService().createVariableInstanceQuery().processInstanceIdIn(instance.getId()).variableName("listVar").singleResult();
    assertNotNull(variable);
    assertEquals("stringValue", variable.getValue());
    HistoricVariableInstance historicVariable = engineRule.getHistoryService().createHistoricVariableInstanceQuery().singleResult();
    assertEquals(variable.getName(), historicVariable.getName());
    assertEquals(HistoricVariableInstance.STATE_CREATED, historicVariable.getState());
  }

  @Test
  @RequiredHistoryLevel(ProcessEngineConfiguration.HISTORY_AUDIT)
  public void testDeleteAndInsertTheSameVariable() {
    BpmnModelInstance bpmnModel =
        Bpmn.createExecutableProcess("serviceTaskProcess")
        .startEvent()
        .userTask("userTask")
        .serviceTask("service")
          .camundaClass(DeleteAndInsertVariableDelegate.class)
        .userTask("userTask1")
        .endEvent()
        .done();
    ProcessDefinition processDefinition = testHelper.deployAndGetDefinition(bpmnModel);
    VariableMap variables = Variables.createVariables().putValue("foo", "firstValue");
    ProcessInstance instance = engineRule.getRuntimeService().startProcessInstanceById(processDefinition.getId(), variables);

    Task task = engineRule.getTaskService().createTaskQuery().singleResult();
    engineRule.getTaskService().complete(task.getId());

    VariableInstance variable = engineRule.getRuntimeService().createVariableInstanceQuery().processInstanceIdIn(instance.getId()).variableName("foo").singleResult();
    assertNotNull(variable);
    assertEquals("secondValue", variable.getValue());
    HistoricVariableInstance historicVariable = engineRule.getHistoryService().createHistoricVariableInstanceQuery().singleResult();
    assertEquals(variable.getName(), historicVariable.getName());
    assertEquals(HistoricVariableInstance.STATE_CREATED, historicVariable.getState());
  }

  @Test
  @RequiredHistoryLevel(ProcessEngineConfiguration.HISTORY_AUDIT)
  public void testInsertDeleteInsertTheSameVariable() {
    BpmnModelInstance bpmnModel =
        Bpmn.createExecutableProcess("serviceTaskProcess")
        .startEvent()
        .userTask("userTask")
        .serviceTask("service")
          .camundaClass(InsertDeleteInsertVariableDelegate.class)
        .userTask("userTask1")
        .endEvent()
        .done();
    ProcessDefinition processDefinition = testHelper.deployAndGetDefinition(bpmnModel);
    VariableMap variables = Variables.createVariables().putValue("listVar", Arrays.asList(new int[] { 1, 2, 3 }));
    ProcessInstance instance = engineRule.getRuntimeService().startProcessInstanceById(processDefinition.getId(), variables);

    Task task = engineRule.getTaskService().createTaskQuery().singleResult();
    engineRule.getTaskService().complete(task.getId());

    VariableInstance variable = engineRule.getRuntimeService().createVariableInstanceQuery().processInstanceIdIn(instance.getId()).variableName("foo")
        .singleResult();
    assertNotNull(variable);
    assertEquals("bar", variable.getValue());
    List<HistoricVariableInstance> historyVariables = engineRule.getHistoryService().createHistoricVariableInstanceQuery().list();
    for (HistoricVariableInstance historicVariable : historyVariables) {
      if (variable.getName().equals(historicVariable.getName())) {
        assertEquals(HistoricVariableInstance.STATE_CREATED, historicVariable.getState());
        break;
      }
    }
  }
}
