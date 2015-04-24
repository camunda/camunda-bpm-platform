package org.camunda.bpm.engine.spring.test.transaction;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.springframework.transaction.annotation.Transactional;


public class ServiceTaskBean implements JavaDelegate{

  private BeanWithException beanWithException;
  
  public void setBeanWithException(BeanWithException bean){
    this.beanWithException = bean;
  }

  @Transactional
  @Override
  public void execute(DelegateExecution execution) throws Exception {
    beanWithException.doSomething();
  }

}
