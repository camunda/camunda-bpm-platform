package com.camunda.fox.cycle.impl.connector.signavio;

import javax.ws.rs.FormParam;


public class SignavioRequestForm {
  
  @FormParam("x-signavio-id")
  private String xSignavioId;
  
  public String getXSignavioId() {
    return this.xSignavioId;
  }
  
//  public 

}
