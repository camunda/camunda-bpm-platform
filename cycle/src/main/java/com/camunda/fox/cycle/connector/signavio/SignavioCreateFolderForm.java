package com.camunda.fox.cycle.connector.signavio;

import javax.ws.rs.FormParam;


public class SignavioCreateFolderForm {

  @FormParam("name")
  private String name;
  
  @FormParam("description")
  private String description;
  
  @FormParam("parent")
  private String parent;
  
  public SignavioCreateFolderForm(String name, String description, String parent) {
    this.name = name;
    this.description = description;
    this.parent = "/directory" + parent;
  }
  
  public String getName() {
    return name;
  }

  public String getDescription() {
    return description;
  }

  public String getParent() {
    return parent;
  }

}
