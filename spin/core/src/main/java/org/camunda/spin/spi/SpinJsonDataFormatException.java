package org.camunda.spin.spi;

public class SpinJsonDataFormatException extends SpinDataFormatException {

  public SpinJsonDataFormatException(String message) {
    super(message);
  }
  
  public SpinJsonDataFormatException(String message, Throwable cause) {
    super(message, cause);
  }

  private static final long serialVersionUID = 1L;

}
