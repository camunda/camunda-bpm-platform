package org.camunda.bpm.security;

/**
 *
 * @author nico.rehwaldt
 */
public class SecurityException extends RuntimeException {
  
  private static final long serialVersionUID = 1L;

  public SecurityException(String message) {
    super(message);
  }

  public SecurityException(Throwable cause) {
    super(cause);
  }

  public SecurityException(String message, Throwable cause) {
    super(message, cause);
  }
}
