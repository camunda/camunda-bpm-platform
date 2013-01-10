package org.camunda.bpm.engine.rest.exception;

public class InvalidRequestException extends RestException {

  private static final long serialVersionUID = 1L;

  public InvalidRequestException(String string) {
    super(string);
  }
}
