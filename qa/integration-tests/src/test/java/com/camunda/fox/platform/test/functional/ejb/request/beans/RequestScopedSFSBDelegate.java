package com.camunda.fox.platform.test.functional.ejb.request.beans;

import javax.ejb.Stateful;
import javax.enterprise.context.RequestScoped;
import javax.inject.Named;

import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.JavaDelegate;

/**
 * A request scoped Stateful Session Bean acting as as a JavaDelegate
 * 
 * @author Daniel Meyer
 * 
 */
@Named
@Stateful
@RequestScoped
public class RequestScopedSFSBDelegate implements JavaDelegate {

  private int invocationCounter = 0;

  public void execute(DelegateExecution execution) throws Exception {
    invocationCounter++;
    execution.setVariable("invocationCounter", invocationCounter);
  }

}
