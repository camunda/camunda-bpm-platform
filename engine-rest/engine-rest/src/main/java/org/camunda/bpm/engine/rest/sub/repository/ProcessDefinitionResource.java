/*
 * Copyright Camunda Services GmbH and/or licensed to Camunda Services GmbH
 * under one or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information regarding copyright
 * ownership. Camunda licenses this file to you under the Apache License,
 * Version 2.0; you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.camunda.bpm.engine.rest.sub.repository;

import org.camunda.bpm.engine.rest.dto.StatisticsResultDto;
import org.camunda.bpm.engine.rest.dto.HistoryTimeToLiveDto;
import org.camunda.bpm.engine.rest.dto.VariableValueDto;
import org.camunda.bpm.engine.rest.dto.batch.BatchDto;
import org.camunda.bpm.engine.rest.dto.repository.CalledProcessDefinitionDto;
import org.camunda.bpm.engine.rest.dto.repository.ProcessDefinitionDiagramDto;
import org.camunda.bpm.engine.rest.dto.repository.ProcessDefinitionDto;
import org.camunda.bpm.engine.rest.dto.repository.ProcessDefinitionSuspensionStateDto;
import org.camunda.bpm.engine.rest.dto.runtime.ProcessInstanceDto;
import org.camunda.bpm.engine.rest.dto.runtime.RestartProcessInstanceDto;
import org.camunda.bpm.engine.rest.dto.runtime.StartProcessInstanceDto;
import org.camunda.bpm.engine.rest.dto.task.FormDto;
import org.camunda.bpm.engine.rest.sub.VariableResource;

import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.util.List;
import java.util.Map;

public interface ProcessDefinitionResource {

  @GET
  @Produces(MediaType.APPLICATION_JSON)
  ProcessDefinitionDto getProcessDefinition();

  @GET
  @Path("/xml")
  @Produces(MediaType.APPLICATION_JSON)
  ProcessDefinitionDiagramDto getProcessDefinitionBpmn20Xml();

  @GET
  @Path("/diagram")
  Response getProcessDefinitionDiagram();

  @DELETE
  Response deleteProcessDefinition(@QueryParam("cascade") boolean cascade,
                                   @QueryParam("skipCustomListeners") boolean skipCustomListeners,
                                   @QueryParam("skipIoMappings") boolean skipIoMappings);

  @POST
  @Path("/start")
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  ProcessInstanceDto startProcessInstance(@Context UriInfo context, StartProcessInstanceDto parameters);

  @POST
  @Path("/restart")
  @Consumes(MediaType.APPLICATION_JSON)
  void restartProcessInstance(RestartProcessInstanceDto restartProcessInstanceDto);

  @POST
  @Path("/restart-async")
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  BatchDto restartProcessInstanceAsync(RestartProcessInstanceDto restartProcessInstanceDto);

  @POST
  @Path("/submit-form")
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  ProcessInstanceDto submitForm(@Context UriInfo context, StartProcessInstanceDto parameters);

  @GET
  @Path("/statistics")
  @Produces(MediaType.APPLICATION_JSON)
  List<StatisticsResultDto> getActivityStatistics(@QueryParam("failedJobs") Boolean includeFailedJobs, @QueryParam("incidents") Boolean includeIncidents, @QueryParam("incidentsForType") String includeIncidentsForType);

  @GET
  @Path("/startForm")
  @Produces(MediaType.APPLICATION_JSON)
  FormDto getStartForm();

  @GET
  @Path("/deployed-start-form")
  Response getDeployedStartForm();

  @GET
  @Path("/rendered-form")
  @Produces(MediaType.APPLICATION_XHTML_XML)
  Response getRenderedForm();

  @PUT
  @Path("/suspended")
  @Consumes(MediaType.APPLICATION_JSON)
  void updateSuspensionState(ProcessDefinitionSuspensionStateDto dto);

  @PUT
  @Path("/history-time-to-live")
  @Consumes(MediaType.APPLICATION_JSON)
  void updateHistoryTimeToLive(HistoryTimeToLiveDto historyTimeToLiveDto);

  @GET
  @Path("/form-variables")
  @Produces(MediaType.APPLICATION_JSON)
  Map<String, VariableValueDto> getFormVariables(@QueryParam("variableNames") String variableNames,
      @QueryParam(VariableResource.DESERIALIZE_VALUES_QUERY_PARAM) @DefaultValue("true") boolean deserializeValues);

  @GET
  @Path("/static-called-process-definitions")
  @Produces(MediaType.APPLICATION_JSON)
  List<CalledProcessDefinitionDto> getStaticCalledProcessDefinitions();
}
