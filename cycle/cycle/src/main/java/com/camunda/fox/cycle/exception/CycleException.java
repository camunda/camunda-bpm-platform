package com.camunda.fox.cycle.exception;

public class CycleException extends RuntimeException {

  private static final long serialVersionUID = 1L;

  public CycleException() {
    super();
  }

  public CycleException(String message, Throwable cause) {
    super(message, cause);
  }

  public CycleException(String message) {
    super(message);
  }

  public CycleException(Throwable cause) {
    super(cause);
  }
  
  @Override
  public String getMessage() {
    if(getCause() != null) {
      return super.getMessage() + " - " + getCause().getMessage();
    } else {
      return super.getMessage();
    }
  }
}
