package org.camunda.bpm.engine.rest.dto;

import javax.xml.bind.annotation.XmlElement;

public class AtomLink {

  private String href;
  private String rel;
  
  public AtomLink(String rel, String href) {
    this.href = href;
    this.rel = rel;
  }
  
  @XmlElement
  public String getHref() {
    return href;
  }
  
  public void setHref(String href) {
    this.href = href;
  }
  
  @XmlElement
  public String getRel() {
    return rel;
  }
  
  public void setRel(String rel) {
    this.rel = rel;
  }
  
  public void linkTo(ResponseDto object, String relationName) {
    
  }
  
  
}
