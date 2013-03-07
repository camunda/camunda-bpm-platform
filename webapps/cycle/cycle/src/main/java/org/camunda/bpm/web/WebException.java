package org.camunda.bpm.web;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

/**
 *
 * @author nico.rehwaldt
 */
public class WebException extends RuntimeException {
  
  private static final long serialVersionUID = 1L;
  
  private final Status status;

  public WebException(String message) {
    this(message, null);
  }
  
  public WebException(Throwable cause) {
    this(null, cause);
  }
  
  public WebException(String message, Throwable cause) {
    this(Response.Status.INTERNAL_SERVER_ERROR, message, cause);
  }
  
  public WebException(Response.Status status, String message, Throwable cause) {
    super(message, cause);
    
    this.status = status;
  }

  public Status getStatus() {
    return status;
  }
}
