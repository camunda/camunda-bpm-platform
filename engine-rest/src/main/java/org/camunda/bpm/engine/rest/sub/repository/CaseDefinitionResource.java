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
package org.camunda.bpm.engine.rest.sub.repository;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.camunda.bpm.engine.rest.dto.repository.CaseDefinitionDiagramDto;
import org.camunda.bpm.engine.rest.dto.repository.CaseDefinitionDto;
import org.camunda.bpm.engine.rest.dto.runtime.CaseInstanceDto;
import org.camunda.bpm.engine.rest.dto.runtime.CreateCaseInstanceDto;

/**
 * @author Roman Smirnov
 *
 */
public interface CaseDefinitionResource {

  @GET
  @Produces(MediaType.APPLICATION_JSON)
  CaseDefinitionDto getCaseDefinition();

  @GET
  @Path("/xml")
  @Produces(MediaType.APPLICATION_JSON)
  CaseDefinitionDiagramDto getCaseDefinitionCmmnXml();

  @POST
  @Path("/create")
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  CaseInstanceDto createCaseInstance(@Context UriInfo context, CreateCaseInstanceDto parameters);

  @GET
  @Path("/diagram")
  Response getCaseDefinitionDiagram();

}
