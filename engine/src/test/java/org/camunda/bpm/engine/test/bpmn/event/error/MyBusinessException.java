package org.camunda.bpm.engine.test.bpmn.event.error;

public class MyBusinessException extends Exception {

  private static final long serialVersionUID = -8849430031097301135L;

  public MyBusinessException(String message) {
    super(message);
  }
}
