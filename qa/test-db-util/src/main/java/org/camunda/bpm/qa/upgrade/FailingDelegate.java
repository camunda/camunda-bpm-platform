package org.camunda.bpm.qa.upgrade;

import org.camunda.bpm.engine.delegate.BpmnError;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;

public class FailingDelegate implements JavaDelegate {

  public void execute(DelegateExecution execution) throws Exception {
      throw new BpmnError("org.camunda.bpm.qa.upgrade.BuisnessError", "Expected exception");
  }
}
