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

import org.camunda.bpm.engine.rest.dto.CountResultDto;
import org.camunda.bpm.engine.rest.dto.ResourceOptionsDto;
import org.camunda.bpm.engine.rest.dto.authorization.AuthorizationCheckResultDto;
import org.camunda.bpm.engine.rest.dto.authorization.AuthorizationCreateDto;
import org.camunda.bpm.engine.rest.dto.authorization.AuthorizationDto;
import org.camunda.bpm.engine.rest.sub.authorization.AuthorizationResource;

import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriInfo;
import java.util.List;

/**
 * @author Daniel Meyer
 *
 */
@Produces(MediaType.APPLICATION_JSON)
public interface AuthorizationRestService {

  public static final String PATH = "/authorization";

  @GET
  @Path("/check")
  @Produces(MediaType.APPLICATION_JSON)
  AuthorizationCheckResultDto isUserAuthorized(
      @QueryParam("permissionName") String permissionName,
      @QueryParam("resourceName") String resourceName,
      @QueryParam("resourceType") Integer resourceType,
      @QueryParam("resourceId") String resourceId);

  @Path("/{id}")
  AuthorizationResource getAuthorization(@PathParam("id") String id);

  @GET
  @Produces(MediaType.APPLICATION_JSON)
  List<AuthorizationDto> queryAuthorizations(@Context UriInfo uriInfo,
      @QueryParam("firstResult") Integer firstResult, @QueryParam("maxResults") Integer maxResults);

  @GET
  @Path("/count")
  @Produces(MediaType.APPLICATION_JSON)
  CountResultDto getAuthorizationCount(@Context UriInfo uriInfo);

  @POST
  @Path("/create")
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  AuthorizationDto createAuthorization(@Context UriInfo context, AuthorizationCreateDto dto);

  @OPTIONS
  @Produces(MediaType.APPLICATION_JSON)
  ResourceOptionsDto availableOperations(@Context UriInfo context);

}
