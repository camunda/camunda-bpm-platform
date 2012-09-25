package com.camunda.fox.cycle.connector;

import java.io.Serializable;
import java.util.Date;


public class ContentInformation implements Serializable {
  private static final long serialVersionUID = 1L;

  private boolean isAvailable;
  private Date lastModified;
  
  public ContentInformation(boolean isAvailable, Date lastModified) {
    super();
    this.isAvailable = isAvailable;
    this.lastModified = lastModified;
  }

  public boolean exists() {
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
  
  /**
   * Standard {@link ContentInformation} for not found files.
   * 
   * @return 
   */
  public static ContentInformation notFound() {
    return new ContentInformation(false, null);
  }
}
