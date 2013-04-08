package org.camunda.bpm.integrationtest.functional.ejb.remote.bean;

import javax.ejb.EJB;
import javax.inject.Named;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.camunda.bpm.integrationtest.util.TestContainer;


/**
 * A CDI bean delegating to the remote business 
 * interface of a SFSB from a different application.
 * 
 * @author Daniel Meyer
 *
 */
@Named
public class RemoteSFSBClientDelegateBean implements JavaDelegate {
  
  @EJB(lookup="java:global/" +
          TestContainer.APP_NAME +
          "service/" +
          "RemoteSFSBean!org.camunda.bpm.integrationtest.functional.ejb.remote.bean.BusinessInterface")
  private BusinessInterface businessInterface;

  @Override
  public void execute(DelegateExecution execution) throws Exception {
    execution.setVariable("result", businessInterface.doBusiness());
  }
  
}
