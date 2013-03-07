package org.camunda.bpm.security;

/**
 * 
 * @author nico.rehwaldt
 */
public class UnauthorizedException extends SecurityException {
  
  private static final long serialVersionUID = 1L;

  public UnauthorizedException(String message) {
    super(message);
  }

  public UnauthorizedException(Throwable cause) {
    super(cause);
  }

  public UnauthorizedException(String message, Throwable cause) {
    super(message, cause);
  }

}
