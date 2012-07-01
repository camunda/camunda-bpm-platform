package com.camunda.fox.platform.test.functional.ejb.local.bean;

import javax.ejb.EJB;
import javax.inject.Named;

import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.JavaDelegate;

/**
 * A CDI bean delegating to the local business 
 * interface of a SLSB from a different application.
 * 
 * @author Daniel Meyer
 *
 */
@Named
public class LocalSLSBClientDelegateBean implements JavaDelegate {
  
  @EJB(lookup="java:global/service/LocalSLSBean!com.camunda.fox.platform.test.functional.ejb.local.bean.BusinessInterface")
  private BusinessInterface businessInterface;

  @Override
  public void execute(DelegateExecution execution) throws Exception {
    execution.setVariable("result", businessInterface.doBusiness());
  }

}
