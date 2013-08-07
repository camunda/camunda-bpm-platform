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

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriInfo;

import org.camunda.bpm.engine.rest.dto.CountResultDto;
import org.camunda.bpm.engine.rest.dto.runtime.VariableInstanceDto;
import org.camunda.bpm.engine.rest.dto.runtime.VariableInstanceQueryDto;
import org.camunda.bpm.engine.runtime.VariableInstanceQuery;

/**
 * @author roman.smirnov
 */
@Path(VariableInstanceRestService.PATH)
@Produces(MediaType.APPLICATION_JSON)
public interface VariableInstanceRestService {

  public static final String PATH = "/variable-instance";
  
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
      @QueryParam("maxResults") Integer maxResults);

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
      @QueryParam("maxResults") Integer maxResults);
  
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
