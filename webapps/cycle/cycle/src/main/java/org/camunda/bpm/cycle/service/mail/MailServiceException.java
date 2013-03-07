package org.camunda.bpm.cycle.service.mail;

import org.camunda.bpm.cycle.exception.CycleException;


public class MailServiceException extends CycleException {
  
  private static final long serialVersionUID = 1L;

  public MailServiceException() {
    super();
  }

  public MailServiceException(String message, Throwable cause) {
    super(message, cause);
  }

  public MailServiceException(String message) {
    super(message);
  }

  public MailServiceException(Throwable cause) {
    super(cause);
  }
  
}
