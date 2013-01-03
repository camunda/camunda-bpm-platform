package com.camunda.fox.engine.test.cmd;

import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.JavaDelegate;


public class FailingDelegate implements JavaDelegate {

  public void execute(DelegateExecution execution) throws Exception {
    throw new RuntimeException("I'm supposed to fail!");
  }

}
