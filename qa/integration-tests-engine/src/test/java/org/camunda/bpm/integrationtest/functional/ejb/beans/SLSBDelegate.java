package org.camunda.bpm.integrationtest.functional.ejb.beans;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;

import javax.ejb.Stateless;
import javax.inject.Named;

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
    execution.setVariable(SLSBDelegate.class.getName(), true);
  }

}
