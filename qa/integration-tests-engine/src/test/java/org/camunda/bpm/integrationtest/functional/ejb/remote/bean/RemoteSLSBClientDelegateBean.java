package org.camunda.bpm.integrationtest.functional.ejb.remote.bean;

import javax.ejb.EJB;
import javax.inject.Named;
import javax.naming.InitialContext;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.camunda.bpm.integrationtest.util.TestContainer;


/**
 * A CDI bean delegating to the remote business 
 * interface of a SLSB from a different application.
 * 
 * @author Daniel Meyer
 *
 */
@Named
public class RemoteSLSBClientDelegateBean implements JavaDelegate {
  
  @Override
  public void execute(DelegateExecution execution) throws Exception {
    BusinessInterface businessInterface = (BusinessInterface) new InitialContext().lookup("java:global/" +
        TestContainer.getAppName() +
        "service/" +
        "RemoteSLSBean!org.camunda.bpm.integrationtest.functional.ejb.remote.bean.BusinessInterface");

    execution.setVariable("result", businessInterface.doBusiness());
  }

}
