package org.camunda.bpm.engine.rest.exception;

import org.camunda.bpm.engine.rest.dto.ExceptionDto;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Christopher Zell <christopher.zell@camunda.com>
 */
public class ClientErrorExceptionHandler implements ExceptionMapper<WebApplicationException> {

  private static final Logger LOGGER = Logger.getLogger(ClientErrorExceptionHandler.class.getSimpleName());

  @Override
  public Response toResponse(WebApplicationException exception) {
    ExceptionDto dto = ExceptionDto.fromException(exception);
    LOGGER.log(Level.WARNING, getStackTrace(exception));

    Response response = exception.getResponse();
    Response.ResponseBuilder builder;
    if (response == null) {
      builder = Response.status(Response.Status.BAD_REQUEST);
    } else {
      builder = Response.status(response.getStatus());
    }
    return builder.entity(dto).type(MediaType.APPLICATION_JSON_TYPE).build();
  }

  protected String getStackTrace(Throwable aThrowable) {
    final Writer result = new StringWriter();
    final PrintWriter printWriter = new PrintWriter(result);
    aThrowable.printStackTrace(printWriter);
    return result.toString();
  }
}
