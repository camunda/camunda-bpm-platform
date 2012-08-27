package com.camunda.fox.cycle.web.jaxrs.ext;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import org.thymeleaf.exceptions.TemplateInputException;

/**
 * Maps thymeleaf generated exceptions to their respective http error codes
 * 
 * @author nico.rehwaldt
 */
@Provider
public class TemplateExceptionMapper implements ExceptionMapper<TemplateInputException> {

  @Override
  public Response toResponse(TemplateInputException exception) {
    return Response.status(Response.Status.NOT_FOUND).entity("error/not-found").build();
  }
}
