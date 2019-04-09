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
package org.camunda.bpm.engine.rest;

import org.camunda.bpm.engine.rest.dto.CountResultDto;
import org.camunda.bpm.engine.rest.dto.runtime.VariableInstanceDto;
import org.camunda.bpm.engine.rest.dto.runtime.VariableInstanceQueryDto;
import org.camunda.bpm.engine.rest.sub.VariableResource;
import org.camunda.bpm.engine.rest.sub.runtime.VariableInstanceResource;
import org.camunda.bpm.engine.runtime.VariableInstanceQuery;

import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriInfo;
import java.util.List;

/**
 * @author roman.smirnov
 */
@Produces(MediaType.APPLICATION_JSON)
public interface VariableInstanceRestService {

  public static final String PATH = "/variable-instance";

  @Path("/{id}")
  VariableInstanceResource getVariableInstance(@PathParam("id") String id);

  /**
   * Exposes the {@link VariableInstanceQuery} interface as a REST service.
   *
   * @param uriInfo
   * @param firstResult
   * @param maxResults
   * @return
   */
  @GET
  @Produces(MediaType.APPLICATION_JSON)
  List<VariableInstanceDto> getVariableInstances(@Context UriInfo uriInfo,
      @QueryParam("firstResult") Integer firstResult,
      @QueryParam("maxResults") Integer maxResults,
      @QueryParam(VariableResource.DESERIALIZE_VALUES_QUERY_PARAM) @DefaultValue("true") boolean deserializeObjectValues);

  /**
   * Expects the same parameters as
   * {@link VariableInstanceRestService#getVariableInstances(UriInfo, Integer, Integer)} (as a JSON message body)
   * and allows for any number of variable checks.
   *
   * @param queryDto
   * @param firstResult
   * @param maxResults
   * @return
   */
  @POST
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  List<VariableInstanceDto> queryVariableInstances(VariableInstanceQueryDto queryDto,
      @QueryParam("firstResult") Integer firstResult,
      @QueryParam("maxResults") Integer maxResults,
      @QueryParam(VariableResource.DESERIALIZE_VALUES_QUERY_PARAM) @DefaultValue("true") boolean deserializeObjectValues);

  @GET
  @Path("/count")
  @Produces(MediaType.APPLICATION_JSON)
  CountResultDto getVariableInstancesCount(@Context UriInfo uriInfo);

  @POST
  @Path("/count")
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  CountResultDto queryVariableInstancesCount(VariableInstanceQueryDto queryDto);

}
