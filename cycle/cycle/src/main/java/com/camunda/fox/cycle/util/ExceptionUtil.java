package com.camunda.fox.cycle.util;

/**
 * Util to work with exceptions.
 * 
 * @author roman.smirnov
 *
 */
public class ExceptionUtil {
  
  public static Throwable getRootCause(Throwable t) {
    if (t.getCause() != null) {
      return getRootCause(t.getCause());
    }
    return t;
  }

}
