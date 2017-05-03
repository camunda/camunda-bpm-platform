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
package org.camunda.bpm.engine.rest.sub.repository;

import java.util.List;
import java.util.Map;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.camunda.bpm.engine.rest.dto.HistoryTimeToLiveDto;
import org.camunda.bpm.engine.rest.dto.VariableValueDto;
import org.camunda.bpm.engine.rest.dto.dmn.EvaluateDecisionDto;
import org.camunda.bpm.engine.rest.dto.repository.DecisionDefinitionDiagramDto;
import org.camunda.bpm.engine.rest.dto.repository.DecisionDefinitionDto;

public interface DecisionDefinitionResource {

  @GET
  @Produces(MediaType.APPLICATION_JSON)
  DecisionDefinitionDto getDecisionDefinition();

  @GET
  @Path("/xml")
  @Produces(MediaType.APPLICATION_JSON)
  DecisionDefinitionDiagramDto getDecisionDefinitionDmnXml();

  @GET
  @Path("/diagram")
  Response getDecisionDefinitionDiagram();

  @POST
  @Path("/evaluate")
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  List<Map<String, VariableValueDto>> evaluateDecision(@Context UriInfo context, EvaluateDecisionDto parameters);

  @PUT
  @Path("/history-time-to-live")
  @Consumes(MediaType.APPLICATION_JSON)
  void updateHistoryTimeToLive(HistoryTimeToLiveDto historyTimeToLiveDto);

}
