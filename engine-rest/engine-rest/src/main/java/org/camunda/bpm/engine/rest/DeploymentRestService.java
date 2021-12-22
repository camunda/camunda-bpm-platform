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
import org.camunda.bpm.engine.rest.dto.repository.DeploymentDto;
import org.camunda.bpm.engine.rest.mapper.MultipartFormData;
import org.camunda.bpm.engine.rest.sub.repository.DeploymentResource;

import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriInfo;
import java.util.List;
import java.util.Set;

@Produces(MediaType.APPLICATION_JSON)
public interface DeploymentRestService {

  public static final String PATH = "/deployment";

  @Path("/{id}")
  DeploymentResource getDeployment(@PathParam("id") String deploymentId);

  @GET
  @Produces(MediaType.APPLICATION_JSON)
  List<DeploymentDto> getDeployments(@Context UriInfo uriInfo,
                                     @QueryParam("firstResult") Integer firstResult,
                                     @QueryParam("maxResults") Integer maxResults);

  @POST
  @Path("/create")
  @Consumes(MediaType.MULTIPART_FORM_DATA)
  @Produces(MediaType.APPLICATION_JSON)
  DeploymentDto createDeployment(@Context UriInfo uriInfo, MultipartFormData multipartFormData);

  @GET
  @Path("/count")
  @Produces(MediaType.APPLICATION_JSON)
  CountResultDto getDeploymentsCount(@Context UriInfo uriInfo);

  @GET
  @Path("/registered")
  @Produces(MediaType.APPLICATION_JSON)
  Set<String> getRegisteredDeployments(@Context UriInfo uriInfo);

}
