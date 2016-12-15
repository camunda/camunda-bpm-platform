package org.camunda.bpm.integrationtest.functional.ejb.request.beans;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;

import javax.ejb.EJB;
import javax.inject.Named;

/**
 * 
 * @author Daniel Meyer
 *
 */
@Named
public class InvocationCounterDelegateBeanLocal implements JavaDelegate {
  
  @EJB(lookup="java:global/" +
  	"service/" +
  	"InvocationCounterServiceBean!org.camunda.bpm.integrationtest.functional.ejb.request.beans.InvocationCounterServiceLocal")
  private InvocationCounterServiceLocal invocationCounterService;

  public void execute(DelegateExecution execution) throws Exception {    
    execution.setVariable("invocationCounter", invocationCounterService.getNumOfInvocations());      
  }
  
  
}
