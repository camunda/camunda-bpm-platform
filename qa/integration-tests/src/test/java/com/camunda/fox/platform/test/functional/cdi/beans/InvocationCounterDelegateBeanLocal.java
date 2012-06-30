package com.camunda.fox.platform.test.functional.cdi.beans;

import javax.ejb.EJB;
import javax.inject.Named;

import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.JavaDelegate;

/**
 * 
 * @author Daniel Meyer
 *
 */
@Named
public class InvocationCounterDelegateBeanLocal implements JavaDelegate {
  
  @EJB(lookup="java:/" +
    "global/" +
  	"service/" +
  	"InvocationCounterServiceBean!com.camunda.fox.platform.test.functional.cdi.beans.InvocationCounterServiceLocal")
  private InvocationCounterServiceLocal invocationCounterService;

  public void execute(DelegateExecution execution) throws Exception {    
    execution.setVariable("invocationCounter", invocationCounterService.getNumOfInvocations());      
  }
  
  
}
