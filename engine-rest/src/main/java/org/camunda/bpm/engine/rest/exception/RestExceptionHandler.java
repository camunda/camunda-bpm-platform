package org.camunda.bpm.engine.rest.exception;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.ws.rs.core.MediaType;
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

  private static final Logger LOGGER = Logger.getLogger(ExceptionHandler.class.getSimpleName());

  @Override
  public Response toResponse(RestException exception) {
    ExceptionDto dto = ExceptionDto.fromException(exception);

    LOGGER.log(Level.WARNING, getStackTrace(exception));
    
    if (exception.getStatus() != null) {
      return Response.status(exception.getStatus()).entity(dto).type(MediaType.APPLICATION_JSON_TYPE).build();
    } else {
      return Response.serverError().entity(dto).type(MediaType.APPLICATION_JSON_TYPE).build();
    }
  }
  
  protected String getStackTrace(Throwable aThrowable) {
    final Writer result = new StringWriter();
    final PrintWriter printWriter = new PrintWriter(result);
    aThrowable.printStackTrace(printWriter);
    return result.toString();
  }
}
