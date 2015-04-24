package org.camunda.bpm.engine.spring.test.transaction;


public class BeanWithException {

  public void doSomething(){
    throw new MyRuntimeException();
  }
}
