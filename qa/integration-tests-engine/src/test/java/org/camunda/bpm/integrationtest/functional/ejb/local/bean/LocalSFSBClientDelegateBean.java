package org.camunda.bpm.integrationtest.functional.ejb.local.bean;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;

import javax.ejb.EJB;
import javax.inject.Named;

/**
 * A CDI bean delegating to the local business 
 * interface of a SFSB from a different application.
 * 
 * @author Daniel Meyer
 *
 */
@Named
public class LocalSFSBClientDelegateBean implements JavaDelegate {
  
  @EJB(lookup="java:global/service/LocalSFSBean!org.camunda.bpm.integrationtest.functional.ejb.local.bean.BusinessInterface")
  private BusinessInterface businessInterface;

  @Override
  public void execute(DelegateExecution execution) throws Exception {
    execution.setVariable("result", businessInterface.doBusiness());
  }

}
