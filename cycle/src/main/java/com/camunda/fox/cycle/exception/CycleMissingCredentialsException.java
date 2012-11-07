package com.camunda.fox.cycle.exception;


public class CycleMissingCredentialsException extends CycleException {

  private static final long serialVersionUID = 1L;
  
  public CycleMissingCredentialsException() {
    super();
  }

  public CycleMissingCredentialsException(String message, Throwable cause) {
    super(message, cause);
  }

  public CycleMissingCredentialsException(String message) {
    super(message);
  }

  public CycleMissingCredentialsException(Throwable cause) {
    super(cause);
  }

}
