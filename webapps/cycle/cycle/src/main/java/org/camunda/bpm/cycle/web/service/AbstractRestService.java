package org.camunda.bpm.cycle.web.service;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriInfo;

import org.camunda.bpm.cycle.web.jaxrs.ext.JaxRsUtil;


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
    return JaxRsUtil.createResponse().status(Status.SEE_OTHER).location(uriInfo.getBaseUriBuilder().path(uri).build()).build();
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
  
  /**
   * Issue a internal server error response with the given reason
   * 
   * @param message
   * @return 
   */
  protected WebApplicationException internalServerError(String message) {
    return createWebApplicationException(message, Response.Status.INTERNAL_SERVER_ERROR);
  }

  private WebApplicationException createWebApplicationException(String message, Response.Status status) {
    ResponseBuilder responseBuilder = JaxRsUtil.createResponse().status(status);
    if (message == null) {
      responseBuilder.entity(message);
    }
    Response response = responseBuilder.build();
    return new WebApplicationException(response);
  }
  
}
