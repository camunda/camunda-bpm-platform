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

import org.camunda.bpm.engine.history.HistoricActivityInstanceQuery;
import org.camunda.bpm.engine.rest.dto.CountResultDto;
import org.camunda.bpm.engine.rest.dto.history.HistoricDetailDto;
import org.camunda.bpm.engine.rest.dto.history.HistoricDetailQueryDto;
import org.camunda.bpm.engine.rest.sub.VariableResource;
import org.camunda.bpm.engine.rest.sub.history.HistoricDetailResource;

import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriInfo;
import java.util.List;

/**
 *
 * @author Roman Smirnov
 *
 */
@Path(HistoricVariableInstanceRestService.PATH)
@Produces(MediaType.APPLICATION_JSON)
public interface HistoricDetailRestService {

  public static final String PATH = "/detail";

  @Path("/{id}")
  HistoricDetailResource historicDetail(@PathParam("id") String detailId);

  /**
   * Exposes the {@link HistoricActivityInstanceQuery} interface as a REST
   * service.
   *
   * @param uriInfo
   * @param firstResult
   * @param maxResults
   * @return
   */
  @GET
  @Produces(MediaType.APPLICATION_JSON)
  List<HistoricDetailDto> getHistoricDetails(
      @Context UriInfo uriInfo,
      @QueryParam("firstResult") Integer firstResult,
      @QueryParam("maxResults") Integer maxResults,
      @QueryParam(VariableResource.DESERIALIZE_VALUES_QUERY_PARAM) @DefaultValue("true") boolean deserializeObjectValues);

  /**
   * Exposes the {@link HistoricActivityInstanceQuery} interface as a REST
   * service with additional query parameters (compared to the GET alternative).
   *
   * @param queryDto
   * @param firstResult
   * @param maxResults
   * @return
   */
  @POST
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  List<HistoricDetailDto> queryHistoricDetails(
    HistoricDetailQueryDto queryDto,
    @QueryParam("firstResult") Integer firstResult,
    @QueryParam("maxResults") Integer maxResults,
    @QueryParam(VariableResource.DESERIALIZE_VALUES_QUERY_PARAM) @DefaultValue("true") boolean deserializeObjectValues);

  @GET
  @Path("/count")
  @Produces(MediaType.APPLICATION_JSON)
  CountResultDto getHistoricDetailsCount(@Context UriInfo uriInfo);

}
