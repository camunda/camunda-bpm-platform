package org.camunda.bpm.engine.test.api.mgmt;

import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;

public class FailingDelegate implements JavaDelegate {

  public void execute(DelegateExecution execution) throws Exception {
    Boolean fail = (Boolean) execution.getVariable("fail");
    if (fail != false) {
      throw new ProcessEngineException("Expected exception");
    }
  }

}
