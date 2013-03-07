package org.camunda.bpm.cycle.exception;

import java.util.logging.Level;
import java.util.logging.Logger;

import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import org.camunda.bpm.cycle.web.dto.ExceptionDTO;
import org.camunda.bpm.cycle.web.jaxrs.ext.JaxRsUtil;


@Provider
@Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_OCTET_STREAM})
public class CycleExceptionMapper implements ExceptionMapper<CycleException> {

  @Override
  public Response toResponse(CycleException exception) {
    Logger.getLogger(getClass().getSimpleName()).log(Level.SEVERE, "Exception occurred during request", exception);
    return JaxRsUtil.createResponse()
            .status(Status.INTERNAL_SERVER_ERROR)
            .entity(new ExceptionDTO(exception))
            .type(MediaType.APPLICATION_JSON)
            .build();
  }
}
