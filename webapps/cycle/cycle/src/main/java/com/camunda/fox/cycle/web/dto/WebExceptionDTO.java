package com.camunda.fox.cycle.web.dto;

import java.io.PrintWriter;
import java.io.StringWriter;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import com.camunda.fox.web.WebException;

/**
 * Data object for exceptions being raised by an application
 * @author nico.rehwaldt
 */
public class WebExceptionDTO {
  
  private String message;
  private String cause;
  
  private String stacktrace;
  private Status status;

  public WebExceptionDTO() {}
  
  public WebExceptionDTO(Response.Status status, String message, Throwable cause) {
    this.message = message;
    
    if (cause != null) {
      this.cause = cause.getClass().getName();

      StringWriter writer = new StringWriter();
      PrintWriter printer = new PrintWriter(writer);
      cause.printStackTrace(printer);
      printer.flush();
      
      this.stacktrace = writer.toString();
    }
    
    this.status = status;
  }

  public String getMessage() {
    return message;
  }

  public String getCause() {
    return cause;
  }

  public String getStacktrace() {
    return stacktrace;
  }

  public Status getStatus() {
    return status;
  }
  
  public static WebExceptionDTO wrap(WebException exception) {
    return new WebExceptionDTO(exception.getStatus(), exception.getMessage(), exception.getCause());
  }
  
  public static WebExceptionDTO wrap(Exception exception, Status status) {
    return new WebExceptionDTO(status, exception.getMessage(), exception.getCause());
  }
  
}
