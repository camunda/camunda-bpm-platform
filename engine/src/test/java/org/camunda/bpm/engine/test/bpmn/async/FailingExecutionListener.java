package org.camunda.bpm.engine.test.bpmn.async;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.ExecutionListener;


public class FailingExecutionListener implements ExecutionListener {

  public void notify(DelegateExecution execution) throws Exception {
    throw new RuntimeException("I'm supposed to fail!");
  }

}
