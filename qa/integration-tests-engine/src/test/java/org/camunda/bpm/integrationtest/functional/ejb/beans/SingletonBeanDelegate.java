package org.camunda.bpm.integrationtest.functional.ejb.beans;

import javax.ejb.Singleton;
import javax.inject.Named;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;


@Named("SingletonBeanDelegate")
@Singleton
public class SingletonBeanDelegate implements JavaDelegate {

  public void execute(DelegateExecution execution) throws Exception {
    execution.setVariable(getClass().getName(), true);    
  }

}
