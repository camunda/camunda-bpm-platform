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
import javax.ws.rs.OPTIONS;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriInfo;

import org.camunda.bpm.engine.rest.dto.CountResultDto;
import org.camunda.bpm.engine.rest.dto.ResourceOptionsDto;
import org.camunda.bpm.engine.rest.dto.identity.TenantDto;
import org.camunda.bpm.engine.rest.sub.identity.TenantResource;

@Produces(MediaType.APPLICATION_JSON)
public interface TenantRestService {

  public static final String PATH = "/tenant";

  @Path("/{id}")
  TenantResource getTenant(@PathParam("id") String id);

  @GET
  @Produces(MediaType.APPLICATION_JSON)
  List<TenantDto> queryTenants(@Context UriInfo uriInfo,
      @QueryParam("firstResult") Integer firstResult, @QueryParam("maxResults") Integer maxResults);

  @GET
  @Path("/count")
  @Produces(MediaType.APPLICATION_JSON)
  CountResultDto getTenantCount(@Context UriInfo uriInfo);

  @POST
  @Path("/create")
  @Consumes(MediaType.APPLICATION_JSON)
  void createTenant(TenantDto tenant);

  @OPTIONS
  @Produces(MediaType.APPLICATION_JSON)
  ResourceOptionsDto availableOperations(@Context UriInfo context);

}
