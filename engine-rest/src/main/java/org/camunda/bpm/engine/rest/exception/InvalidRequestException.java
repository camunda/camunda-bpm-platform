package org.camunda.bpm.engine.rest.exception;

/**
 * This exception is used for any kind of errors that occur due to malformed
 * parameters in a Http query.
 * 
 * @author Thorben Lindhauer
 * 
 */
public class InvalidRequestException extends RuntimeException {

  private static final long serialVersionUID = 1L;

  public InvalidRequestException(String string) {
    super(string);
  }
}
