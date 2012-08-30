package com.camunda.fox.cycle.connector;

import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import com.camunda.fox.cycle.exception.RepositoryException;
import com.camunda.fox.cycle.web.dto.ExceptionDAO;

@Provider
@Produces(MediaType.APPLICATION_JSON)
public class ConnectorExceptionMapper implements ExceptionMapper<RepositoryException> {

  @Override
  public Response toResponse(RepositoryException e) {
    return Response.serverError().entity(new ExceptionDAO(e)).build();
  }

}
