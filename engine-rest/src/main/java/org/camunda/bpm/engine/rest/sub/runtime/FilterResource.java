/* Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
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

import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.OPTIONS;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriInfo;

import org.camunda.bpm.engine.rest.dto.CountResultDto;
import org.camunda.bpm.engine.rest.dto.ResourceOptionsDto;
import org.camunda.bpm.engine.rest.dto.runtime.FilterDto;
import org.camunda.bpm.engine.rest.hal.Hal;
import org.camunda.bpm.engine.rest.hal.HalResource;

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
  @Produces(MediaType.APPLICATION_JSON)
  Object executeSingleResult();

  @GET
  @Path("/singleResult")
  @Produces(Hal.MEDIA_TYPE_HAL)
  HalResource executeHalSingleResult();

  @POST
  @Path("/singleResult")
  @Produces(MediaType.APPLICATION_JSON)
  @Consumes(MediaType.APPLICATION_JSON)
  Object querySingleResult(String extendingQuery);

  @POST
  @Path("/singleResult")
  @Produces(Hal.MEDIA_TYPE_HAL)
  @Consumes(MediaType.APPLICATION_JSON)
  HalResource queryHalSingleResult(String extendingQuery);

  @GET
  @Path("/list")
  @Produces(MediaType.APPLICATION_JSON)
  List<Object> executeList(@QueryParam("firstResult") Integer firstResult, @QueryParam("maxResults") Integer maxResults);

  @GET
  @Path("/list")
  @Produces(Hal.MEDIA_TYPE_HAL)
  HalResource executeHalList(@QueryParam("firstResult") Integer firstResult, @QueryParam("maxResults") Integer maxResults);

  @POST
  @Path("/list")
  @Produces(MediaType.APPLICATION_JSON)
  @Consumes(MediaType.APPLICATION_JSON)
  List<Object> queryList(String extendingQuery, @QueryParam("firstResult") Integer firstResult, @QueryParam("maxResults") Integer maxResults);

  @POST
  @Path("/list")
  @Produces(Hal.MEDIA_TYPE_HAL)
  @Consumes(MediaType.APPLICATION_JSON)
  HalResource queryHalList(String extendingQuery, @QueryParam("firstResult") Integer firstResult, @QueryParam("maxResults") Integer maxResults);

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
