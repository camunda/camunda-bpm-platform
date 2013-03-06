package org.camunda.bpm.integrationtest.jobexecutor.beans;

import javax.ejb.Stateless;
import javax.inject.Named;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;

@Named
@Stateless
public class FailingSLSB implements JavaDelegate {

  public void execute(DelegateExecution execution) {
    
    // throw an unexpected exception
    throw new RuntimeException("I am failing");
    
  }
  
}
