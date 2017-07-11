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

import java.util.List;

import org.camunda.bpm.engine.rest.dto.history.HistoricCaseActivityStatisticsDto;
import org.camunda.bpm.engine.rest.dto.CountResultDto;
import org.camunda.bpm.engine.rest.dto.history.CleanableHistoricCaseInstanceReportResultDto;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriInfo;

/**
 * @author Roman Smirnov
 *
 */
@Path(HistoricCaseDefinitionRestService.PATH)
@Produces(MediaType.APPLICATION_JSON)
public interface HistoricCaseDefinitionRestService {

  public static final String PATH = "/case-definition";

  @GET
  @Path("/{id}/statistics")
  @Produces(MediaType.APPLICATION_JSON)
  public List<HistoricCaseActivityStatisticsDto> getHistoricCaseActivityStatistics(@PathParam("id") String caseDefinitionId);

  @GET
  @Path("/cleanable-case-instance-report")
  @Produces(MediaType.APPLICATION_JSON)
  public List<CleanableHistoricCaseInstanceReportResultDto> getCleanableHistoricCaseInstanceReport(@Context UriInfo uriInfo,
                                                                                             @QueryParam("firstResult") Integer firstResult,
                                                                                             @QueryParam("maxResults") Integer maxResults);

  @GET
  @Path("/cleanable-case-instance-report/count")
  @Produces(MediaType.APPLICATION_JSON)
  public CountResultDto getCleanableHistoricCaseInstanceReportCount(@Context UriInfo uriInfo);

}
