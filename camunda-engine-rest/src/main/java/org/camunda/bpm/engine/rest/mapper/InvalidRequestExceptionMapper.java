package org.camunda.bpm.engine.rest.mapper;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import org.camunda.bpm.engine.rest.exception.InvalidRequestException;

@Provider
public class InvalidRequestExceptionMapper implements
    ExceptionMapper<InvalidRequestException> {

  @Override
  public Response toResponse(InvalidRequestException exception) {
    return Response.status(Status.BAD_REQUEST).build();
  }

}
