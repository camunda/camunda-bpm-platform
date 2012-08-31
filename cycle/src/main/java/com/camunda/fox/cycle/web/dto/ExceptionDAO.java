package com.camunda.fox.cycle.web.dto;


public class ExceptionDAO {

  private String message;
  private String exceptionType;

  public ExceptionDAO() {} 
  
  public ExceptionDAO(Exception e) {
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
