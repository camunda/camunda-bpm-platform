package org.camunda.bpm.engine.rest.exception;

public class RestException extends RuntimeException {
  
  private static final long serialVersionUID = 1L;
  
  public RestException(String string) {
    super(string);
  }
}
