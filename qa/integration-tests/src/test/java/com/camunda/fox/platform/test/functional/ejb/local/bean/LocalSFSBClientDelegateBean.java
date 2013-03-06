package com.camunda.fox.platform.test.functional.ejb.local.bean;

import javax.ejb.EJB;
import javax.inject.Named;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;

/**
 * A CDI bean delegating to the local business 
 * interface of a SFSB from a different application.
 * 
 * @author Daniel Meyer
 *
 */
@Named
public class LocalSFSBClientDelegateBean implements JavaDelegate {
  
  @EJB(lookup="java:global/service/LocalSFSBean!com.camunda.fox.platform.test.functional.ejb.local.bean.BusinessInterface")
  private BusinessInterface businessInterface;

  @Override
  public void execute(DelegateExecution execution) throws Exception {
    execution.setVariable("result", businessInterface.doBusiness());
  }

}
