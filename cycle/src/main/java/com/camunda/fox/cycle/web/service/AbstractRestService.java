package com.camunda.fox.cycle.web.service;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import com.sun.jersey.core.spi.factory.ResponseBuilderImpl;

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
    
    return createWebApplicationException(message, Response.Status.NOT_FOUND);
  }

  /**
   * Issue a bad request exception with the given reason
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
  
  protected ResponseBuilderImpl createResponse() {
    // automatic detection of the JAX-RS implementation is broken on WAS 8.5.
    return new ResponseBuilderImpl();
  }
}
