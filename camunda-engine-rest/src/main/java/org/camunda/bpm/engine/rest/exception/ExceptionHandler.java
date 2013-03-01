package org.camunda.bpm.engine.rest.exception;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import org.camunda.bpm.engine.rest.dto.ExceptionDto;

/**
 *
 * @author nico.rehwaldt
 */
@Provider
public class ExceptionHandler implements ExceptionMapper<Exception> {

  @Override
  public Response toResponse(Exception exception) {
    ExceptionDto dto = ExceptionDto.fromException(exception, true);

    System.out.println(exception.getCause());
    
    return Response.serverError().entity(dto).build();
  }
}
