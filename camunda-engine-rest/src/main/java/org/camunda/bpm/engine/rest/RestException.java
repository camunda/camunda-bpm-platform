package org.camunda.bpm.engine.rest;

public class RestException extends RuntimeException {

  public RestException(String string) {
    super(string);
  }

  private static final long serialVersionUID = 1L;

}
