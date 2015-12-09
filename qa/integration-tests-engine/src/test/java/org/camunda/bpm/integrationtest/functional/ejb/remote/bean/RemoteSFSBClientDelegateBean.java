package org.camunda.bpm.integrationtest.functional.ejb.remote.bean;

import javax.inject.Named;
import javax.naming.InitialContext;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.camunda.bpm.integrationtest.util.TestConstants;


/**
 * A CDI bean delegating to the remote business
 * interface of a SFSB from a different application.
 *
 * @author Daniel Meyer
 *
 */
@Named
public class RemoteSFSBClientDelegateBean implements JavaDelegate {

  @Override
  public void execute(DelegateExecution execution) throws Exception {
    BusinessInterface businessInterface = (BusinessInterface) new InitialContext().lookup("java:global/" +
        TestConstants.getAppName() +
        "service/" +
        "RemoteSFSBean!org.camunda.bpm.integrationtest.functional.ejb.remote.bean.BusinessInterface");

    execution.setVariable("result", businessInterface.doBusiness());
  }

}
