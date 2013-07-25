package org.camunda.bpm.pa.service;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;

public class NullPointerFailingDelegate implements JavaDelegate {

  @Override
  public void execute(DelegateExecution execution) throws Exception {
    throw new NullPointerException();
  }

}
