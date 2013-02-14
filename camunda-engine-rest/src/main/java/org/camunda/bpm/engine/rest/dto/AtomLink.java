package org.camunda.bpm.engine.rest.dto;


public class AtomLink {

  private String href;
  private String rel;
  
  public AtomLink(String rel, String href) {
    this.href = href;
    this.rel = rel;
  }
  
  public String getHref() {
    return href;
  }
  
  public void setHref(String href) {
    this.href = href;
  }
  
  public String getRel() {
    return rel;
  }
  
  public void setRel(String rel) {
    this.rel = rel;
  }
  
}
