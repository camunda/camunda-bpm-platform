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
package org.camunda.bpm.engine.rest.history;

import org.camunda.bpm.engine.history.HistoricTaskInstanceQuery;
import org.camunda.bpm.engine.rest.dto.CountResultDto;
import org.camunda.bpm.engine.rest.dto.history.HistoricTaskInstanceDto;
import org.camunda.bpm.engine.rest.dto.history.HistoricTaskInstanceQueryDto;

import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriInfo;
import java.util.List;

/**
 * @author Roman Smirnov
 *
 */
@Path(HistoricTaskInstanceRestService.PATH)
@Produces(MediaType.APPLICATION_JSON)
public interface HistoricTaskInstanceRestService {

  public static final String PATH = "/task";

  /**
   * Exposes the {@link HistoricTaskInstanceQuery} interface as a REST
   * service.
   *
   * @param query
   * @param firstResult
   * @param maxResults
   * @return
   */
  @GET
  @Produces(MediaType.APPLICATION_JSON)
  List<HistoricTaskInstanceDto> getHistoricTaskInstances(@Context UriInfo uriInfo, @QueryParam("firstResult") Integer firstResult,
      @QueryParam("maxResults") Integer maxResults);

  /**
   * @param query
   * @param firstResult
   * @param maxResults
   * @return
   */
  @POST
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  List<HistoricTaskInstanceDto> queryHistoricTaskInstances(HistoricTaskInstanceQueryDto queryDto, @QueryParam("firstResult") Integer firstResult,
      @QueryParam("maxResults") Integer maxResults);

  @GET
  @Path("/count")
  @Produces(MediaType.APPLICATION_JSON)
  CountResultDto getHistoricTaskInstancesCount(@Context UriInfo uriInfo);

  @POST
  @Path("/count")
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  CountResultDto queryHistoricTaskInstancesCount(HistoricTaskInstanceQueryDto queryDto);

}
