package org.camunda.bpm.engine.rest.dto;

import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.core.UriInfo;

public abstract class LinkableDto {

  protected List<AtomLink> links = new ArrayList<AtomLink>();
  
  public List<AtomLink> getLinks() {
    return links;
  }
  
  public void addLink(AtomLink link) {
    links.add(link);
  }
  
  public void addReflexiveLink(UriInfo context, String action, String relation) {
    AtomLink link = generateLink(context, action, relation);
    links.add(link);
  }
  
  public abstract AtomLink generateLink(UriInfo context, String action, String relation);
  
}
