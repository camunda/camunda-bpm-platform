package org.camunda.bpm.qa.upgrade.scenarios770;

import static org.junit.Assert.assertEquals;

import java.util.List;

import org.camunda.bpm.engine.history.HistoricVariableInstance;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.test.ProcessEngineRule;
import org.camunda.bpm.qa.upgrade.Origin;
import org.camunda.bpm.qa.upgrade.ScenarioUnderTest;
import org.junit.Rule;
import org.junit.Test;

@ScenarioUnderTest("CreateProcessInstanceWithVariableScenario")
@Origin("7.7.0")
public class CreateProcessInstanceWithVariableTest {

  @Rule
  public ProcessEngineRule engineRule = new ProcessEngineRule("camunda.cfg.xml");

  @ScenarioUnderTest("initProcessInstance.1")
  @Test
  public void testCreateProcessInstanceWithVariable() {
    // then
    ProcessInstance processInstance = engineRule.getRuntimeService().createProcessInstanceQuery().processInstanceBusinessKey("process").singleResult();
    List<HistoricVariableInstance> variables = engineRule.getHistoryService().createHistoricVariableInstanceQuery().processInstanceId(processInstance.getId()).list();
    assertEquals(1, variables.size());
    assertEquals("foo", variables.get(0).getName());
    assertEquals("bar", variables.get(0).getValue());
  }
}
