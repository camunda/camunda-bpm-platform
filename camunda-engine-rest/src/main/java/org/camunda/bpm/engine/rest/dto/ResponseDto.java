package org.camunda.bpm.engine.rest.dto;

import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.core.UriInfo;

public abstract class ResponseDto {

  protected List<AtomLink> links = new ArrayList<AtomLink>();
  
  public List<AtomLink> getLinks() {
    return links;
  }
  
  public abstract void addLink(UriInfo context, String action, String relation);
  
}
