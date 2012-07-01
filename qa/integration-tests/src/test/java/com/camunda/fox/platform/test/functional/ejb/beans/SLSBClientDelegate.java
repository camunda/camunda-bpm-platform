package com.camunda.fox.platform.test.functional.ejb.beans;

import javax.ejb.EJB;
import javax.inject.Named;

import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.JavaDelegate;

/**
 * This is a CDI bean delegating to a SLSB from the same deployment
 * 
 * @author Daniel Meyer
 *
 */
@Named("SLSBClientDelegate")
public class SLSBClientDelegate implements JavaDelegate {
  
  @EJB
  private JavaDelegate bean;

  public void execute(DelegateExecution execution) throws Exception {
    bean.execute(execution);
  }

}
