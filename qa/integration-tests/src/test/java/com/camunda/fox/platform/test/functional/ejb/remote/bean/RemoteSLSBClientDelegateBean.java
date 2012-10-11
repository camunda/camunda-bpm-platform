package com.camunda.fox.platform.test.functional.ejb.remote.bean;

import javax.ejb.EJB;
import javax.inject.Named;

import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.JavaDelegate;

import com.camunda.fox.platform.test.util.JndiConstants;

/**
 * A CDI bean delegating to the remote business 
 * interface of a SLSB from a different application.
 * 
 * @author Daniel Meyer
 *
 */
@Named
public class RemoteSLSBClientDelegateBean implements JavaDelegate {
  
  @EJB(lookup="java:global/" +
          JndiConstants.APP_NAME +
          "service/" + 
          "RemoteSLSBean!com.camunda.fox.platform.test.functional.ejb.remote.bean.BusinessInterface")
  private BusinessInterface businessInterface;

  @Override
  public void execute(DelegateExecution execution) throws Exception {
    execution.setVariable("result", businessInterface.doBusiness());
  }

}
