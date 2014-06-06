package org.camunda.bpm.engine.test.api.runtime;

import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;

public class FailingDelegate implements JavaDelegate {

  public static final String EXCEPTION_MESSAGE = "Expected exception.";

  @Override
  public void execute(DelegateExecution execution) throws Exception {

    Boolean fail = (Boolean) execution.getVariable("fail");

    if (fail == null || fail == true) {
      throw new ProcessEngineException(EXCEPTION_MESSAGE);
    }

  }

}
