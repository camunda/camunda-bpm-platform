package org.camunda.bpm.integrationtest.functional.spring.beans;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.springframework.stereotype.Component;

@Component
public class ErrorDelegate implements JavaDelegate {
  @Override
  public void execute(DelegateExecution execution) throws Exception {
    throw new RuntimeException("I'm supposed to fail!");
  }
}
