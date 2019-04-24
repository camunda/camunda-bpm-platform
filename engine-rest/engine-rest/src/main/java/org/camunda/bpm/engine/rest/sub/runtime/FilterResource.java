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
package org.camunda.bpm.engine.rest.sub.runtime;

import org.camunda.bpm.engine.rest.dto.CountResultDto;
import org.camunda.bpm.engine.rest.dto.ResourceOptionsDto;
import org.camunda.bpm.engine.rest.dto.runtime.FilterDto;
import org.camunda.bpm.engine.rest.hal.Hal;

import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.UriInfo;

/**
 * @author Sebastian Menski
 */
public interface FilterResource {

  @GET
  @Produces(MediaType.APPLICATION_JSON)
  FilterDto getFilter(@QueryParam("itemCount") Boolean itemCount);

  @DELETE
  void deleteFilter();

  @PUT
  @Consumes(MediaType.APPLICATION_JSON)
  void updateFilter(FilterDto filterDto);

  @GET
  @Path("/singleResult")
  @Produces({MediaType.APPLICATION_JSON, Hal.APPLICATION_HAL_JSON})
  Object executeSingleResult(@Context Request request);

  @POST
  @Path("/singleResult")
  @Produces({MediaType.APPLICATION_JSON, Hal.APPLICATION_HAL_JSON})
  @Consumes(MediaType.APPLICATION_JSON)
  Object querySingleResult(@Context Request request, String extendingQuery);

  @GET
  @Path("/list")
  @Produces({MediaType.APPLICATION_JSON, Hal.APPLICATION_HAL_JSON})
  Object executeList(@Context Request request, @QueryParam("firstResult") Integer firstResult, @QueryParam("maxResults") Integer maxResults);

  @POST
  @Path("/list")
  @Produces({MediaType.APPLICATION_JSON, Hal.APPLICATION_HAL_JSON})
  @Consumes(MediaType.APPLICATION_JSON)
  Object queryList(@Context Request request, String extendingQuery,
                         @QueryParam("firstResult") Integer firstResult, @QueryParam("maxResults") Integer maxResults);

  @GET
  @Path("/count")
  @Produces(MediaType.APPLICATION_JSON)
  CountResultDto executeCount();

  @POST
  @Path("/count")
  @Produces(MediaType.APPLICATION_JSON)
  @Consumes(MediaType.APPLICATION_JSON)
  CountResultDto queryCount(String extendingQuery);

  @OPTIONS
  @Produces(MediaType.APPLICATION_JSON)
  ResourceOptionsDto availableOperations(@Context UriInfo context);


}
