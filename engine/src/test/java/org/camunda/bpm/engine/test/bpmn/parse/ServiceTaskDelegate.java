package org.camunda.bpm.engine.test.bpmn.parse;

import org.camunda.bpm.engine.delegate.BpmnError;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;

public class ServiceTaskDelegate implements JavaDelegate {

  public static boolean firstAttempt = true;

  public void execute(DelegateExecution execution) throws Exception {
    if (firstAttempt) {
      throw new BpmnError("It is supposed to fail.");
    }
  }

}
