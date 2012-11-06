package com.camunda.fox.cycle.web.service;

import javax.ws.rs.WebApplicationException;
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
  
  /**
   * Issue a not found response with the given reason
   * 
   * @param message
   * @return 
   */
  protected WebApplicationException notFound(String message) {
    
    return createWebApplicationException(message, Response.Status.FORBIDDEN);
  }

  /**
   * Issue a bad request exception with the given reson
   * 
   * @param message
   * @return 
   */
  protected WebApplicationException badRequest(String message) {
    return createWebApplicationException(message, Response.Status.BAD_REQUEST);
  }
  
  /**
   * Issue a not found response with the given reason
   * 
   * @param message
   * @return 
   */
  protected WebApplicationException notAllowed(String message) {
    return createWebApplicationException(message, Response.Status.FORBIDDEN);
  }

  private WebApplicationException createWebApplicationException(String message, Response.Status status) {
    Response response = Response.status(status).entity(message).build();
    return new WebApplicationException(response);
  }
}
