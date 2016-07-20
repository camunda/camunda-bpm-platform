package org.camunda.bpm.engine.test.api.variables.scope;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;

/**
 * @author Askar Akhmerov
 */
public class TargetScopeVariableSetter implements JavaDelegate {
  @Override
  public void execute(DelegateExecution execution) throws Exception {
    execution.setVariable("targetOrderId", execution.getVariableLocal("targetOrderId"),"SubProcess_1");
  }
}
