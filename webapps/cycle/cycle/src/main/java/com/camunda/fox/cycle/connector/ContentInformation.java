package com.camunda.fox.cycle.connector;

import java.io.Serializable;
import java.util.Date;

import com.camunda.fox.cycle.util.DateUtil;


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

  /**
   * Returns last modified date normalized to seconds accuracy.
   */
  public Date getLastModified() {
    if(lastModified != null) {
      return DateUtil.getNormalizedDate(lastModified);
    } else {
      return null;
    }
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
