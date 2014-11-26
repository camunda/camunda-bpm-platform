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

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import org.camunda.bpm.engine.rest.dto.ExceptionDto;
import org.codehaus.jackson.JsonParseException;

/**
 * @author Thorben Lindhauer
 *
 */
@Provider
public class JsonParseExceptionHandler implements ExceptionMapper<JsonParseException> {

  @Override
  public Response toResponse(JsonParseException exception) {
    ExceptionDto dto = ExceptionDto.fromException(exception);
    return Response.status(Status.BAD_REQUEST).entity(dto).type(MediaType.APPLICATION_JSON_TYPE).build();
  }
}
