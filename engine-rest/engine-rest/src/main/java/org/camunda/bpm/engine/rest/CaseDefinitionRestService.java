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

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriInfo;

import org.camunda.bpm.engine.repository.CaseDefinitionQuery;
import org.camunda.bpm.engine.rest.dto.CountResultDto;
import org.camunda.bpm.engine.rest.dto.repository.CaseDefinitionDto;
import org.camunda.bpm.engine.rest.sub.repository.CaseDefinitionResource;

/**
 *
 * @author Roman Smirnov
 *
 */
@Produces(MediaType.APPLICATION_JSON)
public interface CaseDefinitionRestService {

  public static final String PATH = "/case-definition";

  @Path("/{id}")
  CaseDefinitionResource getCaseDefinitionById(@PathParam("id") String caseDefinitionId);

  @Path("/key/{key}")
  CaseDefinitionResource getCaseDefinitionByKey(@PathParam("key") String caseDefinitionKey);

  @Path("/key/{key}/tenant-id/{tenantId}")
  CaseDefinitionResource getCaseDefinitionByKeyAndTenantId(@PathParam("key") String caseDefinitionKey,
                                                           @PathParam("tenantId") String tenantId);

  /**
   * Exposes the {@link CaseDefinitionQuery} interface as a REST service.
   *
   * @param uriInfo
   * @param firstResult
   * @param maxResults
   * @return
   */
  @GET
  @Produces(MediaType.APPLICATION_JSON)
  List<CaseDefinitionDto> getCaseDefinitions(@Context UriInfo uriInfo,
                                             @QueryParam("firstResult") Integer firstResult,
                                             @QueryParam("maxResults") Integer maxResults);

  @GET
  @Path("/count")
  @Produces(MediaType.APPLICATION_JSON)
  CountResultDto getCaseDefinitionsCount(@Context UriInfo uriInfo);

}
