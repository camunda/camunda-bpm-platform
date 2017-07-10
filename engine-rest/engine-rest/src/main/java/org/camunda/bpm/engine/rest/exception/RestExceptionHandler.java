package org.camunda.bpm.engine.rest.exception;

import org.camunda.bpm.engine.rest.dto.ExceptionDto;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.util.logging.Level;
import java.util.logging.Logger;

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

    Response.Status responseStatus = ExceptionHandlerHelper.getInstance().getStatus(exception);
    ExceptionDto exceptionDto = ExceptionHandlerHelper.getInstance().fromException(exception);

    LOGGER.log(Level.WARNING, getStackTrace(exception));

    return Response
      .status(responseStatus)
      .entity(exceptionDto)
      .type(MediaType.APPLICATION_JSON_TYPE)
      .build();
  }
  
  protected String getStackTrace(Throwable aThrowable) {
    final Writer result = new StringWriter();
    final PrintWriter printWriter = new PrintWriter(result);
    aThrowable.printStackTrace(printWriter);
    return result.toString();
  }
}
