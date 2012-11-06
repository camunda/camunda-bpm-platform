package com.camunda.fox.security;

/**
 * 
 * @author nico.rehwaldt
 */
public class MissingPrivilegesException extends SecurityException {

  public MissingPrivilegesException(String message) {
    super(message);
  }

  public MissingPrivilegesException(Throwable cause) {
    super(cause);
  }

  public MissingPrivilegesException(String message, Throwable cause) {
    super(message, cause);
  }

}
