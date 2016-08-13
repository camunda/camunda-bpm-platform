package org.camunda.bpm.integrationtest.functional.transactions;

import javax.inject.Named;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;

@Named
public class FailingDelegate implements JavaDelegate {

  @Override
  public void execute(DelegateExecution execution) throws Exception {
    throw new RuntimeException("I'm a complete failure!");
  }

}
