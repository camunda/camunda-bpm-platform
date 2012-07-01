package com.camunda.fox.platform.test.functional.ejb.beans;

import javax.ejb.Stateless;
import javax.inject.Named;

import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.JavaDelegate;


@Named("SFSBDelegate")
@Stateless
public class SFSBDelegate implements JavaDelegate {

  public void execute(DelegateExecution execution) throws Exception {
    execution.setVariable(getClass().getName(), true);    
  }

}