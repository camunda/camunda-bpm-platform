package org.camunda.bpm.pa.service;

import java.io.Serializable;

public class CockpitVariable implements Serializable {
  
  private static final long serialVersionUID = 1L;
  
  protected String name;
  protected String value;
  
  public CockpitVariable(String name, String value) {
    this.name = name;
    this.value = value;
  }

  public String getName() {
    return name;
  }

  public String getValue() {
    return value;
  }
  
}
