package com.camunda.fox.cycle.web.jaxrs.ext;

import java.util.logging.Level;
import java.util.logging.Logger;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;

import org.thymeleaf.exceptions.TemplateInputException;

/**
 * Maps thymeleaf generated exceptions to their respective http error codes
 * 
 * @author nico.rehwaldt
 */
//@Provider
public class TemplateExceptionMapper implements ExceptionMapper<TemplateInputException> {

  @Override
  public Response toResponse(TemplateInputException exception) {
    Logger.getLogger(getClass().getSimpleName()).log(Level.SEVERE, "Error during JAX-RS Request", exception);
    return Response.status(Response.Status.NOT_FOUND).entity("error/not-found").build();
  }
}
