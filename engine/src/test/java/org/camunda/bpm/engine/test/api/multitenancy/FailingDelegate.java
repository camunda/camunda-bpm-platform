package org.camunda.bpm.engine.test.api.multitenancy;

import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;

public class FailingDelegate implements JavaDelegate {

  public void execute(DelegateExecution execution) throws Exception {
      throw new ProcessEngineException("Expected exception");
  }
}
