package org.camunda.bpm.cycle.connector.crypt;


public class EncryptionException extends RuntimeException {

  private static final long serialVersionUID = 1L;

  public EncryptionException(String format, Exception e) {
    super(format, e);
  }

}
