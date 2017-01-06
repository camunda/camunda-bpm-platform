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

import org.camunda.bpm.engine.rest.dto.CountResultDto;
import org.camunda.bpm.engine.rest.dto.history.HistoricExternalTaskLogDto;
import org.camunda.bpm.engine.rest.dto.history.HistoricExternalTaskLogQueryDto;
import org.camunda.bpm.engine.rest.sub.history.HistoricExternalTaskLogResource;

import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriInfo;
import java.util.List;

/**
 * @since 7.7
 */
@Path(HistoricExternalTaskLogRestService.PATH)
@Produces(MediaType.APPLICATION_JSON)
public interface HistoricExternalTaskLogRestService {

  public static final String PATH = "/external-task-log";

  @Path("/{id}")
  HistoricExternalTaskLogResource getHistoricExternalTaskLog(@PathParam("id") String historicExternalTaskLogId);

  @GET
  @Produces(MediaType.APPLICATION_JSON)
  List<HistoricExternalTaskLogDto> getHistoricExternalTaskLogs(@Context UriInfo uriInfo,
                                                               @QueryParam("firstResult") Integer firstResult, @QueryParam("maxResults") Integer maxResults);

  @POST
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  List<HistoricExternalTaskLogDto> queryHistoricExternalTaskLogs(HistoricExternalTaskLogQueryDto queryDto,
                                                                 @QueryParam("firstResult") Integer firstResult,
                                                                 @QueryParam("maxResults") Integer maxResults);

  @GET
  @Path("/count")
  @Produces(MediaType.APPLICATION_JSON)
  CountResultDto getHistoricExternalTaskLogsCount(@Context UriInfo uriInfo);

  @POST
  @Path("/count")
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  CountResultDto queryHistoricExternalTaskLogsCount(HistoricExternalTaskLogQueryDto queryDto);
}
