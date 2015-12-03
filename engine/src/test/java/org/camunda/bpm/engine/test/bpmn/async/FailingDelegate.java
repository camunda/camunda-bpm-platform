package org.camunda.bpm.engine.test.bpmn.async;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;


public class FailingDelegate implements JavaDelegate {

  public void execute(DelegateExecution execution) throws Exception {
    throw new RuntimeException("I'm supposed to fail!");
  }

}
