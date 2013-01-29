package com.camunda.fox.platform.test.jobexecutor.beans;

import javax.ejb.Stateless;
import javax.inject.Named;

import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.JavaDelegate;

@Named
@Stateless
public class FailingSLSB implements JavaDelegate {

  public void execute(DelegateExecution execution) {
    
    // throw an unexpected exception
    throw new RuntimeException("I am failing");
    
  }
  
}
