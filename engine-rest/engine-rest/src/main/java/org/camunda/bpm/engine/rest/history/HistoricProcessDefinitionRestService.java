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
package org.camunda.bpm.engine.rest.history;

import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriInfo;

import org.camunda.bpm.engine.rest.dto.CountResultDto;
import org.camunda.bpm.engine.rest.dto.history.CleanableHistoricProcessInstanceReportResultDto;
import org.camunda.bpm.engine.rest.dto.history.HistoricActivityStatisticsDto;

/**
*
* @author Roman Smirnov
*
*/
@Path(HistoricProcessDefinitionRestService.PATH)
@Produces(MediaType.APPLICATION_JSON)
public interface HistoricProcessDefinitionRestService {

  public static final String PATH = "/process-definition";

  @GET
  @Path("/{id}/statistics")
  @Produces(MediaType.APPLICATION_JSON)
  public List<HistoricActivityStatisticsDto> getHistoricActivityStatistics(@Context UriInfo uriInfo, @PathParam("id") String processDefinitionId);

  @GET
  @Path("/cleanable-process-instance-report")
  @Produces(MediaType.APPLICATION_JSON)
  public List<CleanableHistoricProcessInstanceReportResultDto> getCleanableHistoricProcessInstanceReport(@Context UriInfo uriInfo,
                                                                                                         @QueryParam("firstResult") Integer firstResult,
                                                                                                         @QueryParam("maxResults") Integer maxResults);

  @GET
  @Path("/cleanable-process-instance-report/count")
  @Produces(MediaType.APPLICATION_JSON)
  public CountResultDto getCleanableHistoricProcessInstanceReportCount(@Context UriInfo uriInfo);

}
