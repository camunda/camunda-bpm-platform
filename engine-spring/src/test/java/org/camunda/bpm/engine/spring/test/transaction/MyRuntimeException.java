package org.camunda.bpm.engine.spring.test.transaction;

public class MyRuntimeException extends RuntimeException {
  
  public MyRuntimeException(){
    super("error");
  }

  private static final long serialVersionUID = 1L;

}
