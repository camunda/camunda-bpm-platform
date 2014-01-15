package org.camunda.bpm.integrationtest.functional.ejb.beans;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;

import javax.ejb.Singleton;
import javax.inject.Named;


@Named("SingletonBeanDelegate")
@Singleton
public class SingletonBeanDelegate implements JavaDelegate {

  public void execute(DelegateExecution execution) throws Exception {
    execution.setVariable(SingletonBeanDelegate.class.getName(), true);
  }

}
