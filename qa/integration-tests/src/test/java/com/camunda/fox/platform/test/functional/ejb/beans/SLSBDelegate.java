package com.camunda.fox.platform.test.functional.ejb.beans;

import javax.ejb.Stateless;
import javax.inject.Named;

import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.JavaDelegate;

/**
 * A SLSB acting as a {@link JavaDelegate}
 * 
 * @author Daniel Meyer
 *
 */
@Named("SLSBDelegate")
@Stateless
public class SLSBDelegate implements JavaDelegate {

  public void execute(DelegateExecution execution) throws Exception {
    execution.setVariable(getClass().getName(), true);    
  }

}
