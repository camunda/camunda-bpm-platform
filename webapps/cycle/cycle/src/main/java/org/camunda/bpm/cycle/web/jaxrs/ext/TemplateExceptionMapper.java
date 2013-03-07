package org.camunda.bpm.cycle.web.jaxrs.ext;

import java.util.logging.Level;
import java.util.logging.Logger;

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
    Logger.getLogger(getClass().getSimpleName()).log(Level.SEVERE, "Exception occured during request", exception);
    if (isNotFound(exception)) {
      return JaxRsUtil.createResponse().status(Response.Status.NOT_FOUND).entity("tpl:error/not-found").build();
    } else {
      // rethrow
      throw exception;
    }
  }

  private boolean isNotFound(TemplateInputException e) {
    return e.getMessage().contains("Error resolving template");
  }

}
