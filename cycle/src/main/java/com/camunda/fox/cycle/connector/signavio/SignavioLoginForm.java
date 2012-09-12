package com.camunda.fox.cycle.connector.signavio;

import javax.ws.rs.FormParam;


public class SignavioLoginForm {

  @FormParam("name")
  private String name;
  
  @FormParam("password")
  private String password;
  
  @FormParam("tokenonly")
  private String tokenonly;
  
  public SignavioLoginForm(String name, String password, String tokenonly) {
    this.name = name;
    this.password = password;
    this.tokenonly = tokenonly;
  }
  
  public String getName() {
    return name;
  }

  public String getPassword() {
    return password;
  }
  
  public String getTokenonly() {
    return tokenonly;
  }
  
}
