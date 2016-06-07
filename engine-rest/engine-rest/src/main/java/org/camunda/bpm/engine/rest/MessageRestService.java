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
package org.camunda.bpm.engine.rest;

import java.util.List;
import org.camunda.bpm.engine.rest.dto.message.CorrelationMessageDto;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import org.camunda.bpm.engine.rest.dto.message.MessageCorrelationResultDto;

@Produces(MediaType.APPLICATION_JSON)
public interface MessageRestService {

  public static final String PATH = "/message";
  public static final String PATH_CORRELATION = "/correlation";
  public static final String PATH_CORRELATION_WITH_RESULT = "/correlationWithResult";

  @POST
  @Path(PATH_CORRELATION)
  @Consumes(MediaType.APPLICATION_JSON)
  void deliverMessage(CorrelationMessageDto messageDto);

  @POST
  @Path(PATH_CORRELATION_WITH_RESULT)
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  List<MessageCorrelationResultDto> correlateMessageWithResult(CorrelationMessageDto messageDto);

}
