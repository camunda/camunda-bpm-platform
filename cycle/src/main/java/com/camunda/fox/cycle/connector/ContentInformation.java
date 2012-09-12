package com.camunda.fox.cycle.connector;

import java.io.Serializable;
import java.util.Date;


public class ContentInformation implements Serializable {
  private static final long serialVersionUID = 1L;

  boolean isAvailable;
  Date lastModified;
  
  public ContentInformation(boolean isAvailable, Date lastModified) {
    super();
    this.isAvailable = isAvailable;
    this.lastModified = lastModified;
  }

  public boolean isAvailable() {
    return isAvailable;
  }
  
  public void setAvailable(boolean isAvailable) {
    this.isAvailable = isAvailable;
  }
  
  public Date getLastModified() {
    return lastModified;
  }
  
  public void setLastModified(Date lastModified) {
    this.lastModified = lastModified;
  }
  
}
