package com.camunda.fox.cycle.web.dto;

import java.io.PrintWriter;
import java.io.StringWriter;

import com.camunda.fox.cycle.connector.*;
import com.camunda.fox.cycle.connector.ConnectorStatus.State;

/**
 * Container object for a {@link ConnectorStatus}. 
 * 
 * @author nico.rehwaldt
 */
public class ConnectorStatusDTO {

  private State status;
  private String exceptionMessage;

  private String message;
  
  public ConnectorStatusDTO(State status, Exception exception) {
    this.status = status;
    if (exception != null) {
      this.message = exception.getMessage();
      this.exceptionMessage = buildExceptionMessage(exception);
    }
  }

  private ConnectorStatusDTO(ConnectorStatus status) {
    this(status.getState(), status.getException());
  }

  private String buildExceptionMessage(Exception exception) {
    StringWriter writer = new StringWriter();
    
    exception.printStackTrace(new PrintWriter(writer));
    
    return writer.toString();
  }

  public State getState() {
    return status;
  }

  public String getMessage() {
    return message;
  }

  
  public String getExceptionMessage() {
    return exceptionMessage;
  }

  // static helpers //////////////////////////////////////////

  public static ConnectorStatusDTO wrap(ConnectorStatus status) {
    return new ConnectorStatusDTO(status);
  }
}
