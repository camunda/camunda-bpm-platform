package com.camunda.fox.webapp.faces.exception;

public class TaskListAuthenticationFailedException extends RuntimeException {

  private static final long serialVersionUID = 1L;

  public TaskListAuthenticationFailedException(String message) {
    super(message);
  }

}
