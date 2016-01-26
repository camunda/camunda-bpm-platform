package org.camunda.bpm.cockpit.plugin.base.pa;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;

public class FailingDelegate implements JavaDelegate {

  @Override
  public void execute(DelegateExecution execution) throws Exception {
    throw new RuntimeException("I am failing!");
  }

}
