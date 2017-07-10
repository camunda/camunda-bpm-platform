/* Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.camunda.bpm.engine.rest.exception;

import org.camunda.bpm.engine.AuthorizationException;
import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.rest.dto.ExceptionDto;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * <p>Translates any {@link ProcessEngineException} to a HTTP 500 error and a JSON response.
 * Response content format: <code>{"type" : "ExceptionType", "message" : "some exception message"}</code>
 * </p>
 *
 * <p>Provides dedicated exception handling for {@link AuthorizationException AuthorizationExceptions}:
 * The status code is always set to 403, "Forbidden" and details about the requested resource and
 * violated permission are added to the response body</p>
 *
 *
 * @author Thorben Lindhauer
 * @author Daniel Meyer
 */
@Provider
public class ProcessEngineExceptionHandler implements ExceptionMapper<ProcessEngineException> {

  private static final Logger LOGGER = Logger.getLogger(ExceptionHandler.class.getSimpleName());

  public Response toResponse(ProcessEngineException exception) {

    LOGGER.log(Level.WARNING, getStackTrace(exception));

    Status responseStatus = ExceptionHandlerHelper.getInstance().getStatus(exception);

    ExceptionDto exceptionDto = ExceptionHandlerHelper.getInstance().fromException(exception);

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
