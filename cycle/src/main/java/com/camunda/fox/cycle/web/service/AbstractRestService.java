package com.camunda.fox.cycle.web.service;

import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

/**
 * This is the base class used by all rest controllers and encapsulates shared behavior.
 * 
 * @author nico.rehwaldt
 */
public class AbstractRestService {

  @Context 
  private UriInfo uriInfo;
  
  /**
   * Redirect to the specific url (relative to the base url)
   * @param uri
   * @return 
   */
  protected Response redirectTo(String uri) {
    return Response.seeOther(uriInfo.getBaseUriBuilder().path(uri).build()).build();
  }
}
