package com.camunda.fox.platform.test.functional.ejb.request.beans;

import javax.inject.Named;

import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.JavaDelegate;

import com.camunda.fox.platform.test.util.JndiConstants;

/**
 * 
 * @author Daniel Meyer
 *
 */
@Named
public class InvocationCounterDelegateBean implements JavaDelegate {
  
  private InvocationCounterService invocationCounterService = JndiConstants.lookup("java:global/" +
              JndiConstants.getAppName() +
              "service/" +
              "InvocationCounterServiceBean!com.camunda.fox.platform.test.functional.ejb.request.beans.InvocationCounterService");

  public void execute(DelegateExecution execution) throws Exception {    
    execution.setVariable("invocationCounter", invocationCounterService.getNumOfInvocations());      
  }
  
  
}
