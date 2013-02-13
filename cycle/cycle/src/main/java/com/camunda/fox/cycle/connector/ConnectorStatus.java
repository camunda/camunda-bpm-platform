package com.camunda.fox.cycle.connector;

/**
 * A information about the runtime state of a connector
 * 
 * @author nico.rehwaldt
 */
public class ConnectorStatus {
  
  public enum State {
    OK, 
    IN_ERROR
  }

  private State status;
  private Exception exception;

  public ConnectorStatus(State status, Exception exception) {
    this.status = status;
    this.exception = exception;
  }

  public State getState() {
    return status;
  }

  public Exception getException() {
    return exception;
  }

  // static helpers //////////////////////////////////////////

  public static ConnectorStatus ok() {
    return new ConnectorStatus(State.OK, null);
  }
  
  public static ConnectorStatus inError(Exception e) {
    return new ConnectorStatus(State.IN_ERROR, e);
  }
}
