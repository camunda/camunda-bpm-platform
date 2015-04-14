package org.camunda.bpm.engine.test.bpmn.callactivity;

import org.camunda.bpm.engine.delegate.BpmnError;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;

public class ServiceTaskThrowBpmnError implements JavaDelegate {

  @Override
  public void execute(DelegateExecution execution) throws Exception {
    throw new BpmnError("errorCode", "ouch!");
  }

}
