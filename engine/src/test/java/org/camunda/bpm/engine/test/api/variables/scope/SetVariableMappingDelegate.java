package org.camunda.bpm.engine.test.api.variables.scope;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.DelegateVariableMapping;
import org.camunda.bpm.engine.delegate.VariableScope;
import org.camunda.bpm.engine.variable.VariableMap;

/**
 * @author Askar Akhmerov
 */
public class SetVariableMappingDelegate implements DelegateVariableMapping {
  @Override
  public void mapInputVariables(DelegateExecution superExecution, VariableMap subVariables) {
    subVariables.putValue("orderId",superExecution.getVariable("orderId"));
  }

  @Override
  public void mapOutputVariables(DelegateExecution superExecution, VariableScope subInstance) {
    superExecution.setVariable("targetOrderId",subInstance.getVariable("orderId"),"SubProcess_1");
  }
}
