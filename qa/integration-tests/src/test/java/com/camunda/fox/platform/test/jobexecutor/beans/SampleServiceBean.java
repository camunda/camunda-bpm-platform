package com.camunda.fox.platform.test.jobexecutor.beans;

import javax.enterprise.context.ApplicationScoped;
import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.JavaDelegate;

/**
 *
 * @author nico.rehwaldt
 */
@ApplicationScoped
public class SampleServiceBean implements JavaDelegate {

  private boolean called = false;
  
  @Override
  public void execute(DelegateExecution execution) throws Exception {
    called = true;
  }

  public boolean isCalled() {
    return called;
  }
}
