package org.camunda.bpm.engine.test.standalone.deploy;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.DelegateTask;

public class SingleVariableListener implements org.camunda.bpm.engine.delegate.TaskListener {

  @Override
  public void notify(DelegateTask delegateTask) {
    DelegateExecution execution = delegateTask.getExecution();
    execution.setVariableLocal("isListenerCalled", "true");
  }
}