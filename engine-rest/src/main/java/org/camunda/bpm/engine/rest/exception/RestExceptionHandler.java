package org.camunda.bpm.engine.rest.exception;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import org.camunda.bpm.engine.rest.dto.ExceptionDto;

/**
 * Translates {@link RestException}s to error responses according to the status that is set in the exception.
 * Response content format: <code>{"type" : "ExceptionType", "message" : "some exception message"}</code>
 * 
 * @author Thorben Lindhauer
 */
@Provider
public class RestExceptionHandler implements ExceptionMapper<RestException> {

  @Override
  public Response toResponse(RestException exception) {
    ExceptionDto dto = ExceptionDto.fromException(exception, true);

    Logger.getLogger(ExceptionHandler.class.getSimpleName()).log(Level.WARNING, getStackTrace(exception));
    
    if (exception.getStatus() != null) {
      return Response.status(exception.getStatus()).entity(dto).build();
    } else {
      return Response.serverError().entity(dto).build();
    }
  }
  
  protected String getStackTrace(Throwable aThrowable) {
    final Writer result = new StringWriter();
    final PrintWriter printWriter = new PrintWriter(result);
    aThrowable.printStackTrace(printWriter);
    return result.toString();
  }
}
