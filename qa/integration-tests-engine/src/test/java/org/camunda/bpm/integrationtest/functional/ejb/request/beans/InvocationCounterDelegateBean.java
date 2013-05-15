package org.camunda.bpm.integrationtest.functional.ejb.request.beans;

import javax.ejb.EJB;
import javax.inject.Named;
import javax.naming.InitialContext;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.camunda.bpm.integrationtest.util.TestContainer;


/**
 * 
 * @author Daniel Meyer
 *
 */
@Named
public class InvocationCounterDelegateBean implements JavaDelegate {
  
  public void execute(DelegateExecution execution) throws Exception {
    InvocationCounterService invocationCounterService = (InvocationCounterService) new InitialContext().lookup("java:global/" +
        TestContainer.getAppName() +
        "service/" +
        "InvocationCounterServiceBean!org.camunda.bpm.integrationtest.functional.ejb.request.beans.InvocationCounterService");

    execution.setVariable("invocationCounter", invocationCounterService.getNumOfInvocations());      
  }

}
