package org.camunda.bpm.engine.test.api.variables.scope;

import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.ScriptEvaluationException;
import org.camunda.bpm.engine.impl.persistence.entity.ProcessInstanceWithVariablesImpl;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.test.Deployment;
import org.camunda.bpm.engine.test.ProcessEngineRule;
import org.camunda.bpm.engine.variable.VariableMap;
import org.camunda.bpm.engine.variable.Variables;
import org.junit.Rule;
import org.junit.Test;

import java.util.Arrays;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsInstanceOf.instanceOf;
import static org.junit.Assert.assertThat;

/**
 * @author Askar Akhmerov
 */
public class TargetVariableScopeTest {

  @Rule
  public ProcessEngineRule rule = new ProcessEngineRule();

  @Test
  @Deployment(resources = {"org/camunda/bpm/engine/test/api/variables/scope/TargetVariableScopeTest.testExecutionWithDelegateProcess.bpmn","org/camunda/bpm/engine/test/api/variables/scope/doer.bpmn"})
  public void testExecutionWithDelegateProcess() {
    // Given we create a new process instance
    VariableMap variables = Variables.createVariables().putValue("orderIds", Arrays.asList(new int[]{1, 2, 3}));
    ProcessInstance processInstance = rule.getRuntimeService().startProcessInstanceByKey("Process_MultiInstanceCallAcitivity",variables);

    // it runs without any problems
    assertThat(processInstance.isEnded(),is(true));
    assertThat(((ProcessInstanceWithVariablesImpl) processInstance).getVariables().containsKey("targetOrderId"),is(false));
  }

  @Test
  @Deployment(resources = {"org/camunda/bpm/engine/test/api/variables/scope/TargetVariableScopeTest.testExecutionWithScriptTargetScope.bpmn","org/camunda/bpm/engine/test/api/variables/scope/doer.bpmn"})
  public void testExecutionWithScriptTargetScope () {
    VariableMap variables = Variables.createVariables().putValue("orderIds", Arrays.asList(new int[]{1, 2, 3}));
    ProcessInstance processInstance = rule.getRuntimeService().startProcessInstanceByKey("Process_MultiInstanceCallAcitivity",variables);

    // it runs without any problems
    assertThat(processInstance.isEnded(),is(true));
    assertThat(((ProcessInstanceWithVariablesImpl) processInstance).getVariables().containsKey("targetOrderId"),is(false));
  }

  @Test
  @Deployment(resources = {"org/camunda/bpm/engine/test/api/variables/scope/TargetVariableScopeTest.testExecutionWithoutProperTargetScope.bpmn","org/camunda/bpm/engine/test/api/variables/scope/doer.bpmn"})
  public void testExecutionWithoutProperTargetScope () {
    VariableMap variables = Variables.createVariables().putValue("orderIds", Arrays.asList(new int[]{1, 2, 3}));
    //fails due to inappropriate variable scope target
    try {
      rule.getRuntimeService().startProcessInstanceByKey("Process_MultiInstanceCallAcitivity",variables);
    } catch (ScriptEvaluationException e) {
      assertThat(e.getCause().getCause(), is(instanceOf(ProcessEngineException.class)));
      assertThat(e.getCause().getCause().getMessage(), is("Scope with specified activity ID [NOT_EXISTING] not found"));
    }
  }
}
