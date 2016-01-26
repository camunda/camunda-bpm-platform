package org.camunda.bpm.pa.service;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class CockpitVariable implements Serializable {

  private static final long serialVersionUID = 1L;

  protected String name;
  protected String value;
  protected List<Date> dates = new ArrayList<Date>();

  public CockpitVariable() {
  }

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

  public void setName(String name) {
    this.name = name;
  }

  public void setValue(String value) {
    this.value = value;
  }

  public List<Date> getDates() {
    return dates;
  }

  public void setDates(List<Date> dates) {
    this.dates = dates;
  }

}
