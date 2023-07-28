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
package org.camunda.bpm.cockpit.impl.plugin.resources;

import org.camunda.bpm.cockpit.db.QueryParameters;
import org.camunda.bpm.cockpit.impl.plugin.base.dto.ProcessDefinitionStatisticsDto;
import org.camunda.bpm.cockpit.impl.plugin.base.dto.query.ProcessDefinitionStatisticsQueryDto;
import org.camunda.bpm.cockpit.impl.plugin.base.sub.resources.ProcessDefinitionResource;
import org.camunda.bpm.cockpit.plugin.resource.AbstractPluginResource;
import org.camunda.bpm.engine.rest.dto.CountResultDto;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriInfo;
import java.util.List;

import static org.camunda.bpm.engine.authorization.Permissions.READ;
import static org.camunda.bpm.engine.authorization.Resources.PROCESS_DEFINITION;

public class ProcessDefinitionRestService extends AbstractPluginResource {

  public static final String PATH = "/process-definition";

  public ProcessDefinitionRestService(String engineName) {
    super(engineName);
  }

  @Path("/{id}")
  public ProcessDefinitionResource getProcessDefinition(@PathParam("id") String id) {
    return new ProcessDefinitionResource(getProcessEngine().getName(), id);
  }

  @GET
  @Path("/statistics-count")
  @Produces(MediaType.APPLICATION_JSON)
  public CountResultDto getStatisticsCount(@Context UriInfo uriInfo) {
    QueryParameters queryDto = new ProcessDefinitionStatisticsQueryDto(uriInfo.getQueryParameters());
    configureExecutionQuery(queryDto);
    long count = getQueryService().executeQueryRowCount("selectPDStatisticsCount", queryDto);
    return new CountResultDto(count);
  }

  @GET
  @Path("/statistics")
  @Produces(MediaType.APPLICATION_JSON)
  @Consumes(MediaType.APPLICATION_JSON)
  public List<ProcessDefinitionStatisticsDto> queryStatistics(@Context UriInfo uriInfo,
                                                              @QueryParam("firstResult") Integer firstResult,
                                                              @QueryParam("maxResults") Integer maxResults) {
    QueryParameters queryDto = new ProcessDefinitionStatisticsQueryDto(uriInfo.getQueryParameters());
    configureExecutionQuery(queryDto);
    queryDto.setFirstResult(firstResult != null && firstResult >= 0 ? firstResult : 0);
    queryDto.setMaxResults(maxResults != null && maxResults > 0 ? maxResults : Integer.MAX_VALUE);

    return getQueryService().executeQuery("selectPDStatistics", queryDto);
  }

  protected void configureExecutionQuery(QueryParameters query) {
    configureAuthorizationCheck(query);
    configureTenantCheck(query);
    addPermissionCheck(query, PROCESS_DEFINITION, "RES.KEY_", READ);
  }

}
