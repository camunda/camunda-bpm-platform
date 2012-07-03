package com.camunda.fox.platform.test.functional.ejb.beans;

import javax.ejb.Singleton;
import javax.inject.Named;

import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.JavaDelegate;


@Named("SingletonBeanDelegate")
@Singleton
public class SingletonBeanDelegate implements JavaDelegate {

  public void execute(DelegateExecution execution) throws Exception {
    execution.setVariable(getClass().getName(), true);    
  }

}
