package com.camunda.fox.cycle.web.jaxrs.ext;

import com.camunda.fox.cycle.web.dto.WebExceptionDTO;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import com.camunda.fox.web.WebException;

/**
 *
 * @author nico.rehwaldt
 */
@Provider
public class WebExceptionMapper implements ExceptionMapper<WebException> {

  @Override
  public Response toResponse(WebException exception) {
    return Response.status(exception.getStatus()).entity(WebExceptionDTO.wrap(exception)).build();
  }
}
