package org.camunda.bpm.spring.boot.starter;

import org.springframework.core.NestedRuntimeException;

/**
 * Custom runtime exception that wraps a checked exception.
 * <p>
 * This class can be used when it is necessary to avoid the explicit throwing of checked exceptions.
 * </p>
 */
public class CamundaBpmNestedRuntimeException extends NestedRuntimeException {

  private static final long serialVersionUID = 1L;

  /**
   * Construct an exception with the specified detail message and nested exception.
   * 
   * @param msg the exception message
   */
  public CamundaBpmNestedRuntimeException(final String msg) {
    super(msg);
  }

  /**
   * Construct an exception with the specified detail message and nested exception.
   * 
   * @param msg the exception message
   * @param cause the nested exception
   */
  public CamundaBpmNestedRuntimeException(final String msg, final Throwable cause) {
    super(msg, cause);
  }

}
