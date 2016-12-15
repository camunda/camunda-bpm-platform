package org.camunda.bpm.integrationtest.jobexecutor.beans;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;

import javax.ejb.Stateless;
import javax.inject.Named;

@Named
@Stateless
public class FailingSLSB implements JavaDelegate {

  public void execute(DelegateExecution execution) {
    
    // throw an unexpected exception
    throw new RuntimeException("I am failing");
    
  }
  
}
