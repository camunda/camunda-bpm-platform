package com.camunda.fox.engine.test.cmd;

import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.ExecutionListener;


public class FailingExecutionListener implements ExecutionListener {

  public void notify(DelegateExecution execution) throws Exception {
    throw new RuntimeException("I'm supposed to fail!");
  }

}
