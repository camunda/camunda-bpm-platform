package org.camunda.bpm.cycle.web.dto;


public class ExceptionDTO {

  private String message;
  private String exceptionType;

  public ExceptionDTO() {} 
  
  public ExceptionDTO(Exception e) {
    this.message = e.getMessage();
    this.exceptionType = e.getClass().getName();
  }
  
  public String getMessage() {
    return message;
  }
  
  public String getExceptionType() {
    return exceptionType;
  }
}
